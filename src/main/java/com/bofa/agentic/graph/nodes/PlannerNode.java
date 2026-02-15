package com.bofa.agentic.graph.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphNode;
import com.bofa.agentic.model.ExecutionContext;
import com.bofa.agentic.orchestrator.ExecutionPlan;
import com.bofa.agentic.orchestrator.Planner;

/**
 * Planner Node - Decides strategy (RAG vs Tools)
 * 
 * Uses LLM to analyze the query and determine:
 * - Should we search knowledge base (RAG)?
 * - Should we execute tools?
 * - Which tools to use?
 */
@Component
public class PlannerNode implements GraphNode {
    
    private static final Logger log = LoggerFactory.getLogger(PlannerNode.class);
    
    private final Planner planner;
    
    public PlannerNode(Planner planner) {
        this.planner = planner;
    }
    
    @Override
    public AgentState execute(AgentState state) throws Exception {
        log.debug("Executing PlannerNode for query: {}", state.getRequest().message());
        
        ExecutionContext context = new ExecutionContext(state.getRequest());
        ExecutionPlan plan = planner.plan(context);
        
        boolean useRag = plan.useRag();
        boolean useTools = !plan.toolCalls().isEmpty();
        
        log.info("Plan decided: useRag={}, useTools={}, toolCount={}", 
                useRag, useTools, plan.toolCalls().size());
        
        AgentState newState = state
                .withRagDecision(useRag)
                .withToolsDecision(useTools, 
                        plan.toolCalls().stream()
                                .map(tc -> tc.toolName())
                                .toList())
                .addMetadata("plan", plan);
        
        return newState;
    }
    
    @Override
    public String getName() {
        return "planner";
    }
}
