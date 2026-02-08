package com.bofa.agentic.tools;


import java.util.Map;

import com.bofa.agentic.executor.ExecutionResult;

public interface AgentTool {

	String name();

	ExecutionResult execute(Map<String, Object> input);
}