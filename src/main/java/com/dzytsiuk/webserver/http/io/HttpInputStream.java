package com.dzytsiuk.webserver.http.io;

import java.io.IOException;
import java.io.InputStream;

public class HttpInputStream extends InputStream {
    private static final int CR = 0x0D;
    private static final int LF = 0x0A;
    private static final int DEFAULT_BUFFER_SIZE = 256;
    private InputStream inputStream;
    private boolean isLineEnd = false;

    public HttpInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        if (read == CR) {
            inputStream.mark(1);
            if (inputStream.read() == LF) {
                isLineEnd = true;
                return -1;
            } else {
                inputStream.reset();
            }
        }
        return read;
    }


    public String readLine() throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        StringBuilder sb = new StringBuilder();
        while (!isLineEnd) {
            int readCount = read(buffer);
            if (readCount == -1) {
                break;
            }
            byte[] readBytes = new byte[readCount];
            System.arraycopy(buffer, 0, readBytes, 0, readCount);
            sb.append(new String(readBytes));
        }
        isLineEnd = false;
        return sb.toString();
    }
}
