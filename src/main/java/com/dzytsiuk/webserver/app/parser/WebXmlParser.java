package com.dzytsiuk.webserver.app.parser;

import com.dzytsiuk.webserver.app.ApplicationBuilder;
import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebXmlParser {
    private static final SAXParserFactory SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationBuilder applicationBuilder = new ApplicationBuilder();


    public void createServletsFromWebXml(Path file, String warFolder) {
        log.info("Start parsing web.xml");
        Map<String, WebAppServlet> webAppServletMap = new HashMap<>();
        WebAppServletHandler webAppServletHandler = new WebAppServletHandler(webAppServletMap);
        try {
            SAXParser saxParser = SAX_PARSER_FACTORY.newSAXParser();
            saxParser.parse(file.toFile(), webAppServletHandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Error parsing web.xml ", e);
        }
        ArrayList<WebAppServlet> webAppServlets = new ArrayList<>(webAppServletMap.values());
        log.debug("Found servlets: {}", webAppServlets);
        applicationBuilder.buildApp(webAppServlets, warFolder);
    }

    void setApplicationBuilder(ApplicationBuilder applicationBuilder) {
        this.applicationBuilder = applicationBuilder;
    }
}
