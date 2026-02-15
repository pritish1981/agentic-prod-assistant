package com.bofa.agentic.guardrails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.bofa.agentic.exception.AgentException;

/**
 * Input Guardrail - validates user input BEFORE processing
 * 
 * This provides defense-in-depth by catching policy violations
 * at the entry point, before LLM processing.
 * 
 * Use in ChatController or as a filter/interceptor.
 */
@Component
public class InputGuardrail {
    
    private static final Logger log = LoggerFactory.getLogger(InputGuardrail.class);
    
    private final OpenAiModerationService moderationService;
    private final boolean enabled;
    
    public InputGuardrail(
            OpenAiModerationService moderationService,
            @Value("${agentic.guardrails.input.enabled:true}") boolean enabled) {
        this.moderationService = moderationService;
        this.enabled = enabled;
        log.info("Input Guardrail initialized. Enabled: {}", enabled);
    }
    
    /**
     * Validate user input before processing
     * 
     * @param userMessage The user's input message
     * @throws AgentException if input violates safety policies
     */
    public void validateInput(String userMessage) {
        if (!enabled) {
            return;
        }
        
        if (userMessage == null || userMessage.isBlank()) {
            return;
        }
        
        ModerationResult result = moderationService.moderate(userMessage);
        
        if (result.isFlagged()) {
            String categories = result.getFlaggedCategories();
            log.warn("User input flagged by moderation API. Categories: {}", categories);
            
            throw new AgentException(
                    "INPUT_SAFETY_VIOLATION",
                    "Your message violates our content policy. Please rephrase and try again."
            );
        }
        
        log.debug("User input passed safety validation");
    }
}
