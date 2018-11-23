package com.dzytsiuk.webserver.app.parser

import com.dzytsiuk.webserver.app.ApplicationBuilder
import com.dzytsiuk.webserver.app.entity.WebAppServlet
import org.junit.Test
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Paths

import static org.junit.Assert.assertTrue
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class WebXmlParserTest {
    @Test
    void getServletsFromWebXml() {
        def expectedServlets = [new WebAppServlet(name: 'TestServlet', className: 'com.dzytsiuk.servertest.TestServlet', uriPattern: '/test', loadOnStartup: 0),
                                new WebAppServlet(name: 'HelloServlet', className: 'com.dzytsiuk.servertest.HelloServlet', uriPattern: '/hello', loadOnStartup: 1)]
        def applicationBuilder = mock(ApplicationBuilder.class)
        when(applicationBuilder.buildApp(any(List.class), anyString())).then(new Answer<String>() {
            @Override
            String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments()
                def actualServlets = args[0] as List<WebAppServlet>
                expectedServlets.each { assertTrue(actualServlets.remove(it)) }
                return (String) args[0]
            }
        })

        WebXmlParser webXmlParser = new WebXmlParser()
        webXmlParser.setApplicationBuilder(applicationBuilder)
        webXmlParser.createServletsFromWebXml(Paths.get(ClassLoader.getSystemResource("web.xml").toURI()), "test-app/")

    }
}
