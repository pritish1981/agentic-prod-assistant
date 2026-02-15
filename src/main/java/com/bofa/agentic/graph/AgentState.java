package com.bofa.agentic.graph;

import com.bofa.agentic.model.ChatRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State object that flows through the agent graph
 * 
 * This is the "state" in the state machine - it accumulates
 * information as it flows through nodes (Planner → RAG → Tools → Composer → Guard)
 * 
 * Inspired by LangGraph's state management pattern
 */
public class AgentState {
    
    private final ChatRequest request;
    private String currentNode;
    private String nextNode;
    
    // Planning stage
    private boolean useRag;
    private boolean useTools;
    private List<String> toolNames;
    
    // Data collection stage
    private String ragEvidence;
    private String toolResults;
    
    // Response generation stage
    private String response;
    
    // Guardrail stage
    private boolean guardrailPassed;
    private String guardrailReason;
    
    // Reflection/retry logic
    private int retryCount;
    private List<String> executedNodes;
    
    // Metadata for observability
    private Map<String, Object> metadata;
    private long startTime;
    
    public AgentState(ChatRequest request) {
        this.request = request;
        this.currentNode = "START";
        this.nextNode = "planner";
        this.toolNames = new ArrayList<>();
        this.executedNodes = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.retryCount = 0;
        this.startTime = System.currentTimeMillis();
    }
    
    // Copy constructor for immutable state transitions
    public AgentState(AgentState other) {
        this.request = other.request;
        this.currentNode = other.currentNode;
        this.nextNode = other.nextNode;
        this.useRag = other.useRag;
        this.useTools = other.useTools;
        this.toolNames = new ArrayList<>(other.toolNames);
        this.ragEvidence = other.ragEvidence;
        this.toolResults = other.toolResults;
        this.response = other.response;
        this.guardrailPassed = other.guardrailPassed;
        this.guardrailReason = other.guardrailReason;
        this.retryCount = other.retryCount;
        this.executedNodes = new ArrayList<>(other.executedNodes);
        this.metadata = new HashMap<>(other.metadata);
        this.startTime = other.startTime;
    }
    
    // State transition methods
    public AgentState moveTo(String nodeName) {
        AgentState newState = new AgentState(this);
        newState.currentNode = this.nextNode;
        newState.nextNode = nodeName;
        newState.executedNodes.add(this.nextNode);
        return newState;
    }
    
    public AgentState withRagDecision(boolean useRag) {
        AgentState newState = new AgentState(this);
        newState.useRag = useRag;
        return newState;
    }
    
    public AgentState withToolsDecision(boolean useTools, List<String> toolNames) {
        AgentState newState = new AgentState(this);
        newState.useTools = useTools;
        newState.toolNames = toolNames != null ? new ArrayList<>(toolNames) : new ArrayList<>();
        return newState;
    }
    
    public AgentState withRagEvidence(String evidence) {
        AgentState newState = new AgentState(this);
        newState.ragEvidence = evidence;
        return newState;
    }
    
    public AgentState withToolResults(String results) {
        AgentState newState = new AgentState(this);
        newState.toolResults = results;
        return newState;
    }
    
    public AgentState withResponse(String response) {
        AgentState newState = new AgentState(this);
        newState.response = response;
        return newState;
    }
    
    public AgentState withGuardrailResult(boolean passed, String reason) {
        AgentState newState = new AgentState(this);
        newState.guardrailPassed = passed;
        newState.guardrailReason = reason;
        return newState;
    }
    
    public AgentState incrementRetry() {
        AgentState newState = new AgentState(this);
        newState.retryCount = this.retryCount + 1;
        return newState;
    }
    
    public AgentState addMetadata(String key, Object value) {
        AgentState newState = new AgentState(this);
        newState.metadata.put(key, value);
        return newState;
    }
    
    // Getters
    public ChatRequest getRequest() {
        return request;
    }
    
    public String getCurrentNode() {
        return currentNode;
    }
    
    public String getNextNode() {
        return nextNode;
    }
    
    public boolean isUseRag() {
        return useRag;
    }
    
    public boolean isUseTools() {
        return useTools;
    }
    
    public List<String> getToolNames() {
        return toolNames;
    }
    
    public String getRagEvidence() {
        return ragEvidence != null ? ragEvidence : "";
    }
    
    public String getToolResults() {
        return toolResults != null ? toolResults : "";
    }
    
    public String getResponse() {
        return response;
    }
    
    public boolean isGuardrailPassed() {
        return guardrailPassed;
    }
    
    public String getGuardrailReason() {
        return guardrailReason;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public List<String> getExecutedNodes() {
        return executedNodes;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    public boolean isTerminal() {
        return "END".equals(nextNode) || "ERROR".equals(nextNode);
    }
    
    public boolean canRetry() {
        return retryCount < 2; // Max 2 retries
    }
}
