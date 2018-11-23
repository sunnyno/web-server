package com.dzytsiuk.webserver.http.processor;

import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.context.ApplicationContainer;
import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.HttpRequest;
import com.dzytsiuk.webserver.http.HttpResponse;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.io.ResponseStream;
import com.dzytsiuk.webserver.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class HttpProcessor implements Runnable, Closeable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private int localPort = Integer.parseInt(AppUtil.getApplicationProperty("server.port"));
    private Socket socket;

    private HttpRequestParser httpRequestParser = new HttpRequestParser();
    private ApplicationContainer container = ApplicationContainer.getInstance();
    private HttpResponseHandler httpResponseHandler = new HttpResponseHandler();

    public HttpProcessor(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InetAddress inetAddress = socket.getInetAddress();
            log.info("Client {} connected", inetAddress);
            while (!socket.isClosed()) {
                InputStream inputStream = socket.getInputStream();
                if (inputStream.available() > 0) {
                    process(inputStream);
                }
            }
            log.info("Client {} disconnected", inetAddress);
        } catch (Exception e) {
            throw new HttpException("Error processing request ", e);
        }
    }

    private void process(InputStream inputStream) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        log.info("Extracting http request");
        HttpRequest httpRequest = httpRequestParser.getHttpRequest(inputStream);
        enrichHttpRequest(httpRequest);
        log.info("Forming http response");
        HttpResponse httpResponse = httpResponseHandler.createDefaultResponse(httpRequest, outputStream);
        if (httpResponse == null) {
            log.info("Http response cannot be created");
            return;
        }
        Application application = container.getAppByName(httpRequest.getApplicationName());
        if (application == null) {
            httpResponse.setStatus(StandardHttpStatus.NOT_FOUND.getCode());
            HttpException httpException = new HttpException("Application for request '" + httpRequest.getRequestURL() + "' was not found");
            ((ResponseStream) httpResponse.getOutputStream()).writeException(httpException);
        } else {
            log.info("Sending request to application {}", application.getName());
            try (PrintWriter writer = httpResponse.getWriter();
                 BufferedReader reader = httpRequest.getReader()) {
                application.process(httpRequest, httpResponse);
            }
        }
    }

    private void enrichHttpRequest(HttpRequest httpRequest) {
        InetAddress inetAddress = socket.getInetAddress();
        httpRequest.setServerName(inetAddress.getHostName());
        httpRequest.setRemoteHost(inetAddress.getCanonicalHostName());
        httpRequest.setServerAddress(inetAddress.getHostAddress());
        httpRequest.setLocalPort(localPort);
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing socket.", e);
        }
    }
}
