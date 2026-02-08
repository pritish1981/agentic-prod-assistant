package com.bofa.agentic.memory;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.Getter;

//Enterprise conversation store with sliding window support.

@Component
public class ConversationMemory {
	private static final Logger log = LoggerFactory.getLogger(ConversationMemory.class);
	
	 private static final int MAX_MESSAGES = 20; // sliding window

	    private final Map<String, Deque<Message>> sessionMemory = new ConcurrentHashMap<>();

	    public void addUserMessage(String sessionId, String content) {
	        addMessage(sessionId, "USER", content);
	    }

	    public void addAgentMessage(String sessionId, String content) {
	        addMessage(sessionId, "AGENT", content);
	    }

	    private void addMessage(String sessionId, String role, String content) {

	        sessionMemory.putIfAbsent(sessionId, new ArrayDeque<>());

	        Deque<Message> messages = sessionMemory.get(sessionId);

	        if (messages.size() >= MAX_MESSAGES) {
	            messages.pollFirst(); // remove oldest
	        }

	        messages.addLast(new Message(role, content, Instant.now()));

	        log.debug("Memory updated for session={}", sessionId);
	    }

	    public List<Message> getConversation(String sessionId) {
	        return new ArrayList<>(sessionMemory.getOrDefault(sessionId, new ArrayDeque<>()));
	    }

	    public void clearSession(String sessionId) {
	        sessionMemory.remove(sessionId);
	        log.info("Cleared memory for session={}", sessionId);
	    }

	    public boolean sessionExists(String sessionId) {
	        return sessionMemory.containsKey(sessionId);
	    }

	    @Getter
	    public static class Message {
	        private final String role;
	        private final String content;
	        private final Instant timestamp;

	        public Message(String role, String content, Instant timestamp) {
	            this.role = role;
	            this.content = content;
	            this.timestamp = timestamp;
	        }
	    }

}
