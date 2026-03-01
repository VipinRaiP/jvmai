package dev.jvmai.diagnostic;

import java.util.List;
import java.util.Map;

public record Diagnostics(
        JvmMetadata metadata,
        ThreadDiagnostics threads,
        MemoryDiagnostics memory
) implements java.io.Serializable {

    public record JvmMetadata(
            String version,
            String vendor,
            String name,
            long uptimeMillis,
            String gcAlgorithm
    ) implements java.io.Serializable {}

    public record ThreadDiagnostics(
            int threadCount,
            int daemonThreadCount,
            int peakThreadCount,
            long totalStartedThreadCount,
            Map<String, Integer> stateDistribution,
            // e.g. mapping thread states to counts
            long[] deadlockedThreadIds,
            List<ThreadInfoData> sampleThreads 
            // sample of active threads to limit data size
    ) implements java.io.Serializable {}

    public record ThreadInfoData(
            long threadId,
            String threadName,
            String threadState,
            String lockName,
            long lockOwnerId,
            boolean isSuspended,
            boolean isInNative
    ) implements java.io.Serializable {}

    public record MemoryDiagnostics(
            long heapUsed,
            long heapCommitted,
            long heapMax,
            double heapUsagePercentage,
            long nonHeapUsed,
            long nonHeapCommitted,
            long nonHeapMax,
            List<GarbageCollectorStats> gcStats
    ) implements java.io.Serializable {}

    public record GarbageCollectorStats(
            String name,
            long collectionCount,
            long collectionTimeMillis
    ) implements java.io.Serializable {}
}
