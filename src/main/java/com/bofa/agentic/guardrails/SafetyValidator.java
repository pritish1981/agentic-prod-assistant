package com.bofa.agentic.guardrails;

import java.util.List;

import org.springframework.stereotype.Component;

/*
 * Prevents:

    toxic output
    secrets
    unsafe operational instructions
    policy violations

Later → plug into moderation APIs.
 * 
 */

@Component
public class SafetyValidator {
	
	private static final List<String> BLOCKED_TERMS = List.of(
            "drop database",
            "delete production",
            "expose credentials",
            "shutdown server"
    );

    public GuardrailResult validate(String response) {

        String lower = response.toLowerCase();

        boolean unsafe = BLOCKED_TERMS.stream()
                .anyMatch(lower::contains);

        if (unsafe) {
            return GuardrailResult.block(
                    "Response contains unsafe operational guidance."
            );
        }

        return GuardrailResult.allow(0.95);
    }

}
