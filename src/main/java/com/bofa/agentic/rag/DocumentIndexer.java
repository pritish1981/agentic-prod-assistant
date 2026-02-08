package com.bofa.agentic.rag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DocumentIndexer {
    private static final Logger log = LoggerFactory.getLogger(DocumentIndexer.class);
	
	private final ElasticVectorStore vectorStore;
    private final ObjectMapper objectMapper;

    public DocumentIndexer(Optional<ElasticVectorStore> vectorStore, ObjectMapper objectMapper) {
        this.vectorStore = vectorStore.orElse(null);
        this.objectMapper = objectMapper;
    }
	
	/**
     * Auto-index sample documents on startup.
     * Remove in production and replace with ingestion pipeline.
     */
	
	@EventListener(ApplicationReadyEvent.class)
    public void indexDocuments() {

        try {
            if (vectorStore == null) {
                log.info("Skipping document indexing: ElasticVectorStore not available.");
                return;
            }

            log.info("Starting document indexing...");

            List<Document> documents = new ArrayList<>();

            indexFaqs(documents);
            indexSampleIncidentData(documents);

            if (documents.isEmpty()) {
                log.warn("No documents found to index.");
                return;
            }

            vectorStore.addDocuments(documents);

            log.info("Indexed {} documents successfully.", documents.size());

        } catch (Exception e) {
            log.error("Failed to index documents", e);
        }
    }

    private void indexFaqs(List<Document> documents) {
        Path path = Path.of("datasets", "prod-faqs.json");
        if (!Files.exists(path)) {
            log.warn("FAQ dataset not found at {}", path.toAbsolutePath());
            return;
        }

        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                Files.readString(path),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> item : items) {
                String question = String.valueOf(item.getOrDefault("question", ""));
                String answer = String.valueOf(item.getOrDefault("answer", ""));
                String category = String.valueOf(item.getOrDefault("category", ""));

                if (question.isBlank() && answer.isBlank()) {
                    continue;
                }

                String content = "FAQ\nQuestion: " + question + "\nAnswer: " + answer;
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "faq");
                if (!category.isBlank()) {
                    metadata.put("category", category);
                }

                documents.add(new Document(content, metadata));
            }
        } catch (Exception e) {
            log.error("Failed to index FAQ dataset", e);
        }
    }

    private void indexSampleIncidentData(List<Document> documents) {
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:sample-incident-data/*.txt");

            for (Resource resource : resources) {
                String content = Files.readString(resource.getFile().toPath());
                if (content == null || content.isBlank()) {
                    continue;
                }

                documents.add(new Document(content));
            }
        } catch (Exception e) {
            log.error("Failed to index sample incident data", e);
        }
    }

}
