package com.bofa.agentic.guardrails;

import org.springframework.stereotype.Component;

import com.bofa.agentic.exception.AgentException;

@Component
public class HallucinationGuard {

	private final SafetyValidator safetyValidator;
	private final AnswerGroundingValidator groundingValidator;
	private final ConfidenceScorer confidenceScorer;

	public HallucinationGuard(SafetyValidator safetyValidator, AnswerGroundingValidator groundingValidator,
			ConfidenceScorer confidenceScorer) {
		this.safetyValidator = safetyValidator;
		this.groundingValidator = groundingValidator;
		this.confidenceScorer = confidenceScorer;
	}

	public double validateOrThrow(String response, String evidence) {

		GuardrailResult safety = safetyValidator.validate(response);

		if (!safety.isAllowed()) {
			throw new AgentException("SAFETY_GUARDRAIL_BLOCKED", safety.getReason());
		}

		GuardrailResult grounding = groundingValidator.validate(response, evidence);

		if (!grounding.isAllowed()) {
			throw new AgentException("GROUNDING_GUARDRAIL_BLOCKED", grounding.getReason());
		}

		return confidenceScorer.score(evidence, safety, grounding);
	}

}

/*
LLM Response
↓
SafetyValidator        → blocks harmful output
↓
AnswerGroundingValidator → prevents hallucination
↓
ConfidenceScorer       → scores reliability
↓
HallucinationGuard     → final gatekeeper

*/

