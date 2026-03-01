package dev.jvmai.llm.graph;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import dev.jvmai.diagnostic.Diagnostics;

import java.util.Map;

public class JvmaiState extends AgentState {

    public static final String DIAGNOSTICS = "diagnostics";
    public static final String THREAD_ANALYSIS = "thread_analysis";
    public static final String MEMORY_ANALYSIS = "memory_analysis";
    public static final String FINAL_REASONING = "final_reasoning";

    // Relying on default Channel behavior (overwrite/last_value) for these scalar fields
    public static final Map<String, Channel<?>> SCHEMA = Map.of();

    public JvmaiState(Map<String, Object> initData) {
        super(initData);
    }

    public Diagnostics diagnostics() {
        return this.<Diagnostics>value(DIAGNOSTICS).orElse(null);
    }

    public String threadAnalysis() {
        return this.<String>value(THREAD_ANALYSIS).orElse("");
    }

    public String memoryAnalysis() {
        return this.<String>value(MEMORY_ANALYSIS).orElse("");
    }
    
    public String finalReasoning() {
        return this.<String>value(FINAL_REASONING).orElse("");
    }
}
