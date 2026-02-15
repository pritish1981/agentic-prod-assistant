package com.bofa.agentic.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bofa.agentic.exception.AgentException;
import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphExecutor;
import com.bofa.agentic.model.ChatRequest;

import reactor.core.publisher.Flux;

/**
 * Agent Orchestrator - Now powered by LangGraph-inspired state machine
 * 
 * Replaced linear flow (Planner → RAG → Tools → Composer → Guard)
 * with graph-based routing for:
 * - Conditional branching (skip RAG if not needed)
 * - Better observability (track state transitions)
 * - Future: Reflection and retry cycles
 * 
 * Graph structure defined in GraphConfiguration
 */
@Service
public class AgentOrchestrator {
	
	private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);
	
	private final GraphExecutor graphExecutor;

    public AgentOrchestrator(GraphExecutor graphExecutor) {
        this.graphExecutor = graphExecutor;
    }
    
    public Flux<String> process(ChatRequest request) {
        
        // Wrap graph execution in Flux.defer for reactive error handling
        return Flux.defer(() -> {
            try {
                log.info("Processing request via graph: sessionId={}", request.sessionId());
                
                // Create initial state
                AgentState initialState = new AgentState(request);
                
                // Execute graph
                AgentState finalState = graphExecutor.execute(initialState);
                
                // Check if guardrail passed
                if (!finalState.isGuardrailPassed() && finalState.getGuardrailReason() != null) {
                    // Guardrail failed - throw exception to be caught by error handler
                    throw new AgentException(
                            "SAFETY_GUARDRAIL_BLOCKED",
                            finalState.getGuardrailReason()
                    );
                }
                
                // Return response
                String response = finalState.getResponse();
                
                log.info("Request processed successfully. Nodes executed: {}, Time: {}ms",
                        finalState.getExecutedNodes().size(),
                        finalState.getElapsedTime());
                
                return Flux.just(response);
                
            } catch (Exception e) {
                log.error("Graph execution failed", e);
                // Convert synchronous exceptions to reactive error signal
                return Flux.error(e);
            }
        });
    }

}
