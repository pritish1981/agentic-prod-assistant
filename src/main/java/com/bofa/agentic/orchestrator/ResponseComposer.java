package com.bofa.agentic.orchestrator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ResponseComposer {

	private final ChatClient chatClient;

	public ResponseComposer(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}

	public String compose(String query, String evidence, String toolData) {

		return chatClient.prompt()
				.system("Answer ONLY from provided evidence.")
				.user("""
				Query: %s
				Evidence: %s
				ToolData: %s
				""".formatted(query, evidence, toolData)).call().content();
	}

}
