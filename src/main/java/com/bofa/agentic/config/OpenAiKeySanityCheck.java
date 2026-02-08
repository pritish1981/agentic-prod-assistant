package com.bofa.agentic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class OpenAiKeySanityCheck {
	private static final Logger logger = LoggerFactory.getLogger(OpenAiKeySanityCheck.class);
	private static final String OPENAI_KEY_PROPERTY = "spring.ai.openai.api-key";

	private final Environment environment;

	public OpenAiKeySanityCheck(Environment environment) {
		this.environment = environment;
	}

	@PostConstruct
	public void validateKey() {
		String key = environment.getProperty(OPENAI_KEY_PROPERTY);
		if (key == null || key.isBlank()) {
			return;
		}

		boolean hasWhitespace = !key.equals(key.trim()) || key.matches(".*\\s+.*");
		boolean hasQuotes = (key.startsWith("\"") && key.endsWith("\""))
				|| (key.startsWith("'") && key.endsWith("'"));
		boolean looksLikePlaceholder = key.contains("${") && key.contains("}");

		if (hasWhitespace || hasQuotes || looksLikePlaceholder) {
			logger.warn("OpenAI API key looks suspicious (whitespace, quotes, or unresolved placeholder). "
					+ "Check OPENAI_API_KEY in .env and spring.ai.openai.api-key resolution.");
		}
	}
}
