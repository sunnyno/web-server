package com.dzytsiuk.webserver.http.processor

import com.dzytsiuk.webserver.http.entity.HttpResponse
import com.dzytsiuk.webserver.http.entity.HttpVersion
import org.junit.Test

import javax.servlet.http.Cookie

import static org.junit.Assert.*

class HttpResponseWriterTest {
    @Test
    void getCookieString() {
        def cookies = [new Cookie("testCookie", "cookie"), new Cookie("user", "testUser")]
        HttpResponse httpResponse = new HttpResponse()
        httpResponse.setCookies(cookies)
        HttpResponseWriter httpResponseWriter = new HttpResponseWriter(httpResponse)
        assertEquals("Cookie: testCookie=cookie; user=testUser;", httpResponseWriter.getCookieString().trim())
    }

    @Test
    void writeHeader() {
        HttpResponse httpResponse = new HttpResponse()
        httpResponse.setHttpVersion(HttpVersion.HTTP_1_1)
        httpResponse.setStatus(200)
        httpResponse.setContentLength(10)
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(out, 64)
        httpResponse.setSocketOutputStream(out)
        HttpResponseWriter httpResponseWriter = new HttpResponseWriter(httpResponse)
        httpResponseWriter.writeHeader()
        def bytes = new byte[64]
        def read = inputStream.read(bytes)
        assertEquals("HTTP/1.1 200\r\nContent-Length: 10\r\n\r\n", new String(bytes, 0, read))
    }

    @Test
    void writeErrorResponse() {
        HttpResponse httpResponse = new HttpResponse()
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(out, 64)
        httpResponse.setSocketOutputStream(out)
        HttpResponseWriter httpResponseWriter = new HttpResponseWriter(httpResponse)
        httpResponseWriter.isHeaderWritten = true
        httpResponseWriter.writeErrorResponse("Error")
        def bytes = new byte[64]
        def read = inputStream.read(bytes)
        assertEquals("Error", new String(bytes, 0, read))
    }

}
