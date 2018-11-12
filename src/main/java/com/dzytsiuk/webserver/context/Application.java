package com.dzytsiuk.webserver.context;

import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.HttpRequest;
import com.dzytsiuk.webserver.http.HttpResponse;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.io.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

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

    public void process(HttpRequest httpRequest, HttpResponse httpResponse) throws ServletException, IOException {
        try (PrintWriter writer = httpResponse.getWriter();
             BufferedReader reader = httpRequest.getReader()) {
            httpRequest.setSessionManager(sessionManager);
            HttpServlet httpServlet = appServletContext.getHttpServletByUrlPattern(httpRequest.getRequestURI());
            ResponseStream responseOutputStream = (ResponseStream) httpResponse.getOutputStream();
            if (httpServlet == null) {
                httpResponse.setStatus(StandardHttpStatus.NOT_FOUND.getCode());
                HttpException httpException = new HttpException("URL " + httpRequest.getRequestURI() + " is not mapped");
                responseOutputStream.writeException(httpException);
            } else {
                log.info("Passing request to servlet {}", httpServlet);
                httpRequest.setServletPath(appServletContext.getHttpServletPath(httpServlet));
                try {
                    httpServlet.init();
                    httpServlet.service(httpRequest, httpResponse);
                    httpServlet.destroy();
                } catch (Throwable e) {
                    httpResponse.setStatus(StandardHttpStatus.INTERNAL_SERVER_ERROR.getCode());
                    responseOutputStream.writeException(e);
                }
            }
        }
    }
}
