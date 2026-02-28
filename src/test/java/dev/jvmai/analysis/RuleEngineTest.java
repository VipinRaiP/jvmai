package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleEngineTest {

    private final RuleEngine engine = new RuleEngine();

    private Diagnostics createDummyDiags(Diagnostics.ThreadDiagnostics t, Diagnostics.MemoryDiagnostics m) {
        Diagnostics.JvmMetadata meta = new Diagnostics.JvmMetadata("21", "Vendor", "Name", 10000L, "G1");
        Diagnostics.ThreadDiagnostics threads = t != null ? t : new Diagnostics.ThreadDiagnostics(10, 5, 20, 100, Collections.emptyMap(), new long[0], Collections.emptyList());
        Diagnostics.MemoryDiagnostics mem = m != null ? m : new Diagnostics.MemoryDiagnostics(100, 200, 200, 0.5, 10, 20, 30, Collections.emptyList());
        return new Diagnostics(meta, threads, mem);
    }

    @Test
    void testDeadlockDetection() {
        Diagnostics.ThreadDiagnostics threadDiags = new Diagnostics.ThreadDiagnostics(
                10, 5, 20, 100,
                Map.of("RUNNABLE", 8, "BLOCKED", 2),
                new long[]{1L, 2L}, // Deadlocked thread IDs
                Collections.emptyList()
        );
        Diagnostics diags = createDummyDiags(threadDiags, null);

        List<RuleResult> results = engine.evaluateAll(diags);
        RuleResult result = getResultOrThrow("Deadlock Detection", results);
        
        assertEquals(RuleResult.Severity.CRITICAL, result.severity());
    }

    @Test
    void testHighThreadCountWarning() {
        Diagnostics.ThreadDiagnostics threadDiags = new Diagnostics.ThreadDiagnostics(
                501, 10, 600, 1000,
                Map.of("RUNNABLE", 501),
                new long[0],
                Collections.emptyList()
        );
        Diagnostics diags = createDummyDiags(threadDiags, null);

        List<RuleResult> results = engine.evaluateAll(diags);
        RuleResult result = getResultOrThrow("High Thread Count", results);
        
        assertEquals(RuleResult.Severity.WARNING, result.severity());
    }

    @Test
    void testHighHeapUsageCritical() {
        Diagnostics.MemoryDiagnostics memDiags = new Diagnostics.MemoryDiagnostics(
                860, 1000, 1000, 0.86,
                10, 20, 30,
                Collections.emptyList()
        );
        Diagnostics diags = createDummyDiags(null, memDiags);

        List<RuleResult> results = engine.evaluateAll(diags);
        RuleResult result = getResultOrThrow("High Heap Usage", results);
        
        assertEquals(RuleResult.Severity.CRITICAL, result.severity());
    }

    private RuleResult getResultOrThrow(String name, List<RuleResult> results) {
        return results.stream()
                .filter(r -> r.ruleName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Rule " + name + " not found"));
    }
}
