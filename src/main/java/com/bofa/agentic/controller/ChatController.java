package com.bofa.agentic.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.bofa.agentic.guardrails.InputGuardrail;
import com.bofa.agentic.model.ChatRequest;
import com.bofa.agentic.orchestrator.AgentOrchestrator;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	private final AgentOrchestrator orchestrator;
	private final InputGuardrail inputGuardrail;

	public ChatController(AgentOrchestrator orchestrator, InputGuardrail inputGuardrail) {
		this.orchestrator = orchestrator;
		this.inputGuardrail = inputGuardrail;
	}
	
	@PostMapping(
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.TEXT_EVENT_STREAM_VALUE
	)
	public Flux<String> chat(@RequestBody ChatRequest request) {
		// Wrap everything in Flux.defer to handle all errors reactively
		return Flux.defer(() -> {
			// Validate input (throws AgentException if invalid)
			inputGuardrail.validateInput(request.message());
			
			// Process request through orchestrator
			return orchestrator.process(request);
		})
		.onErrorResume(ex -> {
			// Handle ALL errors gracefully in the stream
			// This catches both input and output validation failures
			String errorMessage = buildErrorMessage(ex);
			return Flux.just(errorMessage);
		});
	}
	
	/**
	 * Build user-friendly error message for streaming response
	 */
	private String buildErrorMessage(Throwable ex) {
		if (ex instanceof com.bofa.agentic.exception.AgentException agentEx) {
			return String.format("❌ Safety Check Failed: %s", agentEx.getMessage());
		}
		return "❌ An unexpected error occurred. Please try again.";
	}

}
