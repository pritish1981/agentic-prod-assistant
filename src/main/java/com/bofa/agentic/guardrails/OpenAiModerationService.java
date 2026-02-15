package com.bofa.agentic.guardrails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Service to interact with OpenAI Moderation API
 * 
 * The Moderation API is free to use and can be used to check whether content
 * complies with OpenAI's usage policies. It helps reduce the frequency of 
 * unsafe content in your applications.
 * 
 * Categories checked:
 * - hate, hate/threatening
 * - harassment, harassment/threatening
 * - self-harm, self-harm/intent, self-harm/instructions
 * - sexual, sexual/minors
 * - violence, violence/graphic
 * 
 * API Docs: https://platform.openai.com/docs/guides/moderation
 */
@Service
public class OpenAiModerationService {
    
    private static final Logger log = LoggerFactory.getLogger(OpenAiModerationService.class);
    private static final String MODERATION_ENDPOINT = "https://api.openai.com/v1/moderations";
    
    private final RestClient restClient;
    private final boolean enabled;
    
    public OpenAiModerationService(
            @Value("${OPENAI_API_KEY}") String apiKey,
            @Value("${agentic.guardrails.moderation.enabled:true}") boolean enabled) {
        this.enabled = enabled;
        this.restClient = RestClient.builder()
                .baseUrl(MODERATION_ENDPOINT)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        log.info("OpenAI Moderation API initialized. Enabled: {}", enabled);
    }
    
    /**
     * Check if the given text violates OpenAI's usage policies
     * 
     * @param text The text to moderate (user input or AI response)
     * @return ModerationResult with flagged categories and scores
     */
    public ModerationResult moderate(String text) {
        if (!enabled) {
            log.debug("Moderation disabled, skipping check");
            return createPassResult();
        }
        
        if (text == null || text.isBlank()) {
            log.warn("Empty text provided for moderation");
            return createPassResult();
        }
        
        try {
            Map<String, String> requestBody = Map.of("input", text);
            
            ModerationResult result = restClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(ModerationResult.class);
            
            if (result != null && result.isFlagged()) {
                log.warn("Content flagged by OpenAI Moderation API. Categories: {}", 
                        result.getFlaggedCategories());
            } else {
                log.debug("Content passed OpenAI Moderation check");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Failed to call OpenAI Moderation API. Allowing content by default.", e);
            // Fail open - don't block on API errors
            return createPassResult();
        }
    }
    
    /**
     * Moderate both user input and AI response
     * Useful for bidirectional safety checks
     */
    public ModerationResult moderateBoth(String userInput, String aiResponse) {
        ModerationResult inputResult = moderate(userInput);
        if (inputResult.isFlagged()) {
            return inputResult;
        }
        return moderate(aiResponse);
    }
    
    private ModerationResult createPassResult() {
        ModerationResult result = new ModerationResult();
        result.setId("local-pass");
        result.setModel("disabled");
        ModerationResult.Result innerResult = new ModerationResult.Result();
        innerResult.setFlagged(false);
        result.setResults(java.util.List.of(innerResult));
        return result;
    }
}
