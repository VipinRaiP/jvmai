package dev.jvmai.llm.graph;

import org.bsc.langgraph4j.action.NodeAction;
import dev.jvmai.llm.LlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

public class MemoryNode implements NodeAction<JvmaiState> {

    private final LlmService llm;
    private final ObjectMapper mapper;

    public MemoryNode(LlmService llm) {
        this.llm = llm;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public Map<String, Object> apply(JvmaiState state) {
        try {
            System.out.println("  [Agent:Memory] Analyzing heap and GC usage...");
            String jsonPayload = mapper.writeValueAsString(state.diagnostics().memory());
            
            String prompt = "You are a Memory Specialist JVM Agent. Analyze these memory and GC metrics. " +
                            "Output ONLY A STRING explaining whether there is memory pressure, " +
                            "if the heap usage percentage is problematic, and if there is GC overhead/thrashing. " +
                            "Do NOT output recommendations or root causes yet. Return ONLY your specialized findings.\n" +
                            "JVM Uptime (ms): " + state.diagnostics().metadata().uptimeMillis() + "\n" +
                            "Memory State: " + jsonPayload;
                            
            String result = llm.askQuestion(prompt);
            System.out.println("  [Agent:Memory] Complete.");
            return Map.of(JvmaiState.MEMORY_ANALYSIS, result);
        } catch (Exception e) {
            System.err.println("Memory Agent failed: " + e.getMessage());
            return Map.of(JvmaiState.MEMORY_ANALYSIS, "Error in Memory Analysis: " + e.getMessage());
        }
    }
}
