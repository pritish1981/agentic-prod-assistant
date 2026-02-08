package com.bofa.agentic.model;

import java.util.Map;

public class ExecutionContext {
private final ChatRequest request;

private Map<String, Object> memory;

public ExecutionContext(ChatRequest request) {
	this.request = request;
}

public ChatRequest getRequest() {
	return request;
}

public Map<String, Object> getMemory() {
	return memory;
}

public void setMemory(Map<String, Object> memory) {
	this.memory = memory;
}


}
