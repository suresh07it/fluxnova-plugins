package org.finos.fluxnova.bpm.agentic.poc;

import org.finos.fluxnova.bpm.engine.RuntimeService;
import org.finos.fluxnova.bpm.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class PocController {

    private final RuntimeService runtimeService;

    public PocController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/poc/start")
    public Map<String, Object> start(@RequestParam(name = "userQuestion", required = false) String userQuestion,
                                     @RequestParam(name = "customerId", required = false) String customerId,
                                     @RequestParam(name = "applicationAmount", required = false) Integer applicationAmount,
                                     @RequestParam(name = "maxTurns", required = false) Integer maxTurns) {

        Map<String, Object> vars = new LinkedHashMap<>();
        if (userQuestion != null) vars.put("userQuestion", userQuestion);
        if (customerId != null) vars.put("customerId", customerId);
        if (applicationAmount != null) vars.put("applicationAmount", applicationAmount);
        if (maxTurns != null) vars.put("_agent.maxTurns", maxTurns);

        Map<String, Object> out = new LinkedHashMap<>();
        try {
            ProcessInstance pi = runtimeService.startProcessInstanceByKey("agenticPoc", vars);
            out.put("processInstanceId", pi.getId());
            out.put("processDefinitionId", pi.getProcessDefinitionId());
            out.put("status", "STARTED");
        } catch (Exception e) {
            out.put("status", "ERROR");
            out.put("error", e.getClass().getName());
            out.put("message", e.getMessage());
        }
        return out;
    }
}
