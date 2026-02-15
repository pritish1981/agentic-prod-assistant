package com.bofa.agentic.graph.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.bofa.agentic.graph.AgentState;
import com.bofa.agentic.graph.GraphNode;
import com.bofa.agentic.rag.RagRetriever;

/**
 * RAG Node - Retrieves relevant context from vector store
 * 
 * Searches Elasticsearch vector store for relevant documents
 * based on semantic similarity to the user's query
 */
@Component
public class RagNode implements GraphNode {
    
    private static final Logger log = LoggerFactory.getLogger(RagNode.class);
    
    private final RagRetriever ragRetriever;
    
    public RagNode(RagRetriever ragRetriever) {
        this.ragRetriever = ragRetriever;
    }
    
    @Override
    public AgentState execute(AgentState state) throws Exception {
        log.debug("Executing RagNode for query: {}", state.getRequest().message());
        
        String evidence = ragRetriever.retrieve(state.getRequest().message());
        
        log.info("RAG retrieval completed. Evidence length: {} chars", 
                evidence != null ? evidence.length() : 0);
        
        return state
                .withRagEvidence(evidence)
                .addMetadata("ragExecuted", true);
    }
    
    @Override
    public String getName() {
        return "rag";
    }
}
