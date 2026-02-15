package com.bofa.agentic.guardrails;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/*
 * Multi-layer Safety Validation:
 * 
 * Layer 1: OpenAI Moderation API
 *   - Checks for: hate, harassment, self-harm, sexual content, violence
 *   - Free to use, production-grade
 * 
 * Layer 2: Domain-specific keyword filtering
 *   - Prevents unsafe operational instructions
 *   - Blocks exposure of sensitive commands
 * 
 * Both layers must pass for content to be allowed.
 */

@Component
public class SafetyValidator {
	
	private static final Logger log = LoggerFactory.getLogger(SafetyValidator.class);
	
	private static final List<String> BLOCKED_TERMS = List.of(
            "drop database",
            "delete production",
            "expose credentials",
            "shutdown server",
            "rm -rf /",
            "delete all"
    );
    
    private final OpenAiModerationService moderationService;

    public SafetyValidator(OpenAiModerationService moderationService) {
        this.moderationService = moderationService;
    }

    public GuardrailResult validate(String response) {
        
        // Layer 1: OpenAI Moderation API (primary guardrail)
        ModerationResult moderationResult = moderationService.moderate(response);
        
        if (moderationResult.isFlagged()) {
            String categories = moderationResult.getFlaggedCategories();
            log.warn("Content flagged by OpenAI Moderation: {}", categories);
            return GuardrailResult.block(
                    "Content violates safety policies. Flagged categories: " + categories
            );
        }
        
        // Layer 2: Domain-specific keyword filtering (secondary guardrail)
        String lower = response.toLowerCase();
        boolean unsafe = BLOCKED_TERMS.stream()
                .anyMatch(lower::contains);

        if (unsafe) {
            log.warn("Content flagged by keyword filter");
            return GuardrailResult.block(
                    "Response contains unsafe operational guidance."
            );
        }

        return GuardrailResult.allow(0.95);
    }

}
