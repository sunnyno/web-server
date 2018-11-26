package com.dzytsiuk.webserver.context

import com.dzytsiuk.webserver.app.entity.WebAppFilter
import com.dzytsiuk.webserver.app.entity.WebAppFilterChain
import com.dzytsiuk.webserver.app.entity.WebAppServlet
import org.junit.Test
import org.mockito.Mockito
import org.slf4j.LoggerFactory

import javax.servlet.Filter
import java.nio.file.Paths

import static org.junit.Assert.*

class AppServletContextTest {

    @Test
    void getGeneralHttpServletByUriPattern() {

        def servletContext = Mockito.mock(AppServletContext.class)
        Mockito.when(servletContext.getWebAppServletByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppServlets(Mockito.anyListOf(WebAppServlet.class))).thenCallRealMethod()

        WebAppServlet expectedServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/*")
        def servlets = new ArrayList<WebAppServlet>()
        servlets.add(expectedServlet)
        servletContext.setWebAppServlets(servlets)

        def actualServlet = servletContext.getWebAppServletByUrlPattern("/test")
        assertEquals(expectedServlet, actualServlet)
    }

    @Test
    void getDefaultHttpServletByUriPattern() {

        def servletContext = Mockito.mock(AppServletContext.class)
        Mockito.when(servletContext.getWebAppServletByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppServlets(Mockito.anyListOf(WebAppServlet.class))).thenCallRealMethod()

        WebAppServlet expectedServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/")
        def servlets = new ArrayList<WebAppServlet>()
        servlets.add(expectedServlet)
        servletContext.setWebAppServlets(servlets)

        def actualServlet = servletContext.getWebAppServletByUrlPattern("/test")
        assertEquals(expectedServlet, actualServlet)
    }

    @Test
    void getWildcardHttpServletByUriPattern() {

        def servletContext = Mockito.mock(AppServletContext.class)
        Mockito.when(servletContext.getWebAppServletByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppServlets(Mockito.anyListOf(WebAppServlet.class))).thenCallRealMethod()

        WebAppServlet expectedServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/test/*/add")
        WebAppServlet additionalServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/test/1")
        def servlets = new ArrayList<WebAppServlet>()
        servlets.add(expectedServlet)
        servlets.add(additionalServlet)
        servletContext.setWebAppServlets(servlets)

        def actualServlet = servletContext.getWebAppServletByUrlPattern("/test/1/add")
        assertEquals(expectedServlet, actualServlet)
    }

    @Test
    void getEqualsHttpServletByUriPattern() {
        def servletContext = Mockito.mock(AppServletContext.class)
        Mockito.when(servletContext.getWebAppServletByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppServlets(Mockito.anyListOf(WebAppServlet.class))).thenCallRealMethod()

        WebAppServlet expectedServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/test")
        WebAppServlet additionalServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/test/*")
        def servlets = new ArrayList<WebAppServlet>()
        servlets.add(expectedServlet)
        servlets.add(additionalServlet)
        servletContext.setWebAppServlets(servlets)

        def actualServlet = servletContext.getWebAppServletByUrlPattern("/test")
        assertEquals(expectedServlet, actualServlet)
    }


    @Test
    void getNullHttpServletByUriPattern() {

        def servletContext = Mockito.mock(AppServletContext.class)
        Mockito.when(servletContext.getWebAppServletByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppServlets(Mockito.anyListOf(WebAppServlet.class))).thenCallRealMethod()

        WebAppServlet additionalServlet = new WebAppServlet(name: "Test", className: "Test", uriPattern: "/test/*")
        def servlets = new ArrayList<WebAppServlet>()
        servlets.add(additionalServlet)
        servletContext.setWebAppServlets(servlets)

        def actualServlet = servletContext.getWebAppServletByUrlPattern("/invalid")
        assertNull(actualServlet)
    }

    @Test
    void getResourcePaths() {
        def urls = [Paths.get("/testclasses/TestServlet.class").toUri().toURL()] as URL[]
        def servletContext = new AppServletContext(new URLClassLoader(urls))
        servletContext.setContextPath("testclasses")
        def paths = servletContext.getResourcePaths("/")
        assertTrue(paths.contains("TestServlet.class"))
    }

    @Test
    void getRealPath() {
        def urls = [Paths.get("/testclasses/TestServlet.class").toUri().toURL()] as URL[]
        def expectedPath = "file:/testclasses/TestServlet.class"
        def servletContext = new AppServletContext(new URLClassLoader(urls))
        assertEquals(expectedPath, servletContext.getRealPath(/testclasses/))
    }

    @Test
    void getFilterChainByUrlPattern(){
        def servletContext = Mockito.mock(AppServletContext.class)
        def filter = Mockito.mock(Filter.class)
        Mockito.when(servletContext.getFilterChainByUrlPattern(Mockito.anyString())).thenCallRealMethod()
        Mockito.when(servletContext.setWebAppFilters(Mockito.anyListOf(WebAppFilter.class))).thenCallRealMethod()
        Mockito.when(servletContext.getFilterInstance(Mockito.any(WebAppFilter.class))).thenReturn(filter)

        WebAppFilter expectedFilter = new WebAppFilter(name: "Test", className: "Test", uriPattern: "/*")
        WebAppFilter additionalFilter = new WebAppFilter(name: "Test", className: "Test", uriPattern: "/test")
        def filters = new ArrayList<WebAppFilter>()
        filters.add(expectedFilter)
        filters.add(additionalFilter)
        servletContext.setWebAppFilters(filters)

        def actualFilterChain = servletContext.getFilterChainByUrlPattern("/test") as WebAppFilterChain
        assertEquals(2, actualFilterChain.filterQueue.size())
    }
}
