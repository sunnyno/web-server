package com.dzytsiuk.webserver.context;

import com.dzytsiuk.webserver.app.entity.WebAppFilter;
import com.dzytsiuk.webserver.app.entity.WebAppFilterChain;
import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import com.dzytsiuk.webserver.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppServletContext implements ServletContext {
    private static final String WEB_SERVER_INFO = "Web Server v1";
    private static final int SESSION_DEFAULT_TIMEOUT = Integer.parseInt(AppUtil.getApplicationProperty("session.default.timeout"));
    private static final String GENERAL_URL_PATTERN = "/*";
    private static final String DEFAULT_URL_PATTERN = "/";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private List<WebAppFilter> webAppFilters;
    private Map<String, Filter> httpFilters = new HashMap<>();
    private WebAppFilter generalWebAppFilter;
    private WebAppFilter defaultWebAppFilter;
    private List<WebAppServlet> webAppServlets;
    private WebAppServlet generalWebAppServlet;
    private WebAppServlet defaultWebAppServlet;
    private Map<String, HttpServlet> httpServlets = new HashMap<>();
    private final URLClassLoader classLoader;
    private String contextPath;
    private Map<String, Object> attributes = new HashMap<>();
    private Map<String, String> initParameters = new HashMap<>();

    public AppServletContext(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        if (contextPath.contains(uripath)) {
            return this;
        }
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 4;
    }

    @Override
    public int getMinorVersion() {
        return 2;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 4;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 2;
    }

    @Override
    public String getMimeType(String file) {
        URL resource = classLoader.findResource(file);
        if (resource == null) {
            return null;
        } else {
            try {
                return Files.probeContentType(Paths.get(resource.toURI()));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Error getting MIME type of resource");
            }
        }
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        URL[] urLs = classLoader.getURLs();
        Set<String> requestedPath = new HashSet<>();
        List<String> contextPathUrlList = getContextPathUrlList(urLs);
        if ("/".equals(path)) {
            for (String contextPathUrl : contextPathUrlList) {
                requestedPath.add(contextPathUrl.split("/")[0]);
            }
        } else {
            for (String contextPathUrl : contextPathUrlList) {
                if (contextPathUrl.startsWith(path)) {
                    requestedPath.add(contextPathUrl);
                }
            }
        }
        return requestedPath;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return classLoader.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return classLoader.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return null;
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return httpServlets.get(name);
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        List<Servlet> servlets = new ArrayList<>(httpServlets.values());
        return Collections.enumeration(servlets);
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(httpServlets.keySet());
    }

    @Override
    public void log(String msg) {
        log.info(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
        log(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        StringJoiner sj = new StringJoiner("at ");
        sj.add(message);
        for (StackTraceElement stackTraceElement : stackTrace) {
            sj.add(stackTraceElement.toString());
        }
        log.error(sj.toString());
    }

    @Override
    public String getRealPath(String path) {
        URL[] urLs = classLoader.getURLs();
        for (URL urL : urLs) {
            String urlString = urL.toString();
            if (urlString.contains(path)) {
                return urlString;
            }
        }
        return null;
    }

    @Override
    public String getServerInfo() {
        return WEB_SERVER_INFO;
    }

    @Override
    public String getInitParameter(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return initParameters.put(name, value) != null;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return contextPath;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException("Dynamic Servlet registration is not supported");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException("Dynamic Filter registration is not supported");
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    @Override
    public void addListener(String className) {

    }

    @Override
    public <T extends EventListener> void addListener(T t) {

    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {

    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void declareRoles(String... roleNames) {

    }

    @Override
    public String getVirtualServerName() {
        return WEB_SERVER_INFO;
    }

    @Override
    public int getSessionTimeout() {
        return SESSION_DEFAULT_TIMEOUT;
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        throw new IllegalStateException("Servlet Context already initialized");
    }

    @Override
    public String getRequestCharacterEncoding() {
        return null;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {

    }

    @Override
    public String getResponseCharacterEncoding() {
        return null;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {

    }

    public void setWebAppFilters(List<WebAppFilter> webAppFilters) {
        for (WebAppFilter webAppFilter : webAppFilters) {
            if (GENERAL_URL_PATTERN.equals(webAppFilter.getUriPattern())) {
                generalWebAppFilter = webAppFilter;
            } else if (DEFAULT_URL_PATTERN.equals(webAppFilter.getUriPattern())) {
                defaultWebAppFilter = webAppFilter;
            }
        }
        webAppFilters.remove(generalWebAppFilter);
        this.webAppFilters = webAppFilters;
    }

    public void setWebAppServlets(List<WebAppServlet> webAppServlets) {
        for (WebAppServlet webAppServlet : webAppServlets) {
            if (GENERAL_URL_PATTERN.equals(webAppServlet.getUriPattern())) {
                generalWebAppServlet = webAppServlet;
            } else if (DEFAULT_URL_PATTERN.equals(webAppServlet.getUriPattern())) {
                defaultWebAppServlet = webAppServlet;
            }
        }
        webAppServlets.remove(generalWebAppServlet);
        webAppServlets.remove(defaultWebAppServlet);
        this.webAppServlets = webAppServlets;
    }

    public void loadServletsOnStartUp() {
        log.info("Start loading onStartup servlets");
        List<WebAppServlet> loadOnStartUpServlets = webAppServlets.stream()
                .filter(webAppServlet -> webAppServlet.getLoadOnStartup() > 0)
                .sorted(Comparator.comparingInt(WebAppServlet::getLoadOnStartup))
                .collect(Collectors.toList());
        for (WebAppServlet servlet : loadOnStartUpServlets) {
            loadServlet(servlet);
        }
        log.info("Finish loading onStartup servlets");
    }

    HttpServlet getHttpServletByUrlPattern(String requestURI) {
        WebAppServlet webAppServlet = getWebAppServletByUrlPattern(requestURI);
        log.info("Servlet for uri {} is {}", requestURI, webAppServlet);
        return webAppServlet != null ? getServletInstance(webAppServlet) : null;
    }

    FilterChain getFilterChainByUrlPattern(String requestURI) {
        List<WebAppFilter> webAppFilters = getWebAppFiltersByUrlPattern(requestURI);
        ArrayDeque<Filter> filters = new ArrayDeque<>();
        for (WebAppFilter appFilter : webAppFilters) {
            filters.addLast(getFilterInstance(appFilter));
        }
        return new WebAppFilterChain(filters);
    }

    String getHttpServletPath(HttpServlet httpServlet) {
        for (Map.Entry<String, HttpServlet> servletEntry : httpServlets.entrySet()) {
            if (servletEntry.getValue() == httpServlet) {
                String servletName = servletEntry.getKey();
                Optional<WebAppServlet> appServlet = webAppServlets.stream().filter(webAppServlet -> servletName.equalsIgnoreCase(webAppServlet.getName())).findFirst();
                return appServlet.map(WebAppServlet::getUriPattern).orElse(null);
            }
        }
        return null;
    }

    WebAppServlet getWebAppServletByUrlPattern(String requestURI) {
        if (generalWebAppServlet != null) {
            return generalWebAppServlet;
        }

        //lookup for wildcard uri servlet
        WebAppServlet appServlet = webAppServlets.stream()
                .filter(webAppServlet -> isUriMatched(webAppServlet.getUriPattern(), requestURI))
                .findFirst().orElse(null);
        if (appServlet != null) {
            return appServlet;
        }

        //lookup for equal uri servlet
        appServlet = webAppServlets.stream()
                .filter(webAppServlet -> requestURI.equals(webAppServlet.getUriPattern()))
                .findFirst().orElse(null);
        if (appServlet != null) {
            return appServlet;
        }

        //lookup for default servlet
        //return null if nothing found
        return defaultWebAppServlet;
    }

    private HttpServlet loadServlet(WebAppServlet servlet) {
        try {
            String servletClassName = servlet.getClassName();
            HttpServlet servletInstance = (HttpServlet) classLoader.loadClass(servletClassName).newInstance();
            servletInstance.init();
            httpServlets.put(servlet.getName(), servletInstance);
            log.info("Object of class {} created", servletClassName);
            return servletInstance;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ServletException e) {
            throw new RuntimeException("Error instantiating servlet ", e);
        }
    }

    private List<WebAppFilter> getWebAppFiltersByUrlPattern(String requestURI) {
        List<WebAppFilter> filtersForRequest = new ArrayList<>();
        //lookup for equal uri servlet
        WebAppFilter webAppFilter = webAppFilters.stream()
                .filter(filter -> requestURI.equals(filter.getUriPattern()))
                .findFirst().orElse(null);
        if (webAppFilter != null) {
            filtersForRequest.add(webAppFilter);
        }

        //lookup for wildcard uri servlet
        webAppFilter = webAppFilters.stream()
                .filter(filter -> isUriMatched(filter.getUriPattern(), requestURI))
                .findFirst().orElse(null);
        if (webAppFilter != null) {
            filtersForRequest.add(webAppFilter);
        }

        //lookup for general filter
        if (generalWebAppFilter != null) {
            filtersForRequest.add(generalWebAppFilter);
        }

        //lookup for default filter
        if (defaultWebAppFilter != null) {
            filtersForRequest.add(defaultWebAppFilter);
        }
        return filtersForRequest;
    }


    private Filter loadFilter(WebAppFilter filter) {
        try {
            String filterClassName = filter.getClassName();
            Filter filterInstance = (Filter) classLoader.loadClass(filterClassName).newInstance();
            filterInstance.init(null);
            httpFilters.put(filter.getName(), filterInstance);
            log.info("Object of class {} created", filterClassName);
            return filterInstance;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | ServletException e) {
            throw new RuntimeException("Error instantiating filter ", e);
        }
    }

    private boolean isUriMatched(String urlPattern, String requestURI) {
        if (urlPattern.contains("*")) {
            String regexPattern = urlPattern.replace("*", ".*");
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(requestURI);
            return matcher.find();
        } else {
            return false;
        }
    }

    private HttpServlet getServletInstance(WebAppServlet webAppServlet) {
        HttpServlet httpServlet = httpServlets.get(webAppServlet.getName());
        if (httpServlet == null) {
            httpServlet = loadServlet(webAppServlet);
        }
        return httpServlet;
    }

    Filter getFilterInstance(WebAppFilter webAppFilter) {
        Filter filter = httpFilters.get(webAppFilter.getName());
        if (filter == null) {
            filter = loadFilter(webAppFilter);
        }
        return filter;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    private List<String> getContextPathUrlList(URL[] urLs) {
        List<String> contextPathUrlList = new ArrayList<>();
        for (URL url : urLs) {
            String urlString = url.toString();
            Pattern pattern = Pattern.compile("(?<=" + contextPath + "\\/)[\\w+.-]*");
            Matcher matcher = pattern.matcher(urlString);
            if (matcher.find()) {
                String contextPathUrl = matcher.group(0);
                contextPathUrlList.add(contextPathUrl);
            }
        }
        return contextPathUrlList;
    }

    void shutDown() {
        log.info("Destroying servlets");
        for (HttpServlet httpServlet : httpServlets.values()) {
            httpServlet.destroy();
        }
        log.info("Destroying filters");
        for (Filter filter : httpFilters.values()) {
            filter.destroy();
        }
    }
}
