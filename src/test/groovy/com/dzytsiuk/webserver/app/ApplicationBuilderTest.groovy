package com.dzytsiuk.webserver.app

import com.dzytsiuk.webserver.app.entity.WebAppFilter
import com.dzytsiuk.webserver.app.entity.WebAppServlet
import org.junit.Test

import javax.servlet.Servlet

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class ApplicationBuilderTest {
    @Test
    void buildApp() {
        def servlets = [new WebAppServlet(name: 'ErrorServlet', className: 'com.dzytsiuk.servertest.ErrorServlet',
                uriPattern: '/error', loadOnStartup: 0),
                        new WebAppServlet(name: 'TestServlet', className: 'com.dzytsiuk.servertest.TestServlet',
                                uriPattern: '/test', loadOnStartup: 0),
                        new WebAppServlet(name: 'HelloServlet', className: 'com.dzytsiuk.servertest.HelloServlet',
                                uriPattern: '/hello', loadOnStartup: 1),
                        new WebAppServlet(name: 'RedirectServlet', className: 'com.dzytsiuk.servertest.RedirectServlet',
                                uriPattern: '/redirect', loadOnStartup: 0)]
        def filters = [new WebAppFilter(name: 'HelloFilter', className: 'com.dzytsiuk.servertest.filter.HelloFilter',
                uriPattern: '/hello'), new WebAppFilter(name: 'GeneralFilter', className: 'com.dzytsiuk.servertest.filter.GeneralFilter',
                uriPattern: '/*')]
        def builder = new ApplicationBuilder()
        builder.buildApp(servlets, filters, "webapps/web-server-test-v3/")
        def application = builder.applicationContainer.getAppByName("web-server-test-v3")
        assertNotNull(application)
        def context = application.appServletContext
        assertEquals(servlets, context.webAppServlets)
        assertEquals(filters, context.webAppFilters)
        //load on startup
        def loadOnStartupServlets = Collections.list(context.servlets)
        assertEquals(1, loadOnStartupServlets.size())
        assertEquals("com.dzytsiuk.servertest.HelloServlet", loadOnStartupServlets.get(0).getClass().getName())
    }
}
