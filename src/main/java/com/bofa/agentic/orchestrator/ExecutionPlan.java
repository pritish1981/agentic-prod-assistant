package com.bofa.agentic.orchestrator;

import java.util.List;

import com.bofa.agentic.model.ToolCall;

public record ExecutionPlan(
		boolean useRag,
		List<ToolCall> toolCalls) {

}
