package com.dzytsiuk.webserver.app.entity;

import java.util.Objects;

public class WebAppServlet {
    private String name;
    private String className;
    private String uriPattern;
    private int loadOnStartup;

    public WebAppServlet() {
    }

    public WebAppServlet(WebAppServlet currentWebAppServlet) {
        this.name = currentWebAppServlet.getName();
        this.className = currentWebAppServlet.getClassName();
        this.uriPattern = currentWebAppServlet.getUriPattern();
        this.loadOnStartup = currentWebAppServlet.getLoadOnStartup();
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

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebAppServlet that = (WebAppServlet) o;
        return loadOnStartup == that.loadOnStartup &&
                Objects.equals(name, that.name) &&
                Objects.equals(className, that.className) &&
                Objects.equals(uriPattern, that.uriPattern);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, className, uriPattern, loadOnStartup);
    }

    @Override
    public String toString() {
        return "WebAppServlet{" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", uriPattern='" + uriPattern + '\'' +
                ", loadOnStartup=" + loadOnStartup +
                '}';
    }
}
