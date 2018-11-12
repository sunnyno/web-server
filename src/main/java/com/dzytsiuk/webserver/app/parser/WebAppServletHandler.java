package com.dzytsiuk.webserver.app.parser;

import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;

public class WebAppServletHandler extends DefaultHandler {
    private Map<String, WebAppServlet> webAppServlets;
    private boolean isServletName;
    private boolean isServletClass;
    private boolean isServlet;
    private boolean isServletNameMapping;
    private boolean isUrlPattern;
    private boolean isLoadOnStartUp;

    private WebAppServlet currentWebAppServlet;

    WebAppServletHandler(Map<String, WebAppServlet> webAppServlets) {
        this.webAppServlets = webAppServlets;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("servlet")) {
            currentWebAppServlet = new WebAppServlet();
            isServlet = true;
        } else if (qName.equals("servlet-name") && isServlet) {
            isServletName = true;
        } else if (qName.equals("servlet-class")) {
            isServletClass = true;
        } else if (qName.equals("servlet-name") && !isServlet) {
            isServletNameMapping = true;
        } else if (qName.equals("url-pattern")) {
            isUrlPattern = true;
        } else if (qName.equals("load-on-startup")) {
            isLoadOnStartUp = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String tagValue = new String(ch, start, length).trim();
        if (isServletName) {
            currentWebAppServlet.setName(tagValue);
            isServletName = false;
        } else if (isServletClass) {
            currentWebAppServlet.setClassName(tagValue);
            isServletClass = false;
        } else if (isServletNameMapping) {
            currentWebAppServlet = webAppServlets.get(tagValue);
            isServletNameMapping = false;
        } else if (isUrlPattern) {
            currentWebAppServlet.setUriPattern(tagValue);
            isUrlPattern = false;
        } else if (isLoadOnStartUp) {
            currentWebAppServlet.setLoadOnStartup(Integer.parseInt(tagValue));
            isLoadOnStartUp = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("servlet")) {
            isServlet = false;
            webAppServlets.put(currentWebAppServlet.getName(), new WebAppServlet(currentWebAppServlet));
        } else if (qName.equals("servlet-mapping")) {
            webAppServlets.put(currentWebAppServlet.getName(), new WebAppServlet(currentWebAppServlet));
        }
    }
}
