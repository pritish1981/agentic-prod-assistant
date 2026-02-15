package com.bofa.agentic.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the agent graph structure
 * 
 * Maps node names to their implementations and defines edges (routing logic)
 * 
 * Example graph:
 * START → planner → (conditional) → [rag, tools, composer] → composer → guard → (conditional) → [END, reflection]
 * 
 * Inspired by LangGraph's StateGraph
 */
public class StateGraph {
    
    private final Map<String, GraphNode> nodes;
    private final Map<String, ConditionalEdge> conditionalEdges;
    private final Map<String, String> fixedEdges;
    private String entryPoint;
    
    private StateGraph() {
        this.nodes = new HashMap<>();
        this.conditionalEdges = new HashMap<>();
        this.fixedEdges = new HashMap<>();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public GraphNode getNode(String nodeName) {
        return nodes.get(nodeName);
    }
    
    public String getNextNode(String currentNode, AgentState state) {
        // Check conditional edges first
        if (conditionalEdges.containsKey(currentNode)) {
            return conditionalEdges.get(currentNode).route(state);
        }
        
        // Fall back to fixed edges
        return fixedEdges.getOrDefault(currentNode, "END");
    }
    
    public String getEntryPoint() {
        return entryPoint;
    }
    
    public boolean hasNode(String nodeName) {
        return nodes.containsKey(nodeName);
    }
    
    /**
     * Builder for StateGraph
     */
    public static class Builder {
        private final StateGraph graph;
        
        private Builder() {
            this.graph = new StateGraph();
        }
        
        /**
         * Add a node to the graph
         */
        public Builder addNode(String name, GraphNode node) {
            graph.nodes.put(name, node);
            return this;
        }
        
        /**
         * Add a fixed edge (always routes to same next node)
         */
        public Builder addEdge(String from, String to) {
            graph.fixedEdges.put(from, to);
            return this;
        }
        
        /**
         * Add a conditional edge (routes based on state)
         */
        public Builder addConditionalEdge(String from, ConditionalEdge edge) {
            graph.conditionalEdges.put(from, edge);
            return this;
        }
        
        /**
         * Set the entry point node
         */
        public Builder setEntryPoint(String nodeName) {
            graph.entryPoint = nodeName;
            return this;
        }
        
        /**
         * Build the graph
         */
        public StateGraph build() {
            if (graph.entryPoint == null) {
                throw new IllegalStateException("Entry point must be set");
            }
            if (!graph.nodes.containsKey(graph.entryPoint)) {
                throw new IllegalStateException("Entry point node does not exist: " + graph.entryPoint);
            }
            return graph;
        }
    }
}
