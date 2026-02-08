package com.bofa.agentic.exception;

import java.util.Map;

import lombok.Getter;

@Getter
public class AgentException extends RuntimeException {

	private final String errorCode;
	private final boolean retryable;
	private final Map<String, Object> metadata;

	public AgentException(String errorCode, String message, boolean retryable, Map<String, Object> metadata,
			Throwable cause) {

		super(message, cause);
		this.errorCode = errorCode;
		this.retryable = retryable;
		this.metadata = metadata;
	}

	public AgentException(String errorCode, String message) {
		this(errorCode, message, false, null, null);
	}

	public AgentException(String errorCode, String message, boolean retryable) {
		this(errorCode, message, retryable, null, null);
	}
}


