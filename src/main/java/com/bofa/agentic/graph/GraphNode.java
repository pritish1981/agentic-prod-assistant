package com.bofa.agentic.graph;

/**
 * Represents a node in the agent graph
 * 
 * Each node performs a specific operation and returns updated state
 * Examples: PlannerNode, RagNode, ToolsNode, ComposerNode, GuardNode
 * 
 * Inspired by LangGraph's node pattern
 */
@FunctionalInterface
public interface GraphNode {
    
    /**
     * Execute this node's logic and return updated state
     * 
     * @param state Current agent state
     * @return Updated agent state after this node's execution
     * @throws Exception if node execution fails
     */
    AgentState execute(AgentState state) throws Exception;
    
    /**
     * Optional: Get node name for logging/debugging
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
