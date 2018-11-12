package com.dzytsiuk.webserver.http.processor;

import com.dzytsiuk.webserver.exception.HttpException;
import com.dzytsiuk.webserver.http.HttpRequest;
import com.dzytsiuk.webserver.http.HttpResponse;
import com.dzytsiuk.webserver.http.entity.StandardHttpStatus;
import com.dzytsiuk.webserver.http.io.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class HttpResponseHandler {

    HttpResponse createDefaultResponse(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setHttpRequest(httpRequest);
        httpResponse.setSocketOutputStream(outputStream);
        httpResponse.setHttpVersion(httpRequest.getHttpVersion());
        httpResponse.setCookies(httpRequest.getCookiesList());
        HttpResponseWriter httpResponseWriter = new HttpResponseWriter(httpResponse);
        httpResponse.setOutputStream(new ResponseStream(httpResponseWriter));
        if (!isMethodAllowed(httpRequest, httpResponse)) {
            return null;
        }
        return httpResponse;
    }

    private boolean isMethodAllowed(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (httpRequest.getMethod() == null) {
            httpResponse.setStatus(StandardHttpStatus.METHOD_NOT_ALLOWED.getCode());
            HttpException httpException = new HttpException("Method not allowed");
            ((ResponseStream) httpResponse.getOutputStream()).writeException(httpException);
            return false;
        }
        return true;
    }
}
