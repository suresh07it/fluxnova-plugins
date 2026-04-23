package org.finos.fluxnova.bpm.engine.ai.agent.extract;

import org.finos.fluxnova.bpm.engine.impl.util.xml.Element;
import org.finos.fluxnova.bpm.engine.impl.util.xml.Parse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BpmnXmlParserTest {

    private static final String MINIMAL_BPMN = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
              <process id="testProcess"/>
            </definitions>
            """;

    @Test
    void parse_returnsRootElementWithCorrectTagName() {
        BpmnXmlParser parser = new BpmnXmlParser();
        Parse parse = parser.createParse()
                .sourceInputStream(new ByteArrayInputStream(MINIMAL_BPMN.getBytes(StandardCharsets.UTF_8)))
                .execute();

        Element root = parse.getRootElement();

        assertNotNull(root);
        assertEquals("definitions", root.getTagName());
    }

    @Test
    void parse_navigatesToChildElement() {
        BpmnXmlParser parser = new BpmnXmlParser();
        Parse parse = parser.createParse()
                .sourceInputStream(new ByteArrayInputStream(MINIMAL_BPMN.getBytes(StandardCharsets.UTF_8)))
                .execute();

        Element process = parse.getRootElement().element("process");

        assertNotNull(process);
        assertEquals("testProcess", process.attribute("id"));
    }
}
