package com.bofa.agentic.graph;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bofa.agentic.graph.nodes.*;

/**
 * Configures the LangGraph-inspired state machine
 * 
 * Defines the graph structure:
 * 
 * START → planner → (conditional routing)
 *                ↓
 *            [rag] ← (if useRag=true)
 *                ↓
 *          [tools] ← (if useTools=true)
 *                ↓
 *          composer → guard → (conditional routing)
 *                               ↓
 *                           [END] ← (if passed)
 * 
 * Future enhancement: Add reflection node for self-correction cycles
 */
@Configuration
public class GraphConfiguration {
    
    @Bean
    public StateGraph agentStateGraph(
            PlannerNode plannerNode,
            RagNode ragNode,
            ToolsNode toolsNode,
            ComposerNode composerNode,
            GuardNode guardNode) {
        
        return StateGraph.builder()
                // Add all nodes
                .addNode("planner", plannerNode)
                .addNode("rag", ragNode)
                .addNode("tools", toolsNode)
                .addNode("composer", composerNode)
                .addNode("guard", guardNode)
                
                // Set entry point
                .setEntryPoint("planner")
                
                // Conditional routing after planner
                .addConditionalEdge("planner", state -> {
                    if (state.isUseRag() && state.isUseTools()) {
                        return "rag"; // Do RAG first, then tools
                    } else if (state.isUseRag()) {
                        return "rag"; // Only RAG
                    } else if (state.isUseTools()) {
                        return "tools"; // Only tools
                    } else {
                        return "composer"; // Direct to composer
                    }
                })
                
                // After RAG: go to tools if needed, otherwise composer
                .addConditionalEdge("rag", state -> 
                        state.isUseTools() ? "tools" : "composer")
                
                // After tools: always go to composer
                .addEdge("tools", "composer")
                
                // After composer: always go to guard
                .addEdge("composer", "guard")
                
                // Conditional routing after guard
                .addConditionalEdge("guard", state -> {
                    if (state.isGuardrailPassed()) {
                        return "END"; // Success
                    } else {
                        // Guardrail failed - could add reflection/retry here
                        // For now, terminate with error
                        return "END";
                    }
                })
                
                .build();
    }
    
    @Bean
    public GraphExecutor graphExecutor(StateGraph agentStateGraph) {
        return new GraphExecutor(agentStateGraph);
    }
}
