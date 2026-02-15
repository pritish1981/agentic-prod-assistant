package com.bofa.agentic.guardrails;

import java.util.List;
import java.util.Map;

/**
 * Represents the response from OpenAI Moderation API
 * 
 * API Response Structure:
 * {
 *   "id": "modr-xxx",
 *   "model": "text-moderation-007",
 *   "results": [
 *     {
 *       "flagged": true,
 *       "categories": {
 *         "sexual": false,
 *         "hate": true,
 *         "harassment": false,
 *         "self-harm": false,
 *         "sexual/minors": false,
 *         "hate/threatening": false,
 *         "violence/graphic": false,
 *         "self-harm/intent": false,
 *         "self-harm/instructions": false,
 *         "harassment/threatening": false,
 *         "violence": false
 *       },
 *       "category_scores": {
 *         "sexual": 0.0001,
 *         "hate": 0.9999,
 *         ...
 *       }
 *     }
 *   ]
 * }
 */
public class ModerationResult {
    
    private String id;
    private String model;
    private List<Result> results;
    
    public static class Result {
        private boolean flagged;
        private Map<String, Boolean> categories;
        private Map<String, Double> categoryScores;
        
        public boolean isFlagged() {
            return flagged;
        }
        
        public void setFlagged(boolean flagged) {
            this.flagged = flagged;
        }
        
        public Map<String, Boolean> getCategories() {
            return categories;
        }
        
        public void setCategories(Map<String, Boolean> categories) {
            this.categories = categories;
        }
        
        public Map<String, Double> getCategoryScores() {
            return categoryScores;
        }
        
        public void setCategoryScores(Map<String, Double> categoryScores) {
            this.categoryScores = categoryScores;
        }
        
        public String getFlaggedCategories() {
            if (categories == null || !flagged) {
                return "none";
            }
            return categories.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .map(Map.Entry::getKey)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("none");
        }
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public List<Result> getResults() {
        return results;
    }
    
    public void setResults(List<Result> results) {
        this.results = results;
    }
    
    public boolean isFlagged() {
        return results != null && !results.isEmpty() && results.get(0).isFlagged();
    }
    
    public String getFlaggedCategories() {
        if (results == null || results.isEmpty()) {
            return "none";
        }
        return results.get(0).getFlaggedCategories();
    }
}
