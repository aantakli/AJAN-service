package de.dfki.asr.ajan.pluginsystem.pythonplugin.monitoring;

import de.dfki.asr.ajan.pluginsystem.extensionpoints.PythonExecutionResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

public class PythonExecutionMetrics {
    private static PythonExecutionMetrics instance;

    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);
    private final DoubleAdder totalExecutionTimeMs = new DoubleAdder();

    private PythonExecutionMetrics() {}

    public static synchronized PythonExecutionMetrics getInstance() {
        if (instance == null) {
            instance = new PythonExecutionMetrics();
        }
        return instance;
    }

    public void recordExecution(PythonExecutionResult result) {
        totalExecutions.incrementAndGet();
        if (result.isSuccess()) {
            successfulExecutions.incrementAndGet();
        } else {
            failedExecutions.incrementAndGet();
        }
        totalExecutionTimeMs.add(result.getExecutionTimeMs());
    }

    public Map<String, Object> getMetricsSummary() {
        Map<String, Object> summary = new ConcurrentHashMap<>();
        long total = totalExecutions.get();
        summary.put("totalExecutions", total);
        summary.put("successfulExecutions", successfulExecutions.get());
        summary.put("failedExecutions", failedExecutions.get());
        summary.put("averageExecutionTimeMs", total > 0 ? totalExecutionTimeMs.sum() / total : 0.0);
        return summary;
    }

    public void resetMetrics() {
        totalExecutions.set(0);
        successfulExecutions.set(0);
        failedExecutions.set(0);
        totalExecutionTimeMs.reset();
    }
}
