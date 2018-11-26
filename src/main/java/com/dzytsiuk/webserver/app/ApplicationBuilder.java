package com.dzytsiuk.webserver.app;

import com.dzytsiuk.webserver.app.entity.WebAppFilter;
import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import com.dzytsiuk.webserver.context.AppServletContext;
import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.context.ApplicationContainer;
import com.dzytsiuk.webserver.exception.AppInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ApplicationBuilder {
    private static final String CLASSES_FOLDER_NAME = "classes";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationContainer applicationContainer = ApplicationContainer.getInstance();

    public void buildApp(List<WebAppServlet> webAppServlets, List<WebAppFilter> webAppFilters, String warFolder) {
        //get classloader
        URLClassLoader webAppClassLoader = getClassLoader(warFolder);
        //get servlet context
        log.info("Constructing servlet context");
        AppServletContext appServletContext = new AppServletContext(webAppClassLoader);
        appServletContext.setWebAppServlets(webAppServlets);
        appServletContext.setWebAppFilters(webAppFilters);

        //create app
        String appName = warFolder.split(File.separator)[1];
        log.info("Application {} is created", appName);
        appServletContext.setContextPath(appName);
        Application application = new Application(appName, appServletContext);
        applicationContainer.registerApp(application);
        application.getAppServletContext().loadServletsOnStartUp();
    }

    private URLClassLoader getClassLoader(String warFolder) {
        log.info("Injecting URLs into classloader");
        try {
            URL[] urls = Files.walk(Paths.get(warFolder))
                    .map(this::getUrl)
                    .filter(Objects::nonNull)
                    .toArray(URL[]::new);
            log.info("Class loader urls are {}", Arrays.toString(urls));
            return new URLClassLoader(urls);
        } catch (IOException e) {
            throw new RuntimeException("Error creating Class Loader ", e);
        }
    }

    private URL getUrl(Path path) {
        String fileName = path.getFileName().toString();
        try {
            if (fileName.equals(CLASSES_FOLDER_NAME) || fileName.endsWith(JAR_FILE_EXTENSION)) {
                return path.toFile().toURI().toURL();
            }
            return null;
        } catch (IOException e) {
            throw new AppInstantiationException("Error forming url", e);
        }
    }

    ApplicationContainer getApplicationContainer() {
        return applicationContainer;
    }
}
