package com.dzytsiuk.webserver.http.entity;

import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.io.ResponseStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class HttpResponse implements HttpServletResponse {
    private static final DateTimeFormatter COOKIE_EXPIRE_TIME_FORMATTER = DateTimeFormatter.ofPattern("E, dd-MMM-yyyy HH:mm:ss");
    private static final String SESSION_ID_QUERY_PARAM = "sessionId=";
    private static final StandardHttpStatus DEFAULT_STATUS = StandardHttpStatus.OK;
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private ResponseStream outputStream;
    private HttpStatus status = new HttpStatus(DEFAULT_STATUS);
    private HttpVersion httpVersion;
    private OutputStream socketOutputStream;
    private List<Cookie> cookies;
    private long contentLength;
    private Charset characterEncoding;
    private List<HttpHeader> headers = new ArrayList<>();
    private Locale locale = Locale.getDefault();
    private PrintWriter printWriter;
    private HttpRequest httpRequest;

    @Override
    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
        String cookieHeaderString = getSetCookieHeaderString(cookie);
        System.out.println(cookieHeaderString);
        addHeader(HttpHeaderName.SET_COOKIE.getHeaderName(), cookieHeaderString);
    }

    @Override
    public boolean containsHeader(String name) {
        Optional<HttpHeader> header = headers.stream().filter(httpHeader -> name.equalsIgnoreCase(httpHeader.getName())).findFirst();
        return header.isPresent();
    }

    @Override
    public String encodeURL(String url) {
        if (httpRequest.isRequestedSessionIdFromURL()) {
            HttpSession session = httpRequest.getSession();
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            url += SESSION_ID_QUERY_PARAM + session.getId();
        }
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        setStatus(sc);
        ((ResponseStream) getOutputStream()).getHttpResponseWriter().writeErrorResponse(msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException("Response was already committed");
        }
        sendError(sc, null);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(StandardHttpStatus.REDIRECT.getCode());
        addHeader(HttpHeaderName.LOCATION.getHeaderName(), location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        removePreviousHeader(name);
        addDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        headers.add(new HttpHeader(name, String.valueOf(date)));
    }

    @Override
    public void setHeader(String name, String value) {
        removePreviousHeader(name);
        addHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.add(new HttpHeader(name, value));
    }

    @Override
    public void setIntHeader(String name, int value) {
        removePreviousHeader(name);
        addIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        headers.add(new HttpHeader(name, String.valueOf(value)));
    }

    @Override
    public void setStatus(int sc) {
        StandardHttpStatus standardHttpStatus = StandardHttpStatus.getStatusByCode(sc);
        if (standardHttpStatus == null) {
            this.status = new HttpStatus(sc, null);
        } else {
            this.status = new HttpStatus(standardHttpStatus);
        }
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.status = new HttpStatus(sc, sm);
    }

    @Override
    public int getStatus() {
        return status.getCode();
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.stream()
                .filter(httpHeader -> name.equals(httpHeader.getName()))
                .map(HttpHeader::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.stream()
                .map(HttpHeader::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getCharacterEncoding() {
        if (characterEncoding == null) {
            return DEFAULT_CHARSET.name();
        }
        return this.characterEncoding.name();
    }

    @Override
    public String getContentType() {
        Optional<HttpHeader> optionalHeader = headers.stream()
                .filter(httpHeader -> HttpHeaderName.CONTENT_TYPE.getHeaderName().equalsIgnoreCase(httpHeader.getName()))
                .findFirst();
        return optionalHeader.map(HttpHeader::getValue).orElse(null);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null) {
            String characterEncoding = getCharacterEncoding();
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream, characterEncoding));
        }
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.characterEncoding = Charset.forName(charset);
    }

    @Override
    public void setContentLength(int len) {
        this.contentLength = len;
    }

    @Override
    public void setContentLengthLong(long len) {
        this.contentLength = len;
    }

    @Override
    public void setContentType(String type) {
        if (type == null) {
            return;
        }
        headers.add(new HttpHeader(HttpHeaderName.CONTENT_TYPE.getHeaderName(), type));
        if (type.contains("charset")) {
            String charset = type.split("charset=")[1];
            setCharacterEncoding(charset);
        }
    }

    @Override
    public void setBufferSize(int size) {
        try {
            ((ResponseStream) getOutputStream()).setBufferCapacity(size);
        } catch (IOException e) {
            throw new HttpException("Unable to set buffer size", e);
        }
    }

    @Override
    public int getBufferSize() {
        try {
            return ((ResponseStream) getOutputStream()).getBufferCapacity();
        } catch (IOException e) {
            throw new HttpException("Unable to get buffer size", e);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        getOutputStream().flush();
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Buffer is already committed");
        }
        try {
            ((ResponseStream) getOutputStream()).reset();
        } catch (IOException e) {
            throw new HttpException("Unable to reset buffer", e);
        }
    }

    @Override
    public boolean isCommitted() {
        try {
            return ((ResponseStream) getOutputStream()).isCommited();
        } catch (IOException e) {
            throw new HttpException("Unable to check buffer", e);
        }
    }

    @Override
    public void reset() {
        resetBuffer();
        headers = new ArrayList<>();
        setStatus(DEFAULT_STATUS.getCode());
    }

    @Override
    public void setLocale(Locale loc) {
        this.locale = loc;
        if (characterEncoding != null) {
            String encoding = characterEncoding.displayName(locale);
            characterEncoding = Charset.forName(encoding);
        }
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    private void removePreviousHeader(String name) {
        headers.removeIf(httpHeader -> name.equalsIgnoreCase(httpHeader.getName()));
    }

    public int getContentLength() {
        return (int) contentLength;
    }

    public List<HttpHeader> getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public OutputStream getSocketOutputStream() {
        return socketOutputStream;
    }

    public void setSocketOutputStream(OutputStream socketOutputStream) {
        this.socketOutputStream = socketOutputStream;
    }

    public void setOutputStream(ResponseStream outputStream) throws UnsupportedEncodingException {
        this.outputStream = outputStream;
    }

    public HttpStatus getHttpStatus() {
        return status;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    private String getSetCookieHeaderString(Cookie cookie) {
        StringBuilder sb = new StringBuilder(cookie.getName() + "=" + cookie.getValue());
        int maxAge = cookie.getMaxAge();
        if (maxAge != 0) {
            LocalDateTime localExpireTime = LocalDateTime.now().plus(maxAge, ChronoUnit.SECONDS);
            ZonedDateTime zonedExpireTime = localExpireTime.atZone(ZoneOffset.UTC);
            String format = zonedExpireTime.format(COOKIE_EXPIRE_TIME_FORMATTER);
            sb.append("; expires=").append(format);
        }
        String domain = cookie.getDomain();
        if (domain != null && !domain.isEmpty()) {
            sb.append("; domain=").append(domain);
        }
        String path = cookie.getPath();
        if (path != null && !path.isEmpty()) {
            sb.append("; path=").append(path);
        }
        if (cookie.getSecure()) {
            sb.append("; secure");
        }
        if (cookie.isHttpOnly()) {
            sb.append("; httponly");
        }
        return sb.toString();
    }
}
