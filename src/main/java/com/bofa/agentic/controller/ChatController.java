package com.bofa.agentic.controller;

import org.springframework.web.bind.annotation.*;

import com.bofa.agentic.model.ChatRequest;
import com.bofa.agentic.orchestrator.AgentOrchestrator;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
	private final AgentOrchestrator orchestrator;

	public ChatController(AgentOrchestrator orchestrator) {
		this.orchestrator = orchestrator;
	}
	
	@PostMapping(produces = "text/event-stream")
	public Flux<String> chat(@RequestBody ChatRequest request){
		 return orchestrator.process(request);
	}

}
