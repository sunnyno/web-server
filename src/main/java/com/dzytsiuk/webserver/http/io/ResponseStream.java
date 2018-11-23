package com.dzytsiuk.webserver.http.io;

import com.dzytsiuk.webserver.http.processor.HttpResponseWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ResponseStream extends ServletOutputStream {
    private static final int DEFAULT_BUFFER_CAPACITY = 1024;
    private int bufferCapacity = DEFAULT_BUFFER_CAPACITY;
    private HttpResponseWriter httpResponseWriter;
    private ByteBuffer byteBuffer;

    public ResponseStream(HttpResponseWriter httpResponseWriter) {
        this.httpResponseWriter = httpResponseWriter;
    }

    public HttpResponseWriter getHttpResponseWriter() {
        return httpResponseWriter;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    @Override
    public void write(int b) throws IOException {
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(bufferCapacity);
        }
        if (!byteBuffer.hasRemaining()) {
            httpResponseWriter.setChunked(true);
            flush();
        }
        byteBuffer.put((byte) b);
    }

    @Override
    public void flush() throws IOException {
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(bufferCapacity);
        }
        byte[] a = new byte[byteBuffer.position()];
        byteBuffer.rewind();
        byteBuffer.get(a);
        httpResponseWriter.writeResponse(a);
        byteBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        flush();
        if (httpResponseWriter.isChunked()) {
            httpResponseWriter.writeLastChunk();
        }
    }

    public void writeException(Throwable e) throws IOException {
        httpResponseWriter.writeExceptionResponse(e);
    }

    public boolean isCommited() {
        return httpResponseWriter.isHeaderWritten();
    }

    public void reset() {
        byteBuffer.clear();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
