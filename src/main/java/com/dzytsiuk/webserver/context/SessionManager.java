package com.dzytsiuk.webserver.context;

import com.dzytsiuk.webserver.http.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final int SESSION_CHECK_RATE = 1;
    private static final int SESSION_CHECK_DELAY_RATE = 1;
    private static final int MILLISECONDS_IN_MINUTE = 60000;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private Map<String, HttpSession> httpSessionMap = new ConcurrentHashMap<>();
    private ServletContext servletContext;

    SessionManager(ServletContext servletContext) {
        this.servletContext = servletContext;
        EXECUTOR_SERVICE.scheduleAtFixedRate(this::invalidateExpiredSessions, SESSION_CHECK_DELAY_RATE, SESSION_CHECK_RATE, TimeUnit.MINUTES);
    }

    private void invalidateExpiredSessions() {
        List<String> expiredSessions = new ArrayList<>();
        log.debug("Invalidating sessions");
        for (Map.Entry<String, HttpSession> sessionEntry : httpSessionMap.entrySet()) {
            HttpSession session = sessionEntry.getValue();
            if (isInvalid(session)) {
                expiredSessions.add(session.getId());
                session.invalidate();
            }
        }
        expiredSessions.forEach(s -> httpSessionMap.remove(s));
    }

    private boolean isInvalid(HttpSession session) {
        boolean isInvalid = System.currentTimeMillis() - session.getLastAccessedTime() > session.getMaxInactiveInterval() * MILLISECONDS_IN_MINUTE;
        if (isInvalid) {
            log.info("Session {} has expired", session.getId());
        }
        return isInvalid;
    }

    public HttpSession getSession(String sessionId, boolean create) {
        if (sessionId == null) {
            return null;
        }
        HttpSession foundSession = httpSessionMap.get(sessionId);
        if (foundSession == null) {
            if (create) {
                Session session = new Session(sessionId, servletContext);
                httpSessionMap.put(session.getId(), session);
                return session;
            }
        } else {
            ((Session) foundSession).setLastAccessTime(System.currentTimeMillis());
        }
        return foundSession;
    }


}
