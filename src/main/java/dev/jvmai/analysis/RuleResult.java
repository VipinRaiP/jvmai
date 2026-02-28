package dev.jvmai.analysis;

public record RuleResult(String ruleName, Severity severity, String explanation) {
    public enum Severity {
        OK, WARNING, CRITICAL
    }
}
