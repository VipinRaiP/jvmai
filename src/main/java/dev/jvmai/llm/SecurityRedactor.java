package dev.jvmai.llm;

import dev.jvmai.diagnostic.Diagnostics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SecurityRedactor {

    // Simple redaction: masks digits or common pattern of secrets in thread names or JVM args if we had them.
    public Diagnostics redact(Diagnostics diagnostics) {
        // Redact thread names that might contain sensitive info (tokens, user ids)
        List<Diagnostics.ThreadInfoData> redactedThreads = diagnostics.threads().sampleThreads().stream()
                .map(t -> new Diagnostics.ThreadInfoData(
                        t.threadId(),
                        redactString(t.threadName()),
                        t.threadState(),
                        t.lockName() != null ? redactString(t.lockName()) : null,
                        t.lockOwnerId(),
                        t.isSuspended(),
                        t.isInNative()
                )).collect(Collectors.toList());

        Diagnostics.ThreadDiagnostics redactedThreadDiags = new Diagnostics.ThreadDiagnostics(
                diagnostics.threads().threadCount(),
                diagnostics.threads().daemonThreadCount(),
                diagnostics.threads().peakThreadCount(),
                diagnostics.threads().totalStartedThreadCount(),
                diagnostics.threads().stateDistribution(),
                diagnostics.threads().deadlockedThreadIds(),
                redactedThreads
        );

        return new Diagnostics(
                diagnostics.metadata(),
                redactedThreadDiags,
                diagnostics.memory()
        );
    }

    private String redactString(String input) {
        if (input == null) return null;
        // Redact anything that looks like a token/UUID/IP
        String redacted = input.replaceAll("\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b", "[REDACTED_UUID]");
        redacted = redacted.replaceAll("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b", "[REDACTED_IP]");
        redacted = redacted.replaceAll("bearer\\s+[A-Za-z0-9\\-\\._~\\+/]+", "bearer [REDACTED_TOKEN]");
        // Mask long numeric sequences
        redacted = redacted.replaceAll("\\b\\d{6,}\\b", "[REDACTED_NUM]");
        return redacted;
    }
}
