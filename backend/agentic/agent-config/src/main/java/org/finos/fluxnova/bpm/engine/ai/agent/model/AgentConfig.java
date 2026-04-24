package org.finos.fluxnova.bpm.engine.ai.agent.model;

/**
 * Immutable agent configuration extracted from a BPMN element.
 *
 * @param processDefinitionId process definition that owns the configured element
 * @param elementId BPMN element id carrying the agent configuration
 * @param toolScopeElementId BPMN element id that defines the tool-resolution scope; when omitted
 *                           in BPMN, this defaults to {@code elementId}
 */
public record AgentConfig(
    String processDefinitionId,
    String elementId,
    String provider,
    String model,
    String systemPrompt,
    String toolScopeElementId
) {}
