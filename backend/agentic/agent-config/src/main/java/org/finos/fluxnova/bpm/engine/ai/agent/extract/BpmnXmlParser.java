package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.impl.util.xml.Parse;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Parser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

class BpmnXmlParser extends Parser {

    @Override
    public Parse createParse() {
        return new BpmnXmlParse(this);
    }

    @Override
    protected SAXParser getSaxParser() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // Namespace-aware parsing is required for agent:config lookup via elementNS(...).
        factory.setNamespaceAware(true);
        setXxeProcessing(factory);
        return factory.newSAXParser();
    }
}
