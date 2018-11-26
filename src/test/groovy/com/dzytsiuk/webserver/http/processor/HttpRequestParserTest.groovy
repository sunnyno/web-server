package com.dzytsiuk.webserver.http.processor

import com.dzytsiuk.webserver.http.entity.HttpRequest
import com.dzytsiuk.webserver.http.entity.HttpHeaderName
import com.dzytsiuk.webserver.http.entity.HttpVersion
import org.junit.Test

import javax.servlet.http.Cookie

import static org.junit.Assert.*

class HttpRequestParserTest {
    @Test
    void setRequestLanguage() {
        def expectedLocales = [1.0: 'uk-ua', 0.7: 'en-us', 0.6: 'en', 0.9: 'uk', 0.8: 'ru'] as HashMap<Double, Locale>
        HttpRequestParser parser = new HttpRequestParser()
        def httpRequest = new HttpRequest()
        parser.setRequestLanguage(httpRequest, "uk-UA,uk;q=0.9,ru;q=0.8,en-US;q=0.7,en;q=0.6")
        def actualLocales = httpRequest.localeMap
        expectedLocales.each { assertEquals(it.value, actualLocales[it.key as Double] as String) }
    }

    @Test
    void getRemainingRequestInputStream() {
        def contentLength = 7
        def bytes = new ByteArrayInputStream(("line\r\nnext line\r\n").getBytes())
        HttpRequestParser httpRequestParser = new HttpRequestParser()
        def stream = httpRequestParser.getRemainingRequestInputStream(bytes, contentLength)
        def readBytes = new byte[contentLength + 1]
        def read = stream.read(readBytes)
        assertEquals(read, contentLength)
    }

    @Test
    void addHeader() {
        HttpRequestParser httpRequestParser = new HttpRequestParser()
        HttpRequest httpRequest = new HttpRequest()
        httpRequestParser.addHeader("Accept-Language: uk-UA", httpRequest)
        assertEquals('uk-UA', httpRequest.getHeader(HttpHeaderName.ACCEPT_LANGUAGE.headerName))
    }

    @Test
    void addCookieHeader() {
        HttpRequestParser httpRequestParser = new HttpRequestParser()
        HttpRequest httpRequest = new HttpRequest()
        httpRequestParser.addHeader("Cookie: testCookie=cookie; user=testUser", httpRequest)
        def expectedCookies = [new Cookie("testCookie", "cookie"), new Cookie("user", "testUser")]
        def actualCookies = httpRequest.cookies
        expectedCookies.each {
            assertNotNull(actualCookies.find { cookie -> cookie.getName() == it.getName() && cookie.getValue() == it.getValue() })
        }
    }

    @Test
    void setRequestLineToHttpRequest() {
        HttpRequestParser httpRequestParser = new HttpRequestParser()
        HttpRequest httpRequest = new HttpRequest()
        httpRequestParser.setRequestLineToHttpRequest("GET /test/HTTP HTTP/1.1", httpRequest)
        assertEquals("GET", httpRequest.getMethod())
        assertEquals("/test/HTTP", httpRequest.getRequestURL().toString())
        assertEquals("/HTTP", httpRequest.getRequestURI())
        assertEquals(HttpVersion.HTTP_1_1, httpRequest.getHttpVersion())
    }
}
