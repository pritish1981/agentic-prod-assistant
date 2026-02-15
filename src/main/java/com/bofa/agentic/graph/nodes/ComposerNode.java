package com.bofa.agentic.graph.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphNode;
import com.bofa.agentic.orchestrator.ResponseComposer;

/**
 * Composer Node - Synthesizes final response
 * 
 * Combines user query + RAG evidence + tool results
 * into a coherent, helpful response using LLM
 */
@Component
public class ComposerNode implements GraphNode {
    
    private static final Logger log = LoggerFactory.getLogger(ComposerNode.class);
    
    private final ResponseComposer composer;
    
    public ComposerNode(ResponseComposer composer) {
        this.composer = composer;
    }
    
    @Override
    public AgentState execute(AgentState state) throws Exception {
        log.debug("Executing ComposerNode");
        
        String response = composer.compose(
                state.getRequest().message(),
                state.getRagEvidence(),
                state.getToolResults()
        );
        
        log.info("Response composed. Length: {} chars", 
                response != null ? response.length() : 0);
        
        return state
                .withResponse(response)
                .addMetadata("responseComposed", true);
    }
    
    @Override
    public String getName() {
        return "composer";
    }
}
