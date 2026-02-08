package com.bofa.agentic.tools.confluence;

import org.springframework.stereotype.Component;

import com.bofa.agentic.executor.AgentTool;
import com.bofa.agentic.executor.ExecutionResult;

@Component
public class ConfluenceTool implements AgentTool{
	private final ConfluenceClient client;

	public ConfluenceTool(ConfluenceClient client) {
		super();
		this.client = client;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "confluence-tool";
	}

	@Override
    public ExecutionResult execute(String input) {
        long startTimeMs = System.currentTimeMillis();
        try {
            String query = input;
            if (query == null || query.isBlank()) {
                return new ExecutionResult(
                        name(),
                        "Missing required field: query",
                        false,
                        true,
                        System.currentTimeMillis() - startTimeMs
                );
            }

            String result = client.search(query);
            return new ExecutionResult(
                    name(),
                    result,
                    true,
                    false,
                    System.currentTimeMillis() - startTimeMs
            );
        } catch (Exception ex) {
            return new ExecutionResult(
                    name(),
                    "Confluence tool failed: " + ex.getMessage(),
                    false,
                    true,
                    System.currentTimeMillis() - startTimeMs
            );
        }
    }

	
}
