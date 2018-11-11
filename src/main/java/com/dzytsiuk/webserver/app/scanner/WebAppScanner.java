package com.dzytsiuk.webserver.app.scanner;

import com.dzytsiuk.webserver.context.Application;
import com.dzytsiuk.webserver.context.ApplicationContainer;
import com.dzytsiuk.webserver.exception.AppInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

@Component
public class WebAppScanner {
    private static final File WEB_APP_FOLDER = new File("webapps");
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WarManager warManager;
    private final ApplicationContainer applicationContainer;
    private final Map<String, Long> scannedWarsLastModifiedMap = new HashMap<>();

    @Value("${scan.webapps.rate}")
    private long scanRate;

    @Autowired
    public WebAppScanner(ApplicationContainer applicationContainer, WarManager warManager) {
        this.applicationContainer = applicationContainer;
        this.warManager = warManager;
    }

    @PostConstruct
    public void init() {
        scheduledExecutorService.scheduleAtFixedRate(this::scanWebAppFolder, 0, scanRate, TimeUnit.MINUTES);
    }

    private void scanWebAppFolder() {
        log.debug("Start scanning webapps for new war files");
        File[] files = WEB_APP_FOLDER.listFiles((dir, name) -> name.endsWith(".war"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                Long lastModified = scannedWarsLastModifiedMap.get(fileName);
                long currentFileLasModified = file.lastModified();
                log.debug("File {}. Saved last modified: {}. Current last modified: {}", fileName, lastModified, currentFileLasModified);
                if (lastModified == null || lastModified < currentFileLasModified) {
                    log.info("New war file found {}", fileName);
                    try (ZipFile zipFile = new ZipFile(file)) {
                        Application app = warManager.createApp(zipFile);
                        applicationContainer.registerApp(app);
                        app.getAppServletContext().loadServletsOnStartUp();
                    } catch (Exception e) {
                        throw new AppInstantiationException("Error instantiating application from file " + fileName, e);
                    }
                    scannedWarsLastModifiedMap.put(fileName, currentFileLasModified);
                }
            }
        }
        log.debug("Finish scanning webapps for new war files");
    }


}
