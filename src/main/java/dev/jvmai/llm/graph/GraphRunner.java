package dev.jvmai.llm.graph;

import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.StateGraph.END;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.llm.LlmService;

import java.util.Map;

public class GraphRunner {

    private final LlmService llm;

    public GraphRunner(LlmService llm) {
        this.llm = llm;
    }

    public JvmaiState run(Diagnostics diagnostics) throws Exception {
        System.out.println("Starting Multi-Agent Diagnostic Graph (powered by LangGraph4j)...");

        // 1. Initialize Nodes
        ThreadNode threadNode = new ThreadNode(llm);
        MemoryNode memoryNode = new MemoryNode(llm);
        SynthesisNode synthesisNode = new SynthesisNode(llm);

        // 2. Define the StateGraph structure
        var stateGraph = new StateGraph<>(JvmaiState.SCHEMA, JvmaiState::new)
            .addNode("threadAgent", node_async(threadNode))
            .addNode("memoryAgent", node_async(memoryNode))
            .addNode("synthesisAgent", node_async(synthesisNode))
            
            // 3. Define the edges (workflow)
            // Start hands off to both specialists (this simulates parallel or sequential, 
            // depending on edge structure and async wrappers)
            // For simplicity in a strict DAG without custom conditional edges yet, 
            // we chain them sequentially. 
            // (True parallel execution via LangGraph parallel node execution is supported by default if defined as parallel edges from START,
            // but joining requires a Fan-In state. We will do sequential to keep state mutations predictable).
            .addEdge(START, "threadAgent")
            .addEdge("threadAgent", "memoryAgent")
            .addEdge("memoryAgent", "synthesisAgent")
            .addEdge("synthesisAgent", END);

        // 4. Compile the Graph
        CompiledGraph<JvmaiState> compiledGraph = stateGraph.compile();

        // 5. Run it by streaming to the end state
        JvmaiState finalState = null;
        for (org.bsc.langgraph4j.NodeOutput<JvmaiState> output : compiledGraph.stream(Map.of(JvmaiState.DIAGNOSTICS, diagnostics))) {
            finalState = output.state();
        }

        System.out.println("Multi-Agent Graph Complete.");
        return finalState;
    }
}
