package com.bofa.agentic.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
/*
 * Encapsulates embedding generation.

   Why not call model directly everywhere?

   👉 Because you WILL switch models later.

    Architect rule:

        Always abstract model calls.
 * 
 */

@Service
public class EmbeddingService {
	private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
	public float[] embed(String text) {
        return embeddingModel.embed(text);
    }
	

}
