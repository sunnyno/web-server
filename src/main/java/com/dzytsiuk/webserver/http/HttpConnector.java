package com.dzytsiuk.webserver.http;

import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.processor.HttpProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class HttpConnector {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Value("${server.port}")
    private int port;

    @PostConstruct
    public void connect() {
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            log.info("Server is listening on port " + port);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    HttpProcessor httpProcessor = getHttpProcessor();
                    httpProcessor.setSocket(socket);
                    executorService.submit(httpProcessor);
                } catch (Exception e) {
                    throw new HttpException(StandardHttpStatus.INTERNAL_SERVER_ERROR, e);
                }
            }
        } catch (IOException e) {
            throw new HttpException(StandardHttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public abstract HttpProcessor getHttpProcessor();
}
