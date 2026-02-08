package com.bofa.agentic.executor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ParallelExecutor {
	
	private final Executor agentTaskExecutor;

        public ParallelExecutor(Executor agentTaskExecutor) {
                this.agentTaskExecutor = agentTaskExecutor;
        }
	
	/**
     * Execute tool calls in parallel
     */
    public List<ExecutionResult> execute(List<CompletableFuture<ExecutionResult>> futures) {

        CompletableFuture<Void> combined =
                CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );

        combined.join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * Wrap a tool call into async future
     */
    public CompletableFuture<ExecutionResult> supplyAsync(
            ToolExecutor.ToolInvocation invocation) {

        return CompletableFuture.supplyAsync(
                invocation::invoke,
                agentTaskExecutor
        );
    }

}
