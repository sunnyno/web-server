package com.dzytsiuk.webserver.app.scanner;

import com.dzytsiuk.webserver.app.parser.WebXmlParser;
import com.dzytsiuk.webserver.context.AppServletContext;
import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.exception.AppInstantiationException;
import com.dzytsiuk.webserver.exception.NoWebXmlFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WarManager {
    private static final String WEB_XML_FILE_NAME = "web.xml";
    private static final String WAR_FILE_EXTENSION = ".war";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WebXmlParser webXmlParser = new WebXmlParser();

    void process(ZipFile zipFile) throws Exception {
        log.info("Unpacking file {}", zipFile.getName());

        //unpack war
        String warFolder = unpackWar(zipFile);

        //find web.xml
        Optional<Path> webXml = Files.find(Paths.get(warFolder), 3,
                (path, attr) -> path.getFileName().toString().equals(WEB_XML_FILE_NAME))
                .findAny();

        //process web.xml
        webXmlParser.createServletsFromWebXml(webXml.orElseThrow(() -> new NoWebXmlFoundException("No web.xml found")), warFolder);
    }


    String unpackWar(ZipFile zipFile) throws IOException {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        String fileName = zipFile.getName();
        String appFolder = fileName.replace(WAR_FILE_EXTENSION, "") + File.separator;
        Path appPath = Paths.get(appFolder);
        try {
            Files.createDirectory(appPath);
        } catch (FileAlreadyExistsException e) {
            Files.walk(appPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            Files.createDirectory(appPath);
        }
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            //If directory then create a new directory in webapp folder
            String entryName = entry.getName();
            if (entry.isDirectory()) {
                log.info("Creating Directory:" + appFolder + entryName);
                Files.createDirectories(Paths.get(appFolder + entryName));
            }
            //Else create the file
            else {
                String uncompressedFileName = appFolder + entryName;
                Path uncompressedFilePath = Paths.get(uncompressedFileName);
                Files.copy(zipFile.getInputStream(entry), uncompressedFilePath);
                log.info("Written :" + entryName);
            }
        }
        return appFolder;
    }
}
