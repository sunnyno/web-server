package com.dzytsiuk.webserver.http.entity;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Session implements HttpSession {
    private final String id;
    private long creationTime;
    private long lastAccessTime;
    private ServletContext servletContext;
    private int maxInactiveInterval;
    private Map<String, Object> attributes = new HashMap<>();
    private boolean isInvalid;

    public Session(String sessionId, ServletContext servletContext) {
        this.id = sessionId;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessTime = creationTime;
        this.servletContext = servletContext;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        return lastAccessTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        return attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String[] getValueNames() {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        return attributes.keySet().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        attributes.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        attributes.remove(name);
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        isInvalid = true;
    }

    @Override
    public boolean isNew() {
        if(isInvalid) throw new IllegalStateException("Session is invalid");
        return creationTime == lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
