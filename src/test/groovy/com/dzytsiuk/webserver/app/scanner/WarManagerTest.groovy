package com.dzytsiuk.webserver.app.scanner

import org.junit.Test

import java.util.zip.ZipFile

import static org.junit.Assert.assertEquals

class WarManagerTest {
    @Test
    void unpackWar() {
        def manager = new WarManager()
        def file = new ZipFile(new File("webapps/web-server-test-v3.war"))
        assertEquals("webapps/web-server-test-v3/", manager.unpackWar(file))
    }
}
