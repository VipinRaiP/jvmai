package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult.Severity;

public class HighThreadContentionRule implements DiagnosticRule {
    private static final double THRESHOLD = 0.40;

    @Override
    public RuleResult evaluate(Diagnostics diagnostics) {
        int totalThreads = diagnostics.threads().threadCount();
        int blocked = diagnostics.threads().stateDistribution().getOrDefault("BLOCKED", 0);
        
        if (totalThreads > 0) {
            double blockedRatio = (double) blocked / totalThreads;
            if (blockedRatio > THRESHOLD) {
                return new RuleResult(
                    "High Thread Contention",
                    Severity.CRITICAL,
                    String.format("%.1f%% of threads are in BLOCKED state.", blockedRatio * 100)
                );
            }
        }
        return new RuleResult("High Thread Contention", Severity.OK, "Thread contention is within normal limits.");
    }
}
