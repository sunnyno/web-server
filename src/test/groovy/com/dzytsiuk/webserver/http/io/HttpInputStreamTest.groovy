package com.dzytsiuk.webserver.http.io

import org.junit.Test
import static org.junit.Assert.*

class HttpInputStreamTest {
    @Test
    void readLine() {
        def bytes = new ByteArrayInputStream(("line\r\nnext line\r\n").getBytes())
        HttpInputStream httpInputStream = new HttpInputStream(bytes)
        assertEquals("line", httpInputStream.readLine())
        assertEquals("next line", httpInputStream.readLine())
    }
}
