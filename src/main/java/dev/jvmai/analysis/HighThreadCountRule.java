package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult.Severity;

public class HighThreadCountRule implements DiagnosticRule {
    private static final int THRESHOLD = 500;

    @Override
    public RuleResult evaluate(Diagnostics diagnostics) {
        int count = diagnostics.threads().threadCount();
        if (count > THRESHOLD) {
            return new RuleResult(
                "High Thread Count",
                Severity.WARNING,
                "Thread count is " + count + ", exceeding threshold of " + THRESHOLD + " threads."
            );
        }
        return new RuleResult("High Thread Count", Severity.OK, "Thread count is normal (" + count + ").");
    }
}
