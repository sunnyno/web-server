package com.dzytsiuk.webserver.context;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApplicationContainer {
    private Map<String, Application> applicationMap = new ConcurrentHashMap<>();

    public Application getAppByName(String name) {
        if (name == null) {
            return null;
        }
        return applicationMap.get(name);
    }

    public void registerApp(Application application) {
        applicationMap.put(application.getName(), application);
    }
}
