package dev.jvmai.format;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult;

import java.util.List;

public interface OutputFormatter {
    String format(Diagnostics diagnostics, List<RuleResult> ruleResults, String llmReasoning);
}
