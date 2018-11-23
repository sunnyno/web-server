package com.dzytsiuk.webserver.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContainer {
    private static ApplicationContainer INSTANCE;
    private Map<String, Application> applicationMap = new ConcurrentHashMap<>();

    public static ApplicationContainer getInstance() {
        if(INSTANCE == null){
            INSTANCE = new ApplicationContainer();
        }
        return INSTANCE;
    }

    public Application getAppByName(String name) {
        if (name == null) {
            return null;
        }
        return applicationMap.get(name);
    }

    public void registerApp(Application application) {
        applicationMap.put(application.getName(), application);
    }

    public void shutDownApps() {
        for (Application application : applicationMap.values()) {
            application.getAppServletContext().shutDown();
        }
    }
}
