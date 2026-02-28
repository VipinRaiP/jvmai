package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult.Severity;

public class DeadlockRule implements DiagnosticRule {
    @Override
    public RuleResult evaluate(Diagnostics diagnostics) {
        long[] deadlocked = diagnostics.threads().deadlockedThreadIds();
        if (deadlocked != null && deadlocked.length > 0) {
            return new RuleResult(
                "Deadlock Detection",
                Severity.CRITICAL,
                "Detected " + deadlocked.length + " deadlocked threads. Action required immediately."
            );
        }
        return new RuleResult("Deadlock Detection", Severity.OK, "No deadlocked threads found.");
    }
}
