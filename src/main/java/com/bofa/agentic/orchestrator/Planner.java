package com.bofa.agentic.orchestrator;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.bofa.agentic.model.ExecutionContext;


/*
 * Uses LLM to decide:
• RAG?
• Tools?
• Direct answer?
 * 
 */
@Component
public class Planner {
	
	private final ChatClient chatClient;
	
	public Planner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
	
	 public ExecutionPlan plan(ExecutionContext context) {
		 String decision = chatClient.prompt()
	                .user("Decide whether this query needs RAG or Tools: "
	                        + context.getRequest().message())
	                .call()
	                .content();
		 
		 boolean useRag = decision.toLowerCase().contains("rag");
		 
		 return new ExecutionPlan(useRag, List.of());
	 }
}
