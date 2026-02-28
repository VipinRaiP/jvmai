package dev.jvmai.llm;

import dev.jvmai.diagnostic.Diagnostics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityRedactorTest {

    @Test
    void testRedaction() {
        SecurityRedactor redactor = new SecurityRedactor();

        Diagnostics.ThreadInfoData thread1 = new Diagnostics.ThreadInfoData(
                1L,
                "worker-thread-abcd-1234-abcd-1234-abcd",
                "RUNNABLE",
                "lock-12345678", // Numeric test
                0, false, false
        );

        Diagnostics.ThreadDiagnostics tDiags = new Diagnostics.ThreadDiagnostics(
                1, 0, 1, 1,
                java.util.Collections.emptyMap(),
                new long[0],
                List.of(thread1)
        );

        Diagnostics original = new Diagnostics(null, tDiags, null);
        Diagnostics redacted = redactor.redact(original);

        Diagnostics.ThreadInfoData rThread = redacted.threads().sampleThreads().get(0);
        
        // Assert lock name has sequence redacted
        assertEquals("lock-[REDACTED_NUM]", rThread.lockName());
    }
}
