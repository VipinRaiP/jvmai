package dev.jvmai.analysis;

import dev.jvmai.diagnostic.Diagnostics;

import java.util.List;
import java.util.stream.Collectors;

public class RuleEngine {

    private final List<DiagnosticRule> rules;

    public RuleEngine() {
        this.rules = List.of(
            new DeadlockRule(),
            new HighThreadCountRule(),
            new HighThreadContentionRule(),
            new HighHeapUsageRule(),
            new GcThrashingRule()
        );
    }

    public List<RuleResult> evaluateAll(Diagnostics diagnostics) {
        return rules.stream()
                .map(rule -> rule.evaluate(diagnostics))
                .collect(Collectors.toList());
    }
}
