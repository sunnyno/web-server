package com.dzytsiuk.webserver.http.io;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestStream extends ServletInputStream {
    private InputStream inputStream;


    public RequestStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }
}
