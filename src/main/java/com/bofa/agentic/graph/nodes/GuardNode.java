package com.bofa.agentic.graph.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bofa.agentic.exception.AgentException;
import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphNode;
import com.bofa.agentic.guardrails.HallucinationGuard;

/**
 * Guard Node - Validates response safety and grounding
 * 
 * Runs guardrails:
 * - OpenAI Moderation API (safety)
 * - Keyword filtering (unsafe operations)
 * - Grounding validation (hallucination detection)
 */
@Component
public class GuardNode implements GraphNode {
    
    private static final Logger log = LoggerFactory.getLogger(GuardNode.class);
    
    private final HallucinationGuard guard;
    
    public GuardNode(HallucinationGuard guard) {
        this.guard = guard;
    }
    
    @Override
    public AgentState execute(AgentState state) throws Exception {
        log.debug("Executing GuardNode");
        
        try {
            double confidence = guard.validateOrThrow(
                    state.getResponse(),
                    state.getRagEvidence()
            );
            
            log.info("Guardrails passed. Confidence: {}", confidence);
            
            return state
                    .withGuardrailResult(true, null)
                    .addMetadata("guardrailConfidence", confidence);
                    
        } catch (AgentException e) {
            log.warn("Guardrail failed: {} - {}", e.getErrorCode(), e.getMessage());
            
            return state
                    .withGuardrailResult(false, e.getMessage())
                    .addMetadata("guardrailError", e.getErrorCode());
        }
    }
    
    @Override
    public String getName() {
        return "guard";
    }
}
