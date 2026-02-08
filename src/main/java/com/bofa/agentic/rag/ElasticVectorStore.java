package com.bofa.agentic.rag;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnBean(VectorStore.class)
public class ElasticVectorStore {
	
	private final VectorStore vectorStore;

    public ElasticVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

	
	/**
     * Store documents into Elasticsearch vector index
     */
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }
    
    /**
     * Semantic similarity search
     */
    public List<Document> similaritySearch(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        );
    }

}
