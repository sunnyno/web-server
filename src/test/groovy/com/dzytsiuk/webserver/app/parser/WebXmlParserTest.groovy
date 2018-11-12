package com.dzytsiuk.webserver.app.parser

import com.dzytsiuk.webserver.app.entity.WebAppServlet
import org.junit.Test

import java.nio.file.Paths
import static org.junit.Assert.*

class WebXmlParserTest {
    @Test
    void getServletsFromWebXml() {
        def expectedServlets = [new WebAppServlet(name: 'TestServlet', className: 'com.dzytsiuk.servertest.TestServlet', uriPattern: '/test', loadOnStartup: 0),
                                new WebAppServlet(name: 'HelloServlet', className: 'com.dzytsiuk.servertest.HelloServlet', uriPattern: '/hello', loadOnStartup: 1)]
        WebXmlParser webXmlParser = new WebXmlParser()
        def actualServlets = webXmlParser.getServletsFromWebXml(Paths.get(ClassLoader.getSystemResource("web.xml").toURI()))
        expectedServlets.each { assertTrue(actualServlets.remove(it)) }
    }
}
