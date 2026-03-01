package dev.jvmai.llm.graph;

import org.bsc.langgraph4j.action.NodeAction;
import dev.jvmai.llm.LlmService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

public class ThreadNode implements NodeAction<JvmaiState> {

    private final LlmService llm;
    private final ObjectMapper mapper;

    public ThreadNode(LlmService llm) {
        this.llm = llm;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Override
    public Map<String, Object> apply(JvmaiState state) {
        try {
            System.out.println("  [Agent:Thread] Analyzing thread dump...");
            String jsonPayload = mapper.writeValueAsString(state.diagnostics().threads());
            
            String prompt = "You are a Thread Specialist JVM Agent. Analyze these thread metrics. " +
                            "Output ONLY A STRING explaining if there are deadlocks (and their severity), " +
                            "if there is high thread contention, or if thread queues are healthy. " +
                            "Do NOT output recommendations or root causes yet. Return ONLY your specialized findings.\n" +
                            "Thread State: " + jsonPayload;
                            
            String result = llm.askQuestion(prompt);
            System.out.println("  [Agent:Thread] Complete.");
            return Map.of(JvmaiState.THREAD_ANALYSIS, result);
        } catch (Exception e) {
            System.err.println("Thread Agent failed: " + e.getMessage());
            return Map.of(JvmaiState.THREAD_ANALYSIS, "Error in Thread Analysis: " + e.getMessage());
        }
    }
}
