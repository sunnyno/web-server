package com.dzytsiuk.webserver.config;

import com.dzytsiuk.webserver.http.HttpConnector;
import com.dzytsiuk.webserver.http.processor.HttpProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@ComponentScan(basePackages = "com.dzytsiuk.webserver")
@PropertySource("classpath:application.properties")
public class ServerConfiguration {
    @Bean
    public HttpConnector httpConnector() {
        return new HttpConnector() {
            @Override
            public HttpProcessor getHttpProcessor() {
                return httpProcessor();
            }
        };
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpProcessor httpProcessor() {
        return new HttpProcessor();
    }

    @Bean
    public ScheduledExecutorService taskScheduler() {
        return Executors.newSingleThreadScheduledExecutor();
    }

}
