package com.bofa.agentic.executor;

public class ExecutionResult {
	private final String toolName;
    private final String response;
    private final boolean success;
    private final boolean failure;
    private final long latencyMs;

    public ExecutionResult(String toolName, String response, boolean success, boolean failure, long latencyMs) {
        this.toolName = toolName;
        this.response = response;
        this.success = success;
        this.failure = failure;
        this.latencyMs = latencyMs;
    }

    public String getToolName() {
        return toolName;
    }

    public String getResponse() {
        return response;
    }

    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return failure;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

}
