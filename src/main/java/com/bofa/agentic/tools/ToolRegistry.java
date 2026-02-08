package com.bofa.agentic.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {
	
	private final Map<String, AgentTool> toolMap;

    public ToolRegistry(List<AgentTool> tools) {
        this.toolMap = tools.stream()
                .collect(Collectors.toMap(AgentTool::name, t -> t));
    }

    public AgentTool getTool(String name) {
        return toolMap.get(name);
    }

}
