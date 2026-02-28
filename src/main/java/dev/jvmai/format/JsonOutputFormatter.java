package dev.jvmai.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonOutputFormatter implements OutputFormatter {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String format(Diagnostics diagnostics, List<RuleResult> ruleResults, String llmReasoning) {
        Map<String, Object> report = new HashMap<>();
        report.put("diagnostics", diagnostics);
        report.put("ruleResults", ruleResults);
        if (llmReasoning != null) {
            
            // Attempt to parse LLM internal JSON, otherwise keep as string
            try {
                Map<?, ?> llmData = MAPPER.readValue(llmReasoning, Map.class);
                report.put("llmReasoning", llmData);
            } catch (Exception e) {
                report.put("llmReasoning", llmReasoning);
            }
        }

        try {
            return MAPPER.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to format JSON output", e);
        }
    }
}
