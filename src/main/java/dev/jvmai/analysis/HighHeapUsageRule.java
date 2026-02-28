package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult.Severity;

public class HighHeapUsageRule implements DiagnosticRule {
    private static final double THRESHOLD = 0.85;

    @Override
    public RuleResult evaluate(Diagnostics diagnostics) {
        double usagePct = diagnostics.memory().heapUsagePercentage();
        if (usagePct > THRESHOLD) {
            return new RuleResult(
                "High Heap Usage",
                Severity.CRITICAL,
                String.format("Heap usage is at %.1f%%, exceeding threshold of %.1f%%.", usagePct * 100, THRESHOLD * 100)
            );
        }
        return new RuleResult("High Heap Usage", Severity.OK, "Heap usage is normal.");
    }
}
