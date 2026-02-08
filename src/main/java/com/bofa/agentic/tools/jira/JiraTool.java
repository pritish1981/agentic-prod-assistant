package com.bofa.agentic.tools.jira;

import java.util.Map;

import com.bofa.agentic.executor.ExecutionResult;
import com.bofa.agentic.tools.AgentTool;

public class JiraTool implements AgentTool {

	    private final JiraClient jiraClient;
	    
	    public JiraTool(JiraClient jiraClient) {
	        this.jiraClient = jiraClient;
	    }
	    @Override
	    public String name() {
	        return "jira-tool";
	    }

	    @Override
	    public ExecutionResult execute(Map<String, Object> input) {
			long startTimeMs = System.currentTimeMillis();

			try {
				String issueKey = (String) input.get("issueKey");

				if (issueKey == null) {
					return new ExecutionResult(
							name(),
							"Missing required field: issueKey",
							false,
							true,
							System.currentTimeMillis() - startTimeMs
					);
				}

				String response = jiraClient.getIssue(issueKey);

				return new ExecutionResult(
						name(),
						response,
						true,
						false,
						System.currentTimeMillis() - startTimeMs
				);
			} catch (Exception ex) {
				return new ExecutionResult(
						name(),
						"Jira tool execution failed: " + ex.getMessage(),
						false,
						true,
						System.currentTimeMillis() - startTimeMs
				);
			}
	    }

	
}
