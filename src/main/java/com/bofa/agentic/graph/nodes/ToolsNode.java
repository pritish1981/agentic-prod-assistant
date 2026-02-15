package com.bofa.agentic.graph.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bofa.agentic.executor.ToolExecutor;
import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphNode;
import com.bofa.agentic.orchestrator.ExecutionPlan;

/**
 * Tools Node - Executes external tool calls
 * 
 * Runs tools (Jira, Confluence, Outlook) in parallel
 * and aggregates results
 */
@Component
public class ToolsNode implements GraphNode {
    
    private static final Logger log = LoggerFactory.getLogger(ToolsNode.class);
    
    private final ToolExecutor toolExecutor;
    
    public ToolsNode(ToolExecutor toolExecutor) {
        this.toolExecutor = toolExecutor;
    }
    
    @Override
    public AgentState execute(AgentState state) throws Exception {
        log.debug("Executing ToolsNode with {} tools", state.getToolNames().size());
        
        // Extract plan from metadata
        ExecutionPlan plan = (ExecutionPlan) state.getMetadata().get("plan");
        
        if (plan == null || plan.toolCalls().isEmpty()) {
            log.warn("ToolsNode called but no tools in plan");
            return state.withToolResults("");
        }
        
        String results = toolExecutor.execute(plan);
        
        log.info("Tools execution completed. Results length: {} chars", 
                results != null ? results.length() : 0);
        
        return state
                .withToolResults(results)
                .addMetadata("toolsExecuted", true);
    }
    
    @Override
    public String getName() {
        return "tools";
    }
}
