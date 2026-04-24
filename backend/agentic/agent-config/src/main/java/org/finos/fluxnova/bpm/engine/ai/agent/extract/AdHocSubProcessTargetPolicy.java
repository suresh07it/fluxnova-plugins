package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.impl.util.xml.Element;

class AdHocSubProcessTargetPolicy implements AgentConfigTargetPolicy {

    @Override
    public boolean supports(Element element) {
        return "adHocSubProcess".equals(element.getTagName());
    }
}