package dev.jvmai.format;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult;

import java.util.List;

public class TextOutputFormatter implements OutputFormatter {
    @Override
    public String format(Diagnostics diagnostics, List<RuleResult> ruleResults, String llmReasoning) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JVMAI Diagnostic Report ===\n\n");
        sb.append("JVM Details:\n");
        sb.append("  Name: ").append(diagnostics.metadata().name()).append("\n");
        sb.append("  Version: ").append(diagnostics.metadata().version()).append("\n");
        sb.append("  Uptime (ms): ").append(diagnostics.metadata().uptimeMillis()).append("\n");
        sb.append("  GC Algo: ").append(diagnostics.metadata().gcAlgorithm()).append("\n\n");

        sb.append("Resource Summary:\n");
        sb.append("  Thread Count: ").append(diagnostics.threads().threadCount()).append("\n");
        sb.append(String.format("  Heap Usage: %.1f%%\n\n", diagnostics.memory().heapUsagePercentage() * 100));

        sb.append("Rule Analysis:\n");
        for (RuleResult result : ruleResults) {
            String prefix = result.severity() == RuleResult.Severity.OK ? "[OK]" : "[" + result.severity().name() + "]";
            sb.append("  ").append(prefix).append(" ").append(result.ruleName()).append(": ").append(result.explanation()).append("\n");
        }

        if (llmReasoning != null && !llmReasoning.isBlank()) {
            sb.append("\nAI Reasoning & Recommendations:\n");
            sb.append(llmReasoning).append("\n");
        }

        return sb.toString();
    }
}
