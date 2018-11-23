package com.dzytsiuk.webserver.app.scanner;

import com.dzytsiuk.webserver.exception.AppInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.util.zip.ZipFile;

public class WebAppScanner {
    private static final String WEB_APP_FOLDER_NAME = "webapps";
    private static final String WAR_EXTENSION = ".war";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WarManager warManager = new WarManager();

    public void scan() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Paths.get(WEB_APP_FOLDER_NAME).register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                    log.debug("New file loaded to {} folder", WEB_APP_FOLDER_NAME);
                    processEntryCreateEvent(watchKey);
                } catch (InterruptedException e) {
                    watchService.close();
                    log.info("Web app scanner is shut down");
                    break;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error occurred during " + WEB_APP_FOLDER_NAME + " scanning", e);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void processEntryCreateEvent(WatchKey watchKey) {
        for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
            WatchEvent.Kind<?> kind = watchEvent.kind();
            if (kind == StandardWatchEventKinds.OVERFLOW) {
                continue;
            }
            WatchEvent<Path> ev = (WatchEvent<Path>) watchEvent;
            String filename = ev.context().toString();
            if (filename.endsWith(WAR_EXTENSION)) {
                log.debug("Start constructing app from file {}", filename);
                File file = new File(WEB_APP_FOLDER_NAME + File.separator + filename);
                handleWar(file);
                log.debug("Finish constructing app from file {}", filename);
            }
            if (!watchKey.reset()) {
                break;
            }

        }
    }

    public void initialScan() {
        log.debug("Start scanning webapps for new war files");
        File webAppFolder = new File(WEB_APP_FOLDER_NAME);
        File[] files = webAppFolder.listFiles((dir, name) -> name.endsWith(WAR_EXTENSION));
        if (files != null) {
            for (File file : files) {
                handleWar(file);
            }
        }
        log.debug("Finish scanning webapps for new war files");
    }

    private void handleWar(File file) {
        String fileName = file.getName();
        log.info("New war file found {}", fileName);
        try (ZipFile zipFile = new ZipFile(file)) {
            warManager.process(zipFile);
        } catch (Exception e) {
            throw new AppInstantiationException("Error instantiating application from file " + fileName, e);
        }
    }
}
