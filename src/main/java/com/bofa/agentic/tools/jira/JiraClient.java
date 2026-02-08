package com.bofa.agentic.tools.jira;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

public class JiraClient {
	private static final Logger log = LoggerFactory.getLogger(JiraClient.class);
	
	private final WebClient.Builder webClientBuilder;

    public JiraClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Value("${jira.base-url}")
    private String baseUrl;

    @Value("${jira.token}")
    private String token;

    public String getIssue(String issueKey) {

        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/rest/api/3/issue/" + issueKey)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .doOnError(e -> log.error("Jira API failed", e))
                .block();
    }

}
