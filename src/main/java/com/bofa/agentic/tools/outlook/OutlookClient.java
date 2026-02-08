package com.bofa.agentic.tools.outlook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Component
public class OutlookClient {
	
	private static final Logger log = LoggerFactory.getLogger(OutlookClient.class);
	
	private final WebClient.Builder builder;
	
	public OutlookClient(Builder builder) {
		super();
		this.builder = builder;
	}

    @Value("${outlook.base-url}")
    private String baseUrl;

    @Value("${outlook.token}")
    private String token;

    public String searchEmails(String keyword) {

        log.info("Searching Outlook emails for keyword={}", keyword);

        return builder.build()
                .get()
                .uri(baseUrl + "/v1.0/me/messages?$search=\"" + keyword + "\"")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("ConsistencyLevel", "eventual")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .doOnError(e -> log.error("Outlook API failed", e))
                .block();
    }

	

}
