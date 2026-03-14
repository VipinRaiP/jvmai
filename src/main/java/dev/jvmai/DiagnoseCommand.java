package dev.jvmai;

import dev.jvmai.diagnostic.DiagnosticCollector;
import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.diagnostic.JvmConnection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "diagnose", description = "Attaches to a JVM process and collects diagnostics.")
public class DiagnoseCommand implements Callable<Integer> {

    @Option(names = {"-p", "--pid"}, description = "Target JVM Process ID (local attachment)")
    private Long pid;
    
    @Option(names = {"--host"}, description = "Target JVM Hostname (remote JMX attachment)")
    private String host;

    @Option(names = {"--port"}, description = "Target JVM JMX Port (remote JMX attachment)")
    private Integer port;

    @Option(names = {"--no-llm"}, description = "Disable LLM reasoning")
    private boolean noLlm;

    @Option(names = {"--interactive"}, description = "Enter interactive Q&A mode after diagnostics")
    private boolean interactive;

    @Option(names = {"--model"}, description = "LLM model to use (default: llama3)")
    private String model = "llama3";

    @Option(names = {"--output"}, description = "Output format (text, json, markdown)", defaultValue = "text")
    private String output;

    @Option(names = {"--timeout"}, description = "LLM API timeout in seconds", defaultValue = "30")
    private int timeout;

    @Option(names = {"--redact"}, description = "Redact sensitive data before sending to LLM")
    private boolean redact;

    @Override
    public Integer call() throws Exception {
        boolean usePid = pid != null;
        boolean useRemote = host != null && port != null;

        if (usePid && useRemote) {
            System.err.println("Error: Cannot specify both local PID and remote Host/Port. Please use either --pid OR --host and --port.");
            return 1;
        } else if (!usePid && !useRemote) {
            System.err.println("Error: Must specify either a local JVM PID (--pid) or a remote JVM connection (--host AND --port).");
            return 1;
        } else if (host != null && port == null || host == null && port != null) {
            System.err.println("Error: Both --host and --port must be provided for remote connections.");
            return 1;
        }

        Diagnostics diagnostics;
        try {
            JvmConnection connection;
            if (usePid) {
                 System.out.println("Attaching to local JVM with PID: " + pid + "...");
                 connection = new JvmConnection(pid);
            } else {
                 System.out.println("Connecting to remote JVM at " + host + ":" + port + "...");
                 connection = new JvmConnection(host, port);
            }
            
            try (connection) {
                System.out.println("Successfully connected to JVM. Collecting diagnostics...");
                DiagnosticCollector collector = new DiagnosticCollector(connection.getConnection());
                diagnostics = collector.collect();
                System.out.println("Diagnostics collected for JVM: " + diagnostics.metadata().name() + " (" + diagnostics.metadata().version() + ")");
            }
        } catch (Exception e) {
            System.err.println("Failed to attach to or collect from JVM: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }

        java.util.List<dev.jvmai.analysis.RuleResult> ruleResults = new java.util.ArrayList<>();
        String llmReasoning = null;
        dev.jvmai.llm.LlmService llmService = null;

        if (noLlm) {
            System.out.println("Running static diagnostic rules...");
            dev.jvmai.analysis.RuleEngine ruleEngine = new dev.jvmai.analysis.RuleEngine();
            ruleResults = ruleEngine.evaluateAll(diagnostics);
        } else {
            System.out.println("\nInitializing Multi-Agent AI System via LangGraph4j (" + model + ")...");
            llmService = new dev.jvmai.llm.LlmService(model, timeout);
            
            Diagnostics payloadDiags = diagnostics;
            if (redact) {
                payloadDiags = new dev.jvmai.llm.SecurityRedactor().redact(diagnostics);
            }

            try {
                dev.jvmai.llm.graph.GraphRunner graphRunner = new dev.jvmai.llm.graph.GraphRunner(llmService);
                dev.jvmai.llm.graph.JvmaiState finalState = graphRunner.run(payloadDiags);
                llmReasoning = finalState.finalReasoning();
            } catch (Exception e) {
                System.err.println("Warning: LangGraph4j execution failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

        dev.jvmai.format.OutputFormatter formatter;
        switch (output.toLowerCase()) {
            case "json" -> formatter = new dev.jvmai.format.JsonOutputFormatter();
            case "markdown", "md" -> formatter = new dev.jvmai.format.MarkdownOutputFormatter();
            case "text" -> formatter = new dev.jvmai.format.TextOutputFormatter();
            default -> {
                System.err.println("Unknown output format: " + output + ". Falling back to text.");
                formatter = new dev.jvmai.format.TextOutputFormatter();
            }
        }

        System.out.println("\n" + formatter.format(diagnostics, ruleResults, llmReasoning));

        if (interactive && llmService != null) {
            System.out.println("\n--- Interactive QA Mode ---");
            System.out.println("Type 'exit' or 'quit' to leave.");
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            while (true) {
                System.out.print("\njvmai> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                }
                if (line.isEmpty()) continue;
                
                try {
                    String answer = llmService.askQuestion(line);
                    System.out.println("\n" + answer);
                } catch (Exception e) {
                    System.err.println("Error communicating with LLM: " + e.getMessage());
                }
            }
        } else if (interactive && noLlm) {
            System.err.println("Warning: Interactive mode requested but LLM is disabled (--no-llm). Ignoring.");
        }

        return 0;
    }
}
