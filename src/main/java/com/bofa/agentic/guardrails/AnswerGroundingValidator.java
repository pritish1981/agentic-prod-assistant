package com.bofa.agentic.guardrails;

import org.springframework.stereotype.Component;
/*This is HUGE for hallucination prevention.
Rule:

If the answer is not supported by RAG/tool evidence → BLOCK IT.
*/

@Component
public class AnswerGroundingValidator {

	public GuardrailResult validate(String response, String evidence) {

		if (evidence == null || evidence.isBlank()) {

			return GuardrailResult.block("No supporting evidence found for the generated answer.");
		}

       // Simple heuristic (upgrade later with semantic similarity)
		if (response.length() > evidence.length() * 3) {

			return GuardrailResult.block("Response appears ungrounded and potentially hallucinated.");
		}

		return GuardrailResult.allow(0.90);
	}

}
