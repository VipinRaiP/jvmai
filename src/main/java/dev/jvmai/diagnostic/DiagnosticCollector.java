package dev.jvmai.diagnostic;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.lang.management.*;
import java.util.*;

public class DiagnosticCollector {

    private final MBeanServerConnection mbeanServer;

    public DiagnosticCollector(MBeanServerConnection mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public Diagnostics collect() throws Exception {
        return new Diagnostics(
                collectMetadata(),
                collectThreads(),
                collectMemory()
        );
    }

    private Diagnostics.JvmMetadata collectMetadata() throws Exception {
        RuntimeMXBean runtime = ManagementFactory.newPlatformMXBeanProxy(
                mbeanServer, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);

        // Try to get GC algorithm name if available
        String gcAlgo = "Unknown";
        List<GarbageCollectorMXBean> gcs = collectGcs();
        if (!gcs.isEmpty()) {
            gcAlgo = gcs.get(0).getName();
        }

        return new Diagnostics.JvmMetadata(
                runtime.getVmVersion(),
                runtime.getVmVendor(),
                runtime.getVmName(),
                runtime.getUptime(),
                gcAlgo
        );
    }

    private Diagnostics.ThreadDiagnostics collectThreads() throws Exception {
        ThreadMXBean threadMXBean = ManagementFactory.newPlatformMXBeanProxy(
                mbeanServer, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);

        int threadCount = threadMXBean.getThreadCount();
        int daemonThreadCount = threadMXBean.getDaemonThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        long totalStarted = threadMXBean.getTotalStartedThreadCount();

        long[] deadlocked = threadMXBean.findDeadlockedThreads();
        if (deadlocked == null) {
            deadlocked = new long[0];
        }

        Map<String, Integer> stateDistribution = new HashMap<>();
        List<Diagnostics.ThreadInfoData> sampleThreads = new ArrayList<>();

        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                String state = info.getThreadState().name();
                stateDistribution.put(state, stateDistribution.getOrDefault(state, 0) + 1);

                // Add up to 100 samples
                if (sampleThreads.size() < 100) {
                    sampleThreads.add(new Diagnostics.ThreadInfoData(
                            info.getThreadId(),
                            info.getThreadName(),
                            state,
                            info.getLockName(),
                            info.getLockOwnerId(),
                            info.isSuspended(),
                            info.isInNative()
                    ));
                }
            }
        }

        return new Diagnostics.ThreadDiagnostics(
                threadCount, daemonThreadCount, peakThreadCount, totalStarted,
                stateDistribution, deadlocked, sampleThreads
        );
    }

    private Diagnostics.MemoryDiagnostics collectMemory() throws Exception {
        MemoryMXBean memoryMXBean = ManagementFactory.newPlatformMXBeanProxy(
                mbeanServer, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);

        MemoryUsage heapUtils = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUtils = memoryMXBean.getNonHeapMemoryUsage();

        double heapUsagePct = heapUtils.getMax() > 0 ? (double) heapUtils.getUsed() / heapUtils.getMax() : 0.0;

        List<Diagnostics.GarbageCollectorStats> gcStats = new ArrayList<>();
        for (GarbageCollectorMXBean gc : collectGcs()) {
            gcStats.add(new Diagnostics.GarbageCollectorStats(
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTime()
            ));
        }

        return new Diagnostics.MemoryDiagnostics(
                heapUtils.getUsed(),
                heapUtils.getCommitted(),
                heapUtils.getMax(),
                heapUsagePct,
                nonHeapUtils.getUsed(),
                nonHeapUtils.getCommitted(),
                nonHeapUtils.getMax(),
                gcStats
        );
    }

    private List<GarbageCollectorMXBean> collectGcs() throws Exception {
        Set<ObjectName> gcNames = mbeanServer.queryNames(new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*"), null);
        List<GarbageCollectorMXBean> gcs = new ArrayList<>();
        for (ObjectName name : gcNames) {
            gcs.add(ManagementFactory.newPlatformMXBeanProxy(mbeanServer, name.toString(), GarbageCollectorMXBean.class));
        }
        return gcs;
    }
}
