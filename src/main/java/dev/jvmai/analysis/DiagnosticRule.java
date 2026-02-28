package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;

public interface DiagnosticRule {
    RuleResult evaluate(Diagnostics diagnostics);
}
