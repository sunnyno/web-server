package com.dzytsiuk.webserver.http.processor;

import com.dzytsiuk.webserver.http.entity.HttpResponse;
import com.dzytsiuk.webserver.http.entity.*;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.StringJoiner;

public class HttpResponseWriter {
    private static final String CRLF = "" + (char) 0x0D + (char) 0x0A;
    private static final String NAME_VALUE_SEPARATOR = ": ";
    private HttpResponse httpResponse;
    private OutputStream outputStream;
    private boolean isHeaderWritten = false;
    private boolean isChunked = false;


    public HttpResponseWriter(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
        this.outputStream = httpResponse.getSocketOutputStream();
    }

    public boolean isHeaderWritten() {
        return isHeaderWritten;
    }

    public void writeResponse(byte[] array) throws IOException {
        if (!isHeaderWritten) {
            httpResponse.setContentLength(array.length);
            writeHeader();
        }
        if (isChunked) {
            outputStream.write((array.length + CRLF).getBytes());
        }

        outputStream.write(array);

        if (isChunked) {
            outputStream.write(CRLF.getBytes());
        }
    }

    public void writeLastChunk() throws IOException {
        outputStream.write((0 + CRLF + CRLF).getBytes());
        outputStream.flush();
    }

    public void setChunked(boolean isChunked) {
        this.isChunked = isChunked;
    }

    public boolean isChunked() {
        return isChunked;
    }

    public void writeExceptionResponse(Throwable e) throws IOException {
        StringJoiner sj = new StringJoiner(CRLF + "at ");
        sj.add(e.toString());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            sj.add(stackTraceElement.toString());
        }
        String exceptionString = sj.toString();
        httpResponse.setContentLength(exceptionString.length());
        writeHeader();
        writeResponse(exceptionString.getBytes());
    }

    public void writeErrorResponse(String message) throws IOException {
        httpResponse.setContentLength(message.length());
        if (!isHeaderWritten) {
            writeHeader();
        }
        writeResponse(message.getBytes());
    }

    void writeHeader() throws IOException {
        HttpVersion httpVersion = httpResponse.getHttpVersion();
        outputStream.write((httpVersion.getName() + " ").getBytes());
        HttpStatus status = httpResponse.getHttpStatus();
        outputStream.write(String.valueOf(status.getCode()).getBytes());
        String message = status.getMessage();
        if (message != null) {
            outputStream.write((" " + message).getBytes());
        }
        outputStream.write(CRLF.getBytes());
        if (isChunked) {
            outputStream.write((HttpHeaderName.TRANSFER_ENCODING_CHUNKED.getHeaderName() + CRLF).getBytes());
        } else {
            outputStream.write((HttpHeaderName.CONTENT_LENGTH.getHeaderName() + NAME_VALUE_SEPARATOR + (httpResponse.getContentLength()) + CRLF).getBytes());
        }
        for (HttpHeader httpHeader : httpResponse.getHeaders()) {
            outputStream.write((httpHeader.getName() + NAME_VALUE_SEPARATOR + (httpHeader.getValue() + CRLF)).getBytes());
        }

        String cookieString = getCookieString();
        if (cookieString != null) {
            outputStream.write(cookieString.getBytes());
        }
        outputStream.write(CRLF.getBytes());
        isHeaderWritten = true;
    }

    String getCookieString() {
        List<Cookie> cookies = httpResponse.getCookies();
        if (cookies == null) {
            return null;
        }
        StringBuilder cookieHeader = new StringBuilder(HttpHeaderName.COOKIE.getHeaderName() + ": ");
        for (Cookie cookie : cookies) {
            cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
        }
        cookieHeader.append(CRLF);
        return cookieHeader.toString();
    }
}

