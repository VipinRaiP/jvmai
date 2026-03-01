package dev.jvmai.llm.graph;

import org.bsc.langgraph4j.action.NodeAction;
import dev.jvmai.llm.LlmService;

import java.util.Map;

public class SynthesisNode implements NodeAction<JvmaiState> {

    private final LlmService llm;

    public SynthesisNode(LlmService llm) {
        this.llm = llm;
    }

    @Override
    public Map<String, Object> apply(JvmaiState state) {
        try {
            System.out.println("  [Agent:Synthesis] Synthesizing reports into Root Cause Analysis...");
            
            String prompt = "You are the Lead Synthesis JVM Agent. You must review the findings from your two specialist agents:\n\n" +
                            "--- THREAD AGENT FINDINGS ---\n" +
                            state.threadAnalysis() + "\n\n" +
                            "--- MEMORY AGENT FINDINGS ---\n" +
                            state.memoryAnalysis() + "\n\n" +
                            "Your job is to synthesize these findings into a cohesive JSON Root Cause Analysis. " +
                            "Do not contradict the specialists. " +
                            "Output EXACTLY and ONLY valid JSON matching this schema:\n" +
                            "{\n" +
                            "  \"rootCauseAnalysis\": {\n" +
                            "    \"description\": \"Overall root cause merging thread and memory insights\",\n" +
                            "    \"confidenceLevel\": 90,\n" +
                            "    \"severity\": \"High/Medium/Low\"\n" +
                            "  },\n" +
                            "  \"recommendedActions\": [\n" +
                            "    {\n" +
                            "      \"action\": \"specific action 1\",\n" +
                            "      \"probability\": 85\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}";
                            
            String result = llm.askQuestion(prompt);
            
            // Clean markdown JSON formatting if the model wrapped it
            if (result.startsWith("```json")) {
                result = result.substring(7);
                if (result.endsWith("```")) {
                    result = result.substring(0, result.length() - 3);
                }
            } else if (result.startsWith("```")) {
                result = result.substring(3);
                if (result.endsWith("```")) {
                    result = result.substring(0, result.length() - 3);
                }
            }
            
            System.out.println("  [Agent:Synthesis] Complete.");
            return Map.of(JvmaiState.FINAL_REASONING, result.trim());
        } catch (Exception e) {
            System.err.println("Synthesis Agent failed: " + e.getMessage());
            return Map.of(JvmaiState.FINAL_REASONING, "{\"error\": \"Failed to synthesize: " + e.getMessage() + "\"}");
        }
    }
}
