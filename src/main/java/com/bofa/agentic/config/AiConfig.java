package com.bofa.agentic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.ObjectMapper;

// Shared WebClient configuration for tool clients
@Configuration
public class AiConfig {

	@Bean
	public WebClient.Builder webClientBuilder() {
	    return WebClient.builder();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
}
