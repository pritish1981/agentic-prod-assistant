package com.bofa.agentic.guardrails;

import org.springframework.stereotype.Component;

@Component
public class ConfidenceScorer {

	public double score(String evidence, GuardrailResult safety, GuardrailResult grounding) {

		double base = 0.5;

		if (evidence != null && !evidence.isBlank()) {
			base += 0.2;
		}

		base += safety.getConfidence() * 0.15;
		base += grounding.getConfidence() * 0.15;

		return Math.min(base, 0.99);
	}

}
