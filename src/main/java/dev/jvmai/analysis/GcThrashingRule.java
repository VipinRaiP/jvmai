package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import dev.jvmai.analysis.RuleResult.Severity;

public class GcThrashingRule implements DiagnosticRule {
    
    @Override
    public RuleResult evaluate(Diagnostics diagnostics) {
        // A simple heuristic for GC thrashing: excessive GC count compared to uptime or missing throughput metrics.
        // As a placeholder, if GC time > 30% of uptime we might call it warning, but we don't have total GC time easily accessible unless we sum them up.
        long totalGcTimeMsg = 0;
        long uptime = diagnostics.metadata().uptimeMillis();
        
        for (Diagnostics.GarbageCollectorStats gc : diagnostics.memory().gcStats()) {
            totalGcTimeMsg += gc.collectionTimeMillis();
        }
        
        if (uptime > 0) {
            double gcRatio = (double) totalGcTimeMsg / uptime;
            if (gcRatio > 0.3) {
                return new RuleResult(
                    "GC Thrashing",
                    Severity.CRITICAL,
                    String.format("%.1f%% of JVM uptime has been spent in Garbage Collection.", gcRatio * 100)
                );
            } else if (gcRatio > 0.1) {
                return new RuleResult(
                    "GC Overhead Warning",
                    Severity.WARNING,
                    String.format("%.1f%% of JVM uptime has been spent in Garbage Collection.", gcRatio * 100)
                );
            }
        }
        return new RuleResult("GC Health", Severity.OK, "GC overhead is within normal parameters.");
    }
}
