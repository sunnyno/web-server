package com.dzytsiuk.webserver.app.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.util.Queue;

public class WebAppFilterChain implements FilterChain {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Queue<Filter> filterQueue;

    public WebAppFilterChain(Queue<Filter> filterQueue) {
        this.filterQueue = filterQueue;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Filter filter =  filterQueue.poll();
        if(filter != null){
            log.info("Applying filter {}", filter.getClass().getName());
            filter.doFilter(request, response, this);
        }
    }
}
