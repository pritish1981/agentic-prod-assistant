package com.bofa.agentic.executor;

public interface AgentTool {
	String name();

    ExecutionResult execute(String input);

}
