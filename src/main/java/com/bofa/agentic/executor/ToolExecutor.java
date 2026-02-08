package com.bofa.agentic.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bofa.agentic.model.ToolCall;
import com.bofa.agentic.orchestrator.ExecutionPlan;

@Service
public class ToolExecutor {

	private final List<AgentTool> tools;
	private final ParallelExecutor parallelExecutor;

	public ToolExecutor(List<AgentTool> tools, ParallelExecutor parallelExecutor) {
		this.tools = tools;
		this.parallelExecutor = parallelExecutor;
	}

	/**
	 * Registry for tool lookup
	 */
	private Map<String, AgentTool> toolRegistry;

	@jakarta.annotation.PostConstruct
	public void init() {

		toolRegistry = tools.stream().collect(Collectors.toMap(AgentTool::name, t -> t));
	}

	/**
	 * Execute tools from plan
	 */
	public String execute(ExecutionPlan plan) {

		if (plan.toolCalls() == null || plan.toolCalls().isEmpty()) {
			return "";
		}

		List<CompletableFuture<ExecutionResult>> futures = plan.toolCalls().stream().map(this::invokeAsync).toList();

		List<ExecutionResult> results = parallelExecutor.execute(futures);

		return mergeResults(results);
	}

	private CompletableFuture<ExecutionResult> invokeAsync(ToolCall call) {

		AgentTool tool = toolRegistry.get(call.toolName());

		if (tool == null) {
			return CompletableFuture.completedFuture(
					new ExecutionResult(call.toolName(), "Tool not found", false, true, 0)
			);
		}

		return parallelExecutor.supplyAsync(new ToolInvocation(tool, call.input()));
	}

	private String mergeResults(List<ExecutionResult> results) {

		return results.stream().map(r -> "[" + r.getToolName() + "] -> " + r.getResponse())
				.collect(Collectors.joining("\n"));
	}

	/**
	 * Wrapper class for invocation timing
	 */
	public static class ToolInvocation {

		private final AgentTool tool;
		private final String input;

		public ToolInvocation(AgentTool tool, String input) {
			this.tool = tool;
			this.input = input;
		}

		public ExecutionResult invoke() {

			long start = System.currentTimeMillis();

			try {

				ExecutionResult result = tool.execute(input);

				long latency = System.currentTimeMillis() - start;

				return new ExecutionResult(
						result.getToolName(),
						result.getResponse(),
						result.isSuccess(),
						result.isFailure(),
						latency
				);

			} catch (Exception e) {

				return new ExecutionResult(
						tool.name(),
						e.getMessage(),
						false,
						true,
						System.currentTimeMillis() - start
				);
			}
		}
	}

}
