package com.bofa.agentic.streaming;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.model.ChatResponse;

import reactor.core.publisher.Flux;

public class TokenStreamingService {
    private static final Logger log = LoggerFactory.getLogger(TokenStreamingService.class);
	
	private final ChatClient chatClient;

    public TokenStreamingService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Stream tokens from the LLM in real-time.
     *
     * This method is intentionally transport-agnostic.
     * It can power:
     * - SSE
     * - WebSockets
     * - gRPC
     */
    public Flux<String> streamTokens(String prompt) {

        long start = System.currentTimeMillis();

        return chatClient
                .prompt(prompt)
                .stream()
                .chatResponse()
                .map(this::extractToken)
                .doOnSubscribe(s -> log.info("Starting token stream..."))
                .doOnComplete(() ->
                        log.info("Streaming completed in {} ms",
                                System.currentTimeMillis() - start))
                .timeout(Duration.ofSeconds(120))
                .onErrorResume(ex -> {

                    log.error("Streaming error", ex);

                    return Flux.just(
                            "\n\n⚠️ The response was interrupted. Please retry."
                    );
                });
    }

    /**
     * Extract token safely from ChatResponse
     */
    private String extractToken(ChatResponse response) {

        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }

        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }
}


