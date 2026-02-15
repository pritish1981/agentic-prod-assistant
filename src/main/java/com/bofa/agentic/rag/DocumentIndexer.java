package com.bofa.agentic.rag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
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
	
	private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    public DocumentIndexer(Optional<VectorStore> vectorStore, ObjectMapper objectMapper) {
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
                log.info("Skipping document indexing: VectorStore not available.");
                return;
            }

            log.info("Starting document indexing...");

            List<Document> documents = new ArrayList<>();

            indexFaqs(documents);
            indexIncidents(documents);
            indexRunbooks(documents);
            indexSampleIncidentData(documents);

            if (documents.isEmpty()) {
                log.warn("No documents found to index.");
                return;
            }

            vectorStore.add(documents);

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

    private void indexIncidents(List<Document> documents) {
        Path path = Path.of("datasets", "incidents.json");
        if (!Files.exists(path)) {
            log.warn("Incidents dataset not found at {}", path.toAbsolutePath());
            return;
        }

        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                Files.readString(path),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> item : items) {
                String incidentId = String.valueOf(item.getOrDefault("incidentId", ""));
                String title = String.valueOf(item.getOrDefault("title", ""));
                String severity = String.valueOf(item.getOrDefault("severity", ""));
                String service = String.valueOf(item.getOrDefault("service", ""));
                String environment = String.valueOf(item.getOrDefault("environment", ""));
                String description = String.valueOf(item.getOrDefault("description", ""));
                String rootCause = String.valueOf(item.getOrDefault("rootCause", ""));
                String resolution = String.valueOf(item.getOrDefault("resolution", ""));
                String status = String.valueOf(item.getOrDefault("status", ""));

                if (incidentId.isBlank() && title.isBlank()) {
                    continue;
                }

                // Create searchable content with all fields
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("INCIDENT\n");
                contentBuilder.append("ID: ").append(incidentId).append("\n");
                contentBuilder.append("Title: ").append(title).append("\n");
                contentBuilder.append("Severity: ").append(severity).append("\n");
                contentBuilder.append("Service: ").append(service).append("\n");
                contentBuilder.append("Environment: ").append(environment).append("\n");
                contentBuilder.append("Description: ").append(description).append("\n");
                contentBuilder.append("Root Cause: ").append(rootCause).append("\n");
                contentBuilder.append("Resolution: ").append(resolution).append("\n");
                contentBuilder.append("Status: ").append(status);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "incident");
                metadata.put("incidentId", incidentId);
                metadata.put("severity", severity);
                metadata.put("service", service);
                metadata.put("environment", environment);

                documents.add(new Document(contentBuilder.toString(), metadata));
            }
            
            log.info("Loaded {} incidents for indexing", items.size());
        } catch (Exception e) {
            log.error("Failed to index incidents dataset", e);
        }
    }

    private void indexRunbooks(List<Document> documents) {
        Path path = Path.of("datasets", "runbooks.json");
        if (!Files.exists(path)) {
            log.warn("Runbooks dataset not found at {}", path.toAbsolutePath());
            return;
        }

        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                Files.readString(path),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> item : items) {
                String runbookId = String.valueOf(item.getOrDefault("runbookId", ""));
                String title = String.valueOf(item.getOrDefault("title", ""));
                Boolean automationPossible = (Boolean) item.getOrDefault("automationPossible", false);
                
                @SuppressWarnings("unchecked")
                List<String> steps = (List<String>) item.getOrDefault("steps", new ArrayList<>());

                if (runbookId.isBlank() && title.isBlank()) {
                    continue;
                }

                // Create searchable content with all fields
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("RUNBOOK\n");
                contentBuilder.append("ID: ").append(runbookId).append("\n");
                contentBuilder.append("Title: ").append(title).append("\n");
                contentBuilder.append("Automation Possible: ").append(automationPossible).append("\n");
                contentBuilder.append("Steps:\n");
                
                for (int i = 0; i < steps.size(); i++) {
                    contentBuilder.append((i + 1)).append(". ").append(steps.get(i)).append("\n");
                }

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("type", "runbook");
                metadata.put("runbookId", runbookId);
                metadata.put("automationPossible", automationPossible);

                documents.add(new Document(contentBuilder.toString(), metadata));
            }
            
            log.info("Loaded {} runbooks for indexing", items.size());
        } catch (Exception e) {
            log.error("Failed to index runbooks dataset", e);
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
