package com.dzytsiuk.webserver.http.processor;

import com.dzytsiuk.webserver.http.HttpRequest;
import com.dzytsiuk.webserver.http.entity.HttpHeaderName;
import com.dzytsiuk.webserver.http.entity.HttpMethod;
import com.dzytsiuk.webserver.http.entity.HttpVersion;
import com.dzytsiuk.webserver.http.io.RequestStream;
import com.dzytsiuk.webserver.http.io.HttpInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HttpRequestParser {
    private static final String REGEXP_QUERY_STRING = "(?<=\\?).+";
    private static final String REGEXP_APP_NAME = "\\/(.*?)[\\/\\?]";
    private static final String REGEXP_URI = "(?:.*?\\/){2}(.+(?=\\?)|.+)";
    private static final String REGEXP_SESSION_ID="[j]?sessionId=(.+(?=\\&)|.+)";
    private static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";

    private final Logger log = LoggerFactory.getLogger(getClass());

    HttpRequest getHttpRequest(InputStream inputStream) throws IOException {
        HttpInputStream streamReader = new HttpInputStream(inputStream);
        //read request line
        String requestFirstLine = streamReader.readLine();
        HttpRequest httpRequest = new HttpRequest();
        setRequestLineToHttpRequest(requestFirstLine, httpRequest);
        //read headers
        String headerLine;
        while (!(headerLine = streamReader.readLine()).isEmpty()) {
            log.debug("Adding header {}", headerLine);
            addHeader(headerLine, httpRequest);
        }
        //if content length is set then request has a body
        int contentLength = httpRequest.getIntHeader(HttpHeaderName.CONTENT_LENGTH.getHeaderName());
        httpRequest.setContentLength(contentLength);
        InputStream remainingRequestInputStream = getRemainingRequestInputStream(inputStream, contentLength);
        log.debug("Setting request body stream of content length {}", contentLength);
        httpRequest.setInputStream(new RequestStream(remainingRequestInputStream));
        return httpRequest;
    }

    InputStream getRemainingRequestInputStream(InputStream inputStream, int contentLength) throws IOException {
        ByteBuffer byteBuffer;
        if (contentLength == -1) {
            byteBuffer = ByteBuffer.allocate(0);
        } else {
            byteBuffer = ByteBuffer.allocate(contentLength);
            byte[] buffer = new byte[contentLength];
            inputStream.read(buffer);
            byteBuffer.put(buffer);
        }
        byteBuffer.flip();
        return new ByteArrayInputStream(byteBuffer.array());

    }

    void addHeader(String headerLine, HttpRequest httpRequest) {
        String[] headerNameValue = headerLine.split(":");
        Map<HttpHeaderName, String> headerMap = httpRequest.getHeaderMap();
        String headerName = headerNameValue[0].trim();
        String headerValue = headerNameValue[1].trim();
        HttpHeaderName httpHeaderName = HttpHeaderName.getHeaderByHeaderName(headerName);
        if (httpHeaderName != null) {
            if (httpHeaderName == HttpHeaderName.COOKIE) {
                setCookieList(httpRequest, headerValue);
            } else {
                headerMap.put(httpHeaderName, headerValue);
            }
        }
        if (httpHeaderName == HttpHeaderName.ACCEPT_LANGUAGE) {
            setRequestLanguage(httpRequest, headerValue);
        }
    }

    void setRequestLanguage(HttpRequest httpRequest, String acceptLanguage) {
        String[] languages = acceptLanguage.split(",");
        for (String language : languages) {
            String[] languageQuality = language.trim().split(";q=");
            if (languageQuality.length == 2) {
                Locale locale = new Locale(languageQuality[0].trim());
                httpRequest.addLocale(Double.parseDouble(languageQuality[1].trim()), locale);
            } else {
                Locale locale = new Locale(language.trim());
                httpRequest.addLocale(1.0, locale);
            }
        }
    }

    private void setCookieList(HttpRequest httpRequest, String headerValue) {
        List<Cookie> cookiesList = new ArrayList<>();
        String[] cookies = headerValue.split(";\\s");
        for (String cookie : cookies) {
            String[] cookieNameValue = cookie.split("=");
            String name = cookieNameValue[0].trim();
            String value = cookieNameValue[1].trim();
            if(name.equalsIgnoreCase(JSESSIONID_COOKIE_NAME)){
                httpRequest.setSessionId(value);
                httpRequest.setSessionIdFromCookie(true);
            }
            cookiesList.add(new Cookie(name, value));
        }
        httpRequest.setCookies(cookiesList);
    }

    void setRequestLineToHttpRequest(String requestFirstLine, HttpRequest httpRequest) {
        log.info("Processing request line '{}'", requestFirstLine);
        String[] strings = requestFirstLine.split("\\s");
        httpRequest.setMethod(HttpMethod.getMethodByName(strings[0].trim()));
        String url = strings[1].trim();
        httpRequest.setRequestUrl(url);

        String applicationName = getMatchedString(REGEXP_APP_NAME, url, 1);
        httpRequest.setApplicationName(applicationName);

        String uri = getMatchedString(REGEXP_URI, url, 1);
        httpRequest.setRequestUri("/" + uri);

        String queryString = getMatchedString(REGEXP_QUERY_STRING, url, 0);
        if(queryString!= null ){
            String matchedString = getMatchedString(REGEXP_SESSION_ID, queryString, 1);
            httpRequest.setSessionId(matchedString);
            httpRequest.setSessionIdFromUrl(true);
        }
        httpRequest.setQueryString(queryString);

        httpRequest.setHttpVersion(HttpVersion.getVersionByName(strings[2].trim()));
    }

    private String getMatchedString(String regexp, String url, int group) {
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(group);
        }
        return null;
    }
}
