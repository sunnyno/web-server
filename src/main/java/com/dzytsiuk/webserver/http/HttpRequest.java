package com.dzytsiuk.webserver.http;

import com.dzytsiuk.webserver.http.entity.HttpHeaderName;
import com.dzytsiuk.webserver.http.entity.HttpMethod;
import com.dzytsiuk.webserver.http.entity.HttpVersion;
import com.dzytsiuk.webserver.http.io.RequestStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.Charset;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpRequest implements HttpServletRequest {
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private HttpMethod method;
    private String queryString;
    private String requestUri;
    private String requestUrl;
    private RequestStream inputStream;
    private HttpVersion httpVersion;
    private String applicationName;
    private Map<HttpHeaderName, String> headerMap;
    private List<Cookie> cookies;
    private BufferedReader bufferedReader;
    private String servletPath;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private Charset characterEncoding;
    private int contentLength;
    private String serverName;
    private int remotePort;
    private String remoteHostName;
    private Map<Double, Locale> locales = new HashMap<>();
    private String serverAddress;
    private int localPort;

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies == null) {
            return null;
        }
        return cookies.toArray(new Cookie[0]);
    }

    @Override
    public long getDateHeader(String name) {
        Object header = headerMap.get(HttpHeaderName.getHeaderByHeaderName(name));
        if (header == null) {
            return -1;
        }
        try {
            Date headerDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z").parse((String) header);
            return headerDate.getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Header cannot be parsed to date", e);
        }
    }

    @Override
    public String getHeader(String name) {
        return headerMap.get(HttpHeaderName.getHeaderByHeaderName(name));
    }

    @Override
    public Enumeration getHeaders(String name) {
        ArrayList<String> headerValue = new ArrayList<>();
        headerValue.add(getHeader(name));
        return Collections.enumeration(headerValue);
    }

    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(headerMap.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        String header = getHeader(name);
        if (header == null) {
            return -1;
        } else {
            return Integer.parseInt(header);
        }
    }

    @Override
    public String getMethod() {
        if (method == null) {
            return null;
        }
        return method.getMethodName();
    }

    @Override
    public String getPathInfo() {
        return requestUri;
    }

    @Override
    public String getPathTranslated() {
        return requestUri;
    }

    @Override
    public String getContextPath() {
        return requestUri;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public StringBuffer getRequestURL() {
        if (queryString != null) {
            return new StringBuffer(requestUrl.replace(queryString, ""));
        }
        return new StringBuffer(requestUrl);
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding.name();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = Charset.forName(env);
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return getHeader(HttpHeaderName.CONTENT_TYPE.getHeaderName());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[]{getParameter(name)};
    }

    @Override
    public Map getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        return httpVersion.getName();
    }

    @Override
    public String getScheme() {
        return httpVersion.getName().split("/")[1];
    }

    @Override
    public String getServerName() {
        String host = getHeader(HttpHeaderName.HOST.getHeaderName());
        if (host != null) {
            String[] hostNamePort = host.split(":");
            if (hostNamePort.length == 2) {
                serverName = hostNamePort[0];
                remotePort = Integer.parseInt(hostNamePort[1]);
            } else {
                serverName = host;
            }
        }
        return serverName;
    }

    @Override
    public int getServerPort() {
        getServerName();
        return remotePort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (bufferedReader == null) {
            if (characterEncoding == null) {
                characterEncoding = DEFAULT_CHARSET;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, characterEncoding));
        }
        return bufferedReader;
    }

    @Override
    public String getRemoteAddr() {
        return serverName;
    }

    @Override
    public String getRemoteHost() {
        return remoteHostName;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        Optional<Double> max = locales.keySet().stream().max(Double::compareTo);
        return max.map(quality -> locales.get(quality)).orElse(DEFAULT_LOCALE);
    }

    @Override
    public Enumeration getLocales() {
        Collection<Locale> localeCollection = locales.values();
        if (localeCollection.size() == 0) {
            locales.put(1.0, DEFAULT_LOCALE);
        }
        return Collections.enumeration(localeCollection);
    }

    @Override
    public boolean isSecure() {
        return httpVersion == HttpVersion.HTTPS_1_1;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return requestUrl;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String getLocalName() {
        return serverName;
    }

    @Override
    public String getLocalAddr() {
        return serverAddress;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    public void setInputStream(RequestStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setHeaderMap(Map<HttpHeaderName, String> headerMap) {
        this.headerMap = headerMap;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHostName = remoteHost;
    }

    public void addLocale(double quality, Locale locale) {
        this.locales.put(quality, locale);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public List<Cookie> getCookiesList() {
        return cookies;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Map<HttpHeaderName, String> getHeaderMap() {
        if (headerMap == null) {
            headerMap = new HashMap<>();
        }
        return headerMap;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
        if (queryString == null) {
            return;
        }
        String[] params = queryString.split("&");
        for (String paramKeyValue : params) {
            String[] param = paramKeyValue.split("=");
            if (param.length == 2) {
                parameters.put(param[0], param[1]);
            } else {
                parameters.put(param[0], null);
            }
        }
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    //for tests
    Map<Double, Locale> getLocaleMap() {
        return locales;
    }

}
