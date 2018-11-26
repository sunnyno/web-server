package com.dzytsiuk.webserver.app.parser;

import com.dzytsiuk.webserver.app.entity.WebAppFilter;
import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;

public class WebAppServletHandler extends DefaultHandler {
    private Map<String, WebAppServlet> webAppServlets;
    private Map<String, WebAppFilter> webAppFilters;
    private boolean isServletName;
    private boolean isServletClass;
    private boolean isServlet;
    private boolean isServletMapping;
    private boolean isServletNameMapping;
    private boolean isUrlPattern;
    private boolean isLoadOnStartUp;
    private boolean isFilter;
    private boolean isFilterName;
    private boolean isFilterClass;
    private boolean isFilterMapping;
    private boolean isFilterNameMapping;
    private boolean isFilterUrlPattern;

    private WebAppServlet currentWebAppServlet;
    private WebAppFilter currentWebAppFilter;

    WebAppServletHandler(Map<String, WebAppServlet> webAppServlets, Map<String, WebAppFilter> webAppFiltersMap) {
        this.webAppServlets = webAppServlets;
        this.webAppFilters = webAppFiltersMap;
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
            isServletMapping = true;
            isServletNameMapping = true;
        } else if (qName.equals("url-pattern") && isServletMapping) {
            isUrlPattern = true;
        } else if (qName.equals("load-on-startup")) {
            isLoadOnStartUp = true;
        } else if (qName.equals("filter")) {
            currentWebAppFilter = new WebAppFilter();
            isFilter = true;
        } else if (qName.equals("filter-name") && isFilter) {
            isFilterName = true;
        } else if (qName.equals("filter-class")) {
            isFilterClass = true;
        } else if (qName.equals("filter-mapping")) {
            isFilterMapping = true;
        } else if (qName.equals("filter-name") && !isFilter) {
            isFilterNameMapping = true;
        } else if (qName.equals("url-pattern") && isFilterMapping) {
            isFilterUrlPattern = true;
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
        } else if (isFilterName) {
            currentWebAppFilter.setName(tagValue);
            isFilterName = false;
        } else if (isFilterClass) {
            currentWebAppFilter.setClassName(tagValue);
            isFilterClass = false;
        } else if (isFilterNameMapping) {
            currentWebAppFilter = webAppFilters.get(tagValue);
            isFilterNameMapping = false;
        } else if (isFilterUrlPattern) {
            currentWebAppFilter.setUriPattern(tagValue);
            isFilterUrlPattern = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("servlet")) {
            isServlet = false;
            webAppServlets.put(currentWebAppServlet.getName(), new WebAppServlet(currentWebAppServlet));
        } else if (qName.equals("servlet-mapping")) {
            isServletMapping = false;
            webAppServlets.put(currentWebAppServlet.getName(), new WebAppServlet(currentWebAppServlet));
        } else if (qName.equals("filter")) {
            isFilter = false;
            webAppFilters.put(currentWebAppFilter.getName(), new WebAppFilter(currentWebAppFilter));
        } else if (qName.equals("filter-mapping")) {
            isFilterMapping = false;
            webAppFilters.put(currentWebAppFilter.getName(), new WebAppFilter(currentWebAppFilter));
        }
    }
}
