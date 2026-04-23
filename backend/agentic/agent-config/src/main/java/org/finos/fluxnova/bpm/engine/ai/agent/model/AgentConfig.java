package org.finos.fluxnova.bpm.engine.ai.agent.model;

import org.finos.fluxnova.bpm.model.bpmn.AdHocOrdering;

public record AgentConfig(
    String processDefinitionId,
    String elementId,
    String provider,
    String model,
    String systemPrompt,
    String toolScope,
    AdHocOrdering ordering
) {}
