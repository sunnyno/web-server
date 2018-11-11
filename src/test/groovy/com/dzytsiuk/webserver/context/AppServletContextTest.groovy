package com.dzytsiuk.webserver.context

import com.dzytsiuk.webserver.app.entity.WebAppServlet
import org.junit.Test
import org.mockito.Mockito
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
}
