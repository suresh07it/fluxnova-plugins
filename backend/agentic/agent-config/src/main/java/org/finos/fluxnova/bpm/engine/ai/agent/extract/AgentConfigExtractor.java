package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.ai.agent.model.AgentConfig;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Element;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Namespace;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Parse;
import org.finos.fluxnova.bpm.model.bpmn.AdHocOrdering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AgentConfigExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(AgentConfigExtractor.class);

    static final Namespace AGENT_NS = new Namespace("http://fluxnova.finos.org/schema/1.0/ai/agent");

    public List<AgentConfig> extractAll(InputStream bpmnXml, String processDefinitionId) {
        List<AgentConfig> results = new ArrayList<>();
        Parse parse = new BpmnXmlParser().createParse().sourceInputStream(bpmnXml).execute();
        Element root = parse.getRootElement();
        for (Element process : root.elements("process")) {
            collectAdHocSubProcesses(process, processDefinitionId, results);
        }
        return results;
    }

    private void collectAdHocSubProcesses(Element parent, String processDefinitionId, List<AgentConfig> results) {
        for (Element adHocSubProcess : parent.elements("adHocSubProcess")) {
            extract(adHocSubProcess, processDefinitionId).ifPresent(results::add);
        }
        for (Element subProcess : parent.elements("subProcess")) {
            collectAdHocSubProcesses(subProcess, processDefinitionId, results);
        }
    }

    public Optional<AgentConfig> extract(Element element, String processDefinitionId) {
        Element ext = element.element("extensionElements");
        if (ext == null) {
            return Optional.empty();
        }

        Element config = ext.elementNS(AGENT_NS, "config");
        if (config == null) {
            return Optional.empty();
        }

        String elementId = element.attribute("id");
        String provider = config.attribute("provider");
        String model = config.attribute("model");
        String systemPrompt = config.attribute("systemPrompt");

        if (provider == null || model == null || systemPrompt == null) {
            LOG.warn("agent:config on element '{}' is missing required attribute(s) (provider, model, systemPrompt) - skipping", elementId);
            return Optional.empty();
        }

        String toolScope = config.attribute("toolScope");
        if (toolScope == null) {
            toolScope = elementId;
        }

        String orderingAttr = element.attribute("ordering");
        AdHocOrdering ordering = parseOrdering(orderingAttr, elementId);

        return Optional.of(new AgentConfig(processDefinitionId, elementId, provider, model, systemPrompt, toolScope, ordering));
    }

    private AdHocOrdering parseOrdering(String value, String elementId) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(AdHocOrdering.values())
                .filter(v -> v.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseGet(() -> {
                    LOG.warn("agent:config on element '{}' has unrecognised ordering '{}' - ignoring", elementId, value);
                    return null;
                });
    }
}
