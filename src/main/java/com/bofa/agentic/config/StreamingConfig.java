package com.bofa.agentic.config;

import org.springframework.context.annotation.Configuration;

import reactor.core.scheduler.Schedulers;

@Configuration
public class StreamingConfig {
	public StreamingConfig() {
		Schedulers.enableMetrics();
	}
}
