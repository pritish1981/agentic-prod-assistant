package com.bofa.agentic.rag;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RagRetriever {
	private static final Logger log = LoggerFactory.getLogger(RagRetriever.class);
	
	private final ElasticVectorStore vectorStore;
	private final List<FaqEntry> faqEntries;
	
	public RagRetriever(Optional<ElasticVectorStore> vectorStore, ObjectMapper objectMapper) {
		this.vectorStore = vectorStore.orElse(null);
		this.faqEntries = loadFaqs(objectMapper);
    }
	
	public String retrieve(String query) {
		String ragResult = "";
		if (vectorStore != null) {
			List<Document> documents = vectorStore.similaritySearch(query);
			if (!documents.isEmpty()) {
				ragResult = documents.stream()
						.map(Document::getText)
						.filter(text -> text != null && !text.isBlank())
						.collect(Collectors.joining("\n---\n"));
			}
		}

		if (!ragResult.isBlank()) {
			return ragResult;
		}

		return lookupFaq(query);

    }

	private List<FaqEntry> loadFaqs(ObjectMapper objectMapper) {
		Path path = Path.of("datasets", "prod-faqs.json");
		if (!Files.exists(path)) {
			log.warn("FAQ dataset not found at {}", path.toAbsolutePath());
			return List.of();
		}

		try {
			String json = Files.readString(path);
			return objectMapper.readValue(json, new TypeReference<List<FaqEntry>>() {});
		} catch (Exception e) {
			log.error("Failed to load FAQ dataset", e);
			return List.of();
		}
	}

	private String lookupFaq(String query) {
		if (faqEntries.isEmpty() || query == null || query.isBlank()) {
			return "";
		}

		String normalized = query.toLowerCase(Locale.ROOT);
		List<FaqEntry> candidates = new ArrayList<>();

		for (FaqEntry entry : faqEntries) {
			if (entry == null || entry.question == null) {
				continue;
			}
			String question = entry.question.toLowerCase(Locale.ROOT);
			if (question.equals(normalized) || question.contains(normalized) || normalized.contains(question)) {
				candidates.add(entry);
			}
		}

		if (candidates.isEmpty()) {
			return "";
		}

		log.info("FAQ fallback used for query: {}", query);
		return candidates.stream()
				.map(this::formatFaq)
				.filter(text -> text != null && !text.isBlank())
				.collect(Collectors.joining("\n---\n"));
	}

	private String formatFaq(FaqEntry entry) {
		String question = entry.question == null ? "" : entry.question;
		String answer = entry.answer == null ? "" : entry.answer;
		String category = entry.category == null ? "" : entry.category;

		if (question.isBlank() && answer.isBlank()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		builder.append("FAQ\n");
		builder.append("Question: ").append(question).append("\n");
		builder.append("Answer: ").append(answer);
		if (!category.isBlank()) {
			builder.append("\nCategory: ").append(category);
		}
		return builder.toString();
	}

	private static class FaqEntry {
		public String question;
		public String answer;
		public String category;
	}

}
