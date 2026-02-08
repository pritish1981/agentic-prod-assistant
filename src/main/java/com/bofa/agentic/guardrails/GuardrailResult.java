package com.bofa.agentic.guardrails;

public class GuardrailResult {
	
	private boolean allowed;
    private String reason;
    private double confidence;

    public GuardrailResult(boolean allowed, String reason, double confidence) {
        this.allowed = allowed;
        this.reason = reason;
        this.confidence = confidence;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }

    public double getConfidence() {
        return confidence;
    }

    public static GuardrailResult allow(double confidence) {
        return new GuardrailResult(true, null, confidence);
    }

    public static GuardrailResult block(String reason) {
        return new GuardrailResult(false, reason, 0.0);
    }

}
