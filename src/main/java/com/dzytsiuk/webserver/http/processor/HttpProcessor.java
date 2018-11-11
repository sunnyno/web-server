package com.dzytsiuk.webserver.http.processor;

import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.context.ApplicationContainer;
import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.HttpRequest;
import com.dzytsiuk.webserver.http.HttpResponse;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.io.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class HttpProcessor implements Runnable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Value("${server.port}")
    private int localPort;
    private Socket socket;

    private HttpRequestParser httpRequestParser;
    private ApplicationContainer container;
    private HttpResponseHandler httpResponseHandler;

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
            e.printStackTrace();
        }
    }

    private void process(InputStream inputStream) throws IOException, ServletException {
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
            application.process(httpRequest, httpResponse);
        }
    }

    private void enrichHttpRequest(HttpRequest httpRequest) {
        InetAddress inetAddress = socket.getInetAddress();
        httpRequest.setServerName(inetAddress.getHostName());
        httpRequest.setRemoteHost(inetAddress.getCanonicalHostName());
        httpRequest.setServerAddress(inetAddress.getHostAddress());
        httpRequest.setLocalPort(localPort);
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Autowired
    public void setHttpRequestParser(HttpRequestParser httpRequestParser) {
        this.httpRequestParser = httpRequestParser;
    }

    @Autowired
    public void setContainer(ApplicationContainer container) {
        this.container = container;
    }

    @Autowired
    public void setHttpResponseHandler(HttpResponseHandler httpResponseHandler) {
        this.httpResponseHandler = httpResponseHandler;
    }

}
