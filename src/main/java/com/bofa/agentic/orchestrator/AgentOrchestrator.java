package com.bofa.agentic.orchestrator;

import org.springframework.stereotype.Service;

import com.bofa.agentic.executor.ToolExecutor;
import com.bofa.agentic.guardrails.HallucinationGuard;
import com.bofa.agentic.model.ChatRequest;
import com.bofa.agentic.model.ExecutionContext;
import com.bofa.agentic.rag.RagRetriever;

import reactor.core.publisher.Flux;

@Service
public class AgentOrchestrator {
	
	private final Planner planner;
    private final RagRetriever ragRetriever;
    private final ToolExecutor toolExecutor;
    private final ResponseComposer composer;
    private final HallucinationGuard guard;

    public AgentOrchestrator(Planner planner, RagRetriever ragRetriever, ToolExecutor toolExecutor,
            ResponseComposer composer, HallucinationGuard guard) {
        this.planner = planner;
        this.ragRetriever = ragRetriever;
        this.toolExecutor = toolExecutor;
        this.composer = composer;
        this.guard = guard;
    }
    
    public Flux<String> process(ChatRequest request) {

        ExecutionContext context = new ExecutionContext(request);

        ExecutionPlan plan = planner.plan(context);

        String evidence = "";

        if (plan.useRag()) {
            evidence = ragRetriever.retrieve(request.message());
        }

        String toolResults = toolExecutor.execute(plan);

        String response = composer.compose(
                request.message(),
                evidence,
                toolResults
        );

        guard.validateOrThrow(response, evidence);

        return Flux.just(response);
    }

}
