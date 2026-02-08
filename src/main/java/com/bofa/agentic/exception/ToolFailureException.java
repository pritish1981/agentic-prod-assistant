package com.bofa.agentic.exception;

import java.util.Map;

public class ToolFailureException extends AgentException{
	public ToolFailureException(
            String toolName,
            String message,
            boolean retryable,
            Throwable cause) {

        super(
                "TOOL_FAILURE_" + toolName.toUpperCase(),
                message,
                retryable,
                Map.of("tool", toolName),
                cause
        );
    }
	
	public ToolFailureException(String toolName, String message) {

        super(
                "TOOL_FAILURE_" + toolName.toUpperCase(),
                message,
                true,
                Map.of("tool", toolName),
                null
        );
    }

}
