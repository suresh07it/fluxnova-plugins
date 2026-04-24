package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.ai.agent.model.AgentConfig;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Element;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Parse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AgentConfigExtractorTest {

    private static final String PROCESS_DEFINITION_ID = "proc:1";

    private AgentConfigExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AgentConfigExtractor();
    }

    private Element parseAdHocSubProcess(String bpmn) {
        BpmnXmlParser parser = new BpmnXmlParser();
        Parse parse = parser.createParse()
                .sourceInputStream(new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)))
                .execute();
        return parse.getRootElement()
                .element("process")
                .element("adHocSubProcess");
    }

    @Test
    void extract_whenNoExtensionElements_returnsEmpty() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="p">
                    <adHocSubProcess id="sub1"/>
                  </process>
                </definitions>
                """;

        Optional<AgentConfig> result = extractor.extract(parseAdHocSubProcess(bpmn), PROCESS_DEFINITION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void extract_whenExtensionElementsButNoAgentConfig_returnsEmpty() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:other="http://example.com/other">
                  <process id="p">
                    <adHocSubProcess id="sub1">
                      <extensionElements>
                        <other:config provider="x"/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        Optional<AgentConfig> result = extractor.extract(parseAdHocSubProcess(bpmn), PROCESS_DEFINITION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void extract_whenValidAgentConfig_returnsPopulatedAgentConfig() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <adHocSubProcess id="creditCheckAgent">
                      <extensionElements>
                        <agent:config provider="anthropic"
                                      model="claude-sonnet-4-6"
                                      systemPrompt="You are a credit analyst."
                                      toolScopeElementId="creditCheckAgent"/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        Optional<AgentConfig> result = extractor.extract(parseAdHocSubProcess(bpmn), PROCESS_DEFINITION_ID);

        assertTrue(result.isPresent());
        AgentConfig config = result.get();
        assertEquals(PROCESS_DEFINITION_ID, config.processDefinitionId());
        assertEquals("creditCheckAgent", config.elementId());
        assertEquals("anthropic", config.provider());
        assertEquals("claude-sonnet-4-6", config.model());
        assertEquals("You are a credit analyst.", config.systemPrompt());
        assertEquals("creditCheckAgent", config.toolScopeElementId());
    }

    @Test
    void extract_whenToolScopeAbsent_defaultsToElementId() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <adHocSubProcess id="myAgent">
                      <extensionElements>
                        <agent:config provider="openai"
                                      model="gpt-4o"
                                      systemPrompt="You are an assistant."/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        Optional<AgentConfig> result = extractor.extract(parseAdHocSubProcess(bpmn), PROCESS_DEFINITION_ID);

        assertTrue(result.isPresent());
        assertEquals("myAgent", result.get().toolScopeElementId());
    }

    @Test
    void extract_whenRequiredAttributeMissing_returnsEmpty() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <adHocSubProcess id="myAgent">
                      <extensionElements>
                        <agent:config model="claude-sonnet-4-6"
                                      systemPrompt="You are an assistant."/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        Optional<AgentConfig> result = extractor.extract(parseAdHocSubProcess(bpmn), PROCESS_DEFINITION_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void extractAll_findsAgentConfigOnServiceTask() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <serviceTask id="taskAgent">
                      <extensionElements>
                        <agent:config provider="anthropic"
                                      model="claude-sonnet-4-6"
                                      systemPrompt="Task agent."/>
                      </extensionElements>
                    </serviceTask>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

  assertEquals(1, results.size());
  assertEquals("taskAgent", results.get(0).elementId());
  assertEquals("taskAgent", results.get(0).toolScopeElementId());
    }

    @Test
    void extractAll_findsAdHocSubProcessInsideSubProcess() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <subProcess id="outer">
                      <adHocSubProcess id="nestedAgent">
                        <extensionElements>
                          <agent:config provider="anthropic"
                                        model="claude-sonnet-4-6"
                                        systemPrompt="Nested agent."/>
                        </extensionElements>
                      </adHocSubProcess>
                    </subProcess>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

        assertEquals(1, results.size());
        assertEquals("nestedAgent", results.get(0).elementId());
    }

    @Test
    void extractAll_findsAdHocSubProcessInsideEventSubProcess() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <subProcess id="eventSub" triggeredByEvent="true">
                      <adHocSubProcess id="eventAgent">
                        <extensionElements>
                          <agent:config provider="openai"
                                        model="gpt-4o"
                                        systemPrompt="Event agent."/>
                        </extensionElements>
                      </adHocSubProcess>
                    </subProcess>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

        assertEquals(1, results.size());
        assertEquals("eventAgent", results.get(0).elementId());
    }

    @Test
    void extractAll_findsAdHocSubProcessInsideTransaction() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <transaction id="tx1">
                      <adHocSubProcess id="transactionAgent">
                        <extensionElements>
                          <agent:config provider="anthropic"
                                        model="claude-sonnet-4-6"
                                        systemPrompt="Transaction agent."/>
                        </extensionElements>
                      </adHocSubProcess>
                    </transaction>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

        assertEquals(1, results.size());
        assertEquals("transactionAgent", results.get(0).elementId());
    }

    @Test
    void extractAll_findsMultipleAdHocSubProcessesInOneProcess() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p">
                    <adHocSubProcess id="agentA">
                      <extensionElements>
                        <agent:config provider="anthropic" model="claude-sonnet-4-6" systemPrompt="Agent A."/>
                      </extensionElements>
                    </adHocSubProcess>
                    <adHocSubProcess id="agentB">
                      <extensionElements>
                        <agent:config provider="openai" model="gpt-4o" systemPrompt="Agent B."/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

        assertEquals(2, results.size());
        assertEquals("agentA", results.get(0).elementId());
        assertEquals("agentB", results.get(1).elementId());
    }

    @Test
    void extractAll_findsAgentsAcrossMultipleProcesses() {
        String bpmn = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:agent="http://fluxnova.finos.org/schema/1.0/ai/agent">
                  <process id="p1">
                    <adHocSubProcess id="agentP1">
                      <extensionElements>
                        <agent:config provider="anthropic" model="claude-sonnet-4-6" systemPrompt="P1 agent."/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                  <process id="p2">
                    <adHocSubProcess id="agentP2">
                      <extensionElements>
                        <agent:config provider="openai" model="gpt-4o" systemPrompt="P2 agent."/>
                      </extensionElements>
                    </adHocSubProcess>
                  </process>
                </definitions>
                """;

        List<AgentConfig> results = extractor.extractAll(
                new ByteArrayInputStream(bpmn.getBytes(StandardCharsets.UTF_8)), PROCESS_DEFINITION_ID);

        assertEquals(2, results.size());
        assertEquals("agentP1", results.get(0).elementId());
        assertEquals("agentP2", results.get(1).elementId());
    }
}
