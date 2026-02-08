package com.bofa.agentic.tools.confluence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Component
public class ConfluenceClient {
	
	private static final Logger log = LoggerFactory.getLogger(ConfluenceClient.class);
	private final WebClient.Builder builder;

    @Value("${confluence.base-url}")
    private String baseUrl;

    @Value("${confluence.token}")
    private String token;

    public ConfluenceClient(Builder builder) {
    super();
    this.builder = builder;
  }

	public String search(String query) {

        log.info("Searching Confluence for query={}", query);

        return builder.build()
                .get()
                .uri(baseUrl + "/rest/api/search?cql=text~\"" + query + "\"")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(10))
                .doOnError(e -> log.error("Confluence API failed", e))
                .block();
    }

}
