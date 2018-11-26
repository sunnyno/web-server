package com.dzytsiuk.webserver.context;

import com.dzytsiuk.webserver.context.threadfactory.DaemonThreadFactory;
import com.dzytsiuk.webserver.http.entity.Session;
import com.dzytsiuk.webserver.util.AppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class SessionManager {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    private static final int SESSION_CHECK_RATE = Integer.parseInt(AppUtil.getApplicationProperty("session.check.rate"));
    private static final int SESSION_CHECK_DELAY_RATE = Integer.parseInt(AppUtil.getApplicationProperty("session.check.rate"));
    private static final int SESSION_DEFAULT_TIMEOUT = Integer.parseInt(AppUtil.getApplicationProperty("session.default.timeout"));
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
                log.info("Session {} has expired", session.getId());
                expiredSessions.add(session.getId());
                session.invalidate();
            }
        }
        expiredSessions.forEach(s -> httpSessionMap.remove(s));
    }

    private boolean isInvalid(HttpSession session) {
        int maxInactiveInterval = session.getMaxInactiveInterval();
        if (maxInactiveInterval == 0) {
            maxInactiveInterval = SESSION_DEFAULT_TIMEOUT;
        }
        return System.currentTimeMillis() - session.getLastAccessedTime() > maxInactiveInterval * MILLISECONDS_IN_MINUTE;
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

    public String changeSessionId(String sessionId){
        HttpSession session = httpSessionMap.get(sessionId);
        httpSessionMap.remove(sessionId);
        String newSessionId = UUID.randomUUID().toString();
        httpSessionMap.put(newSessionId, session);
        return newSessionId;
    }
}
