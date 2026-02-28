package dev.jvmai;

import dev.jvmai.diagnostic.DiagnosticCollector;
import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.diagnostic.JvmConnection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "diagnose", description = "Attaches to a JVM process and collects diagnostics.")
public class DiagnoseCommand implements Callable<Integer> {

    @Option(names = {"-p", "--pid"}, required = true, description = "Target JVM Process ID")
    private long pid;

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
        System.out.println("Attaching to JVM with PID: " + pid + "...");
        
        Diagnostics diagnostics;
        try (JvmConnection connection = new JvmConnection(pid)) {
            System.out.println("Successfully attached to JVM. Collecting diagnostics...");
            DiagnosticCollector collector = new DiagnosticCollector(connection.getConnection());
            diagnostics = collector.collect();
            System.out.println("Diagnostics collected for JVM: " + diagnostics.metadata().name() + " (" + diagnostics.metadata().version() + ")");
        } catch (Exception e) {
            System.err.println("Failed to attach to or collect from JVM: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }

        dev.jvmai.analysis.RuleEngine engine = new dev.jvmai.analysis.RuleEngine();
        java.util.List<dev.jvmai.analysis.RuleResult> ruleResults = engine.evaluateAll(diagnostics);

        String llmReasoning = null;
        dev.jvmai.llm.LlmService llmService = null;
        if (noLlm) {
            // No reasoning requested
        } else {
            System.out.println("Analyzing with LLM (" + model + ")...");
            llmService = new dev.jvmai.llm.LlmService(model, timeout);
            Diagnostics payloadDiags = diagnostics;
            if (redact) {
                payloadDiags = new dev.jvmai.llm.SecurityRedactor().redact(diagnostics);
            }
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                String jsonDiags = mapper.writeValueAsString(payloadDiags);
                llmReasoning = llmService.analyze(jsonDiags);
            } catch (Exception e) {
                System.err.println("Warning: LLM analysis failed: " + e.getMessage());
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
