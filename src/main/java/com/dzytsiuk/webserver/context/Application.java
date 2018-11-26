package com.dzytsiuk.webserver.context;

import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.entity.HttpRequest;
import com.dzytsiuk.webserver.http.entity.HttpResponse;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.io.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;

public class Application {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String name;
    private final AppServletContext appServletContext;
    private SessionManager sessionManager;

    public Application(String name, AppServletContext appServletContext) {
        this.name = name;
        this.appServletContext = appServletContext;
        sessionManager = new SessionManager(appServletContext);
    }

    public String getName() {
        return name;
    }

    public AppServletContext getAppServletContext() {
        return appServletContext;
    }

    public void process(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        Thread.currentThread().setContextClassLoader(appServletContext.getClassLoader());
        httpRequest.setSessionManager(sessionManager);
        String requestURI = httpRequest.getRequestURI();
        HttpServlet httpServlet = appServletContext.getHttpServletByUrlPattern(requestURI);
        FilterChain filters = appServletContext.getFilterChainByUrlPattern(requestURI);
        ResponseStream responseOutputStream = (ResponseStream) httpResponse.getOutputStream();
        if (httpServlet == null) {
            httpResponse.setStatus(StandardHttpStatus.NOT_FOUND.getCode());
            HttpException httpException = new HttpException("URL " + requestURI + " is not mapped");
            responseOutputStream.writeException(httpException);
        } else {
            log.info("Passing request to servlet {}", httpServlet);
            httpRequest.setServletPath(appServletContext.getHttpServletPath(httpServlet));
            try {
                filters.doFilter(httpRequest, httpResponse);
                httpServlet.service(httpRequest, httpResponse);
            } catch (Throwable e) {
                log.error("Error processing request", e);
                httpResponse.setStatus(StandardHttpStatus.INTERNAL_SERVER_ERROR.getCode());
                responseOutputStream.writeException(e);
            }
        }
    }
}
