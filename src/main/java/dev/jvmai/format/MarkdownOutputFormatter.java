package dev.jvmai.format;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult;

import java.util.List;

public class MarkdownOutputFormatter implements OutputFormatter {
    @Override
    public String format(Diagnostics diagnostics, List<RuleResult> ruleResults, String llmReasoning) {
        StringBuilder sb = new StringBuilder();
        sb.append("# JVMAI Diagnostic Report\n\n");
        
        sb.append("## JVM Details\n");
        sb.append(String.format("- **Name:** %s\n", diagnostics.metadata().name()));
        sb.append(String.format("- **Version:** %s\n", diagnostics.metadata().version()));
        sb.append(String.format("- **Uptime (ms):** %d\n", diagnostics.metadata().uptimeMillis()));
        sb.append(String.format("- **GC Algo:** %s\n\n", diagnostics.metadata().gcAlgorithm()));

        sb.append("## Resource Summary\n");
        sb.append(String.format("- **Thread Count:** %d\n", diagnostics.threads().threadCount()));
        sb.append(String.format("- **Heap Usage:** %.1f%%\n\n", diagnostics.memory().heapUsagePercentage() * 100));

        sb.append("## Rule Analysis\n");
        for (RuleResult result : ruleResults) {
            String icon = result.severity() == RuleResult.Severity.OK ? "✅" : (result.severity() == RuleResult.Severity.WARNING ? "⚠️" : "❌");
            sb.append(String.format("- %s **%s**: %s\n", icon, result.ruleName(), result.explanation()));
        }

        if (llmReasoning != null && !llmReasoning.isBlank()) {
            sb.append("\n## AI Reasoning & Recommendations\n");
            sb.append(llmReasoning).append("\n");
        }

        return sb.toString();
    }
}
