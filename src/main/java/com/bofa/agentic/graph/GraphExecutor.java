package com.bofa.agentic.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes the agent graph - walks through nodes following edges
 * 
 * Implements the LangGraph execution model:
 * 1. Start with initial state
 * 2. Execute current node → get updated state
 * 3. Route to next node based on conditional/fixed edges
 * 4. Repeat until terminal node (END or ERROR)
 * 
 * Supports cycles for reflection/retry patterns
 */
public class GraphExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(GraphExecutor.class);
    private static final int MAX_ITERATIONS = 20; // Prevent infinite loops
    
    private final StateGraph graph;
    
    public GraphExecutor(StateGraph graph) {
        this.graph = graph;
    }
    
    /**
     * Execute the graph starting from initial state
     * 
     * @param initialState Starting state (with user request)
     * @return Final state after graph execution
     * @throws Exception if any node fails
     */
    public AgentState execute(AgentState initialState) throws Exception {
        AgentState currentState = initialState;
        int iterations = 0;
        
        String currentNode = graph.getEntryPoint();
        log.info("Starting graph execution from node: {}", currentNode);
        
        while (!isTerminal(currentNode) && iterations < MAX_ITERATIONS) {
            iterations++;
            
            log.debug("Iteration {}: Executing node '{}'", iterations, currentNode);
            
            // Get and execute the node
            GraphNode node = graph.getNode(currentNode);
            if (node == null) {
                throw new IllegalStateException("Node not found in graph: " + currentNode);
            }
            
            try {
                // Execute node and get updated state
                AgentState newState = node.execute(currentState);
                
                // Determine next node
                String nextNode = graph.getNextNode(currentNode, newState);
                
                log.info("Node '{}' → '{}' (elapsed: {}ms)", 
                        currentNode, nextNode, newState.getElapsedTime());
                
                // Update state with transition
                currentState = newState.moveTo(nextNode);
                currentNode = nextNode;
                
            } catch (Exception e) {
                log.error("Node '{}' failed: {}", currentNode, e.getMessage(), e);
                
                // Add error metadata and move to ERROR terminal
                currentState = currentState
                        .addMetadata("error", e.getMessage())
                        .addMetadata("failedNode", currentNode)
                        .moveTo("ERROR");
                
                throw e;
            }
        }
        
        if (iterations >= MAX_ITERATIONS) {
            log.error("Graph execution exceeded max iterations ({}). Possible cycle detected.", 
                    MAX_ITERATIONS);
            throw new IllegalStateException("Graph execution exceeded maximum iterations");
        }
        
        log.info("Graph execution completed in {} iterations, {}ms total", 
                iterations, currentState.getElapsedTime());
        log.debug("Executed nodes: {}", currentState.getExecutedNodes());
        
        return currentState;
    }
    
    private boolean isTerminal(String nodeName) {
        return "END".equals(nodeName) || "ERROR".equals(nodeName);
    }
}
