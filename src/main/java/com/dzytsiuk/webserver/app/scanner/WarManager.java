package com.dzytsiuk.webserver.app.scanner;

import com.dzytsiuk.webserver.app.entity.WebAppServlet;
import com.dzytsiuk.webserver.app.parser.WebXmlParser;
import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.context.AppServletContext;
import com.dzytsiuk.webserver.exception.AppInstantiationException;
import com.dzytsiuk.webserver.exception.NoWebXmlFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class WarManager {
    private static final String WEB_XML_FILE_NAME = "web.xml";
    private static final String CLASSES_FOLDER_NAME = "classes";
    private static final String JAR_FILE_EXTENSION = ".jar";
    private static final String WAR_FILE_EXTENSION = ".war";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private FileSystem fileSystem = FileSystems.getDefault();

    private final WebXmlParser webXmlParser;

    @Autowired
    public WarManager(WebXmlParser webXmlParser) {
        this.webXmlParser = webXmlParser;
    }

    public Application createApp(ZipFile zipFile) throws Exception {
        log.info("Unpacking file {}", zipFile.getName());

        //unpack war
        String warFolder = unpackWar(zipFile);

        //find web.xml
        Optional<Path> webXml = Files.find(Paths.get(warFolder), 3,
                (path, attr) -> path.getFileName().toString().equals(WEB_XML_FILE_NAME))
                .findAny();

        //parse web.xml
        List<WebAppServlet> webAppServlets = webXml.map(webXmlParser::getServletsFromWebXml)
                .orElseThrow(() -> new NoWebXmlFoundException("No web.xml found"));

        //get classloader
        URLClassLoader webAppClassLoader = getClassLoader(warFolder);
        log.info("Constructing servlet context");

        //get servlet context
        AppServletContext appServletContext = new AppServletContext(webAppClassLoader);
        appServletContext.setWebAppServlets(webAppServlets);

        //create app
        String appName = warFolder.split(File.separator)[1];
        log.info("Application {} is created", appName);
        appServletContext.setContextPath(appName);
        return new Application(appName, appServletContext);
    }

    private URLClassLoader getClassLoader(String warFolder) throws IOException {
        log.info("Injecting URLs into classloader");
        URL[] urls = Files.walk(Paths.get(warFolder))
                .map(this::getUrl)
                .filter(Objects::nonNull)
                .toArray(URL[]::new);
        log.info("Class loader urls are {}", Arrays.toString(urls));
        return new URLClassLoader(urls);
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

    private String unpackWar(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        String fileName = zipFile.getName();
        String appFolder = fileName.replace(WAR_FILE_EXTENSION, "") + File.separator;
        Path appPath = fileSystem.getPath(appFolder);
        try {
            Files.createDirectory(appPath);
        } catch (FileAlreadyExistsException e) {
            FileSystemUtils.deleteRecursively(appPath);
            Files.createDirectory(appPath);
        }
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            //If directory then create a new directory in webapp folder
            String entryName = entry.getName();
            if (entry.isDirectory()) {
                log.info("Creating Directory:" + appFolder + entryName);
                Files.createDirectories(fileSystem.getPath(appFolder + entryName));
            }
            //Else create the file
            else {
                try (InputStream is = zipFile.getInputStream(entry);
                     BufferedInputStream bis = new BufferedInputStream(is)) {
                    String uncompressedFileName = appFolder + entryName;
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read());
                    }
                }
                log.info("Written :" + entryName);
            }
        }
        return appFolder;
    }
}
