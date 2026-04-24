package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.impl.util.xml.Element;

interface AgentConfigTargetPolicy {

    boolean supports(Element element);
}