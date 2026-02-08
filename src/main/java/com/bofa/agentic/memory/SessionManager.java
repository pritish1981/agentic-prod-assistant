package com.bofa.agentic.memory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
 * Enterprise session lifecycle manager.

Supports:

✅ UUID session generation
✅ Idle timeout
✅ Cleanup scheduler
✅ Future Redis plug-in
 * 
 */
@Component
public class SessionManager {
	
	private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
	
	private final ConversationMemory memory;

    public SessionManager(ConversationMemory memory) {
        this.memory = memory;
    }

    private static final long SESSION_TIMEOUT_SECONDS = 3600; // 1 hour

    private final Map<String, Instant> sessionActivity = new ConcurrentHashMap<>();

    /**
     * Create new session
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionActivity.put(sessionId, Instant.now());

        log.info("Created new session={}", sessionId);
        return sessionId;
    }

    /**
     * Validate session
     */
    public boolean isValid(String sessionId) {
        return sessionActivity.containsKey(sessionId);
    }

    /**
     * Update last activity timestamp
     */
    public void touch(String sessionId) {
        sessionActivity.put(sessionId, Instant.now());
    }

    /**
     * Destroy session
     */
    public void destroy(String sessionId) {
        memory.clearSession(sessionId);
        sessionActivity.remove(sessionId);

        log.info("Destroyed session={}", sessionId);
    }

    /**
     * Automatic cleanup (Enterprise requirement)
     */
    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void cleanupIdleSessions() {

        Instant now = Instant.now();

        sessionActivity.entrySet().removeIf(entry -> {

            long idleSeconds =
                    now.getEpochSecond() - entry.getValue().getEpochSecond();

            if (idleSeconds > SESSION_TIMEOUT_SECONDS) {

                memory.clearSession(entry.getKey());

                log.info("Session expired due to inactivity={}", entry.getKey());
                return true;
            }

            return false;
        });
    }

}
