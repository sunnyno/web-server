package com.dzytsiuk.webserver.http

import com.dzytsiuk.webserver.http.entity.HttpHeaderName
import com.dzytsiuk.webserver.http.entity.HttpRequest
import org.junit.Test

import static org.junit.Assert.*

class HttpRequestTest {
    @Test
    void getDateHeader() {
        def httpRequest = new HttpRequest()
        def headerMap = [(HttpHeaderName.DATE): "Sun, 10 Oct 2010 23:26:07 GMT"] as HashMap<HttpHeaderName, String>
        httpRequest.setHeaderMap(headerMap)
        def expectedDate = 1286753167000
        def actualDate = httpRequest.getDateHeader(HttpHeaderName.DATE.getHeaderName())
        assertEquals(expectedDate, actualDate)
    }
}
