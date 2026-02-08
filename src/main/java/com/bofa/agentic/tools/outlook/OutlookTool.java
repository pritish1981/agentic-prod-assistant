package com.bofa.agentic.tools.outlook;

import org.springframework.stereotype.Component;

import com.bofa.agentic.executor.AgentTool;
import com.bofa.agentic.executor.ExecutionResult;

@Component
public class OutlookTool implements AgentTool{
	
	private final OutlookClient client;
	

	public OutlookTool(OutlookClient client) {
		super();
		this.client = client;
	}

	@Override
	public String name() {
		return "outlook-tool";
	}

	@Override
    public ExecutionResult execute(String input) {
        long startTimeMs = System.currentTimeMillis();
        try {
            String keyword = input;
            if (keyword == null || keyword.isBlank()) {
                return new ExecutionResult(
                        name(),
                        "Missing required field: keyword",
                        false,
                        true,
                        System.currentTimeMillis() - startTimeMs
                );
            }

            String result = client.searchEmails(keyword);
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
                    "Outlook tool failed: " + ex.getMessage(),
                    false,
                    true,
                    System.currentTimeMillis() - startTimeMs
            );
        }
    }

}
