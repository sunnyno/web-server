package com.dzytsiuk.webserver;

import com.dzytsiuk.webserver.config.ServerConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Starter {
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(ServerConfiguration.class);
    }
}
