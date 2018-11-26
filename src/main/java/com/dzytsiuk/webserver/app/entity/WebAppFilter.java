package com.dzytsiuk.webserver.app.entity;

import java.util.Objects;

public class WebAppFilter {
    private String name;
    private String className;
    private String uriPattern;

    public WebAppFilter() {
    }

    public WebAppFilter(WebAppFilter currentWebAppFilter) {
        this.name = currentWebAppFilter.getName();
        this.className = currentWebAppFilter.getClassName();
        this.uriPattern = currentWebAppFilter.getUriPattern();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebAppFilter that = (WebAppFilter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(className, that.className) &&
                Objects.equals(uriPattern, that.uriPattern);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, className, uriPattern);
    }

    @Override
    public String toString() {
        return "WebAppFilter{" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", uriPattern='" + uriPattern + '\'' +
                '}';
    }
}
