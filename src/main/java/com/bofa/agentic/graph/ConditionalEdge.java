package com.bofa.agentic.graph;

/**
 * Conditional edge router - determines next node based on state
 * 
 * This enables dynamic routing in the graph:
 * - After Planner: route to RAG, Tools, or directly to Composer
 * - After Guard: route to END if passed, or Reflection if failed
 * - After Reflection: route back to Planner for retry
 * 
 * Inspired by LangGraph's conditional edges
 */
@FunctionalInterface
public interface ConditionalEdge {
    
    /**
     * Determine the next node based on current state
     * 
     * @param state Current agent state
     * @return Name of the next node to execute
     */
    String route(AgentState state);
}
