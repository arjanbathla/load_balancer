/*
 * Performance Test Suite for Load Balancer
 * Tests different scheduling algorithms under various load conditions
 */
package com.mycompany.javafxapplication1;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Comprehensive performance testing suite for the load balancer
 */
public class PerformanceTestSuite {
    private final Logger logger;
    private final Map<LoadBalancer.SchedulingAlgorithm, TestResults> results;
    
    public PerformanceTestSuite() {
        this.logger = Logger.getLogger(PerformanceTestSuite.class.getName());
        this.results = new HashMap<>();
    }
    
    public static class TestResults {
        private final LoadBalancer.SchedulingAlgorithm algorithm;
        private final List<Long> responseTimes;
        private final Map<String, Integer> requestDistribution;
        private final Map<String, Double> nodeUtilization;
        private final int totalRequests;
        private final long totalTime;
        private final double averageResponseTime;
        private final double throughput;
        
        public TestResults(LoadBalancer.SchedulingAlgorithm algorithm, List<Long> responseTimes, 
                          Map<String, Integer> requestDistribution, Map<String, Double> nodeUtilization,
                          int totalRequests, long totalTime) {
            this.algorithm = algorithm;
            this.responseTimes = new ArrayList<>(responseTimes);
            this.requestDistribution = new HashMap<>(requestDistribution);
            this.nodeUtilization = new HashMap<>(nodeUtilization);
            this.totalRequests = totalRequests;
            this.totalTime = totalTime;
            this.averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            this.throughput = totalRequests * 1000.0 / totalTime; // requests per second
        }
        
        // Getters
        public LoadBalancer.SchedulingAlgorithm getAlgorithm() { return algorithm; }
        public List<Long> getResponseTimes() { return responseTimes; }
        public Map<String, Integer> getRequestDistribution() { return requestDistribution; }
        public Map<String, Double> getNodeUtilization() { return nodeUtilization; }
        public int getTotalRequests() { return totalRequests; }
        public long getTotalTime() { return totalTime; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public double getThroughput() { return throughput; }
        
        public double getFairnessIndex() {
            // Calculate Jain's fairness index for request distribution
            int totalRequests = requestDistribution.values().stream().mapToInt(Integer::intValue).sum();
            if (totalRequests == 0) return 0.0;
            
            double sumSquared = requestDistribution.values().stream()
                .mapToDouble(count -> Math.pow(count, 2))
                .sum();
            double sum = requestDistribution.values().stream()
                .mapToDouble(Integer::doubleValue)
                .sum();
            
            return Math.pow(sum, 2) / (requestDistribution.size() * sumSquared);
        }
    }
    
    /**
     * Run comprehensive performance tests for all scheduling algorithms
     */
    public void runAllTests() {
        List<String> containers = Arrays.asList(
            "comp20081-files-container1",
            "comp20081-files-container2", 
            "comp20081-files-container3",
            "comp20081-files-container4"
        );
        
        LoadBalancer.SchedulingAlgorithm[] algorithms = LoadBalancer.SchedulingAlgorithm.values();
        
        logger.log(Level.INFO, "Starting comprehensive performance testing...");
        
        for (LoadBalancer.SchedulingAlgorithm algorithm : algorithms) {
            logger.log(Level.INFO, String.format("Testing algorithm: %s", algorithm));
            TestResults result = testSchedulingAlgorithm(containers, algorithm);
            results.put(algorithm, result);
            logTestResults(result);
        }
        
        generateComparisonReport();
    }
    
    /**
     * Test a specific scheduling algorithm
     */
    public TestResults testSchedulingAlgorithm(List<String> containers, LoadBalancer.SchedulingAlgorithm algorithm) {
        LoadBalancer loadBalancer = new LoadBalancer(containers, algorithm);
        List<Long> responseTimes = new ArrayList<>();
        Map<String, Integer> requestDistribution = new HashMap<>();
        Map<String, Double> nodeUtilization = new HashMap<>();
        
        int numRequests = 1000;
        int delayMs = 10; // 10ms delay between requests
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numRequests; i++) {
            LoadBalancer.Request request = new LoadBalancer.Request(
                "test_req_" + i,
                "test_file_" + i + ".txt",
                (int) (Math.random() * 5) + 1, // Priority 1-5
                (long) (Math.random() * 3000) + 500, // Duration 0.5-3.5 seconds
                LoadBalancer.Request.RequestType.values()[(int) (Math.random() * 4)]
            );
            
            long requestStart = System.currentTimeMillis();
            String selectedNode = loadBalancer.getNextStorageContainer(request);
            long requestEnd = System.currentTimeMillis();
            
            if (selectedNode != null) {
                responseTimes.add(requestEnd - requestStart);
                requestDistribution.merge(selectedNode, 1, Integer::sum);
            }
            
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        // Get final utilization data
        Map<String, Object> nodeStats = loadBalancer.getNodeStatistics();
        for (Map.Entry<String, Object> entry : nodeStats.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) entry.getValue();
            double utilization = (Integer) stats.get("currentLoad") / 10.0; // Assuming max capacity of 10
            nodeUtilization.put(entry.getKey(), utilization);
        }
        
        return new TestResults(algorithm, responseTimes, requestDistribution, nodeUtilization, 
                              numRequests, endTime - startTime);
    }
    
    /**
     * Test with artificial I/O delays (30-90% as mentioned in requirements)
     */
    public void testWithArtificialDelays() {
        logger.log(Level.INFO, "Testing with artificial I/O delays (30-90%)...");
        
        List<String> containers = Arrays.asList(
            "comp20081-files-container1",
            "comp20081-files-container2", 
            "comp20081-files-container3",
            "comp20081-files-container4"
        );
        
        // Test Round Robin with delays
        LoadBalancer loadBalancer = new LoadBalancer(containers, LoadBalancer.SchedulingAlgorithm.ROUND_ROBIN);
        
        // Simulate node delays
        loadBalancer.setNodeHealth("comp20081-files-container1", true);
        loadBalancer.setNodeHealth("comp20081-files-container2", true);
        loadBalancer.setNodeHealth("comp20081-files-container3", true);
        loadBalancer.setNodeHealth("comp20081-files-container4", true);
        
        // Simulate load with delays
        loadBalancer.simulateLoad(500, 50); // 500 requests with 50ms delay
        
        logger.log(Level.INFO, "Artificial delay testing completed");
    }
    
    /**
     * Test concurrent access scenarios
     */
    public void testConcurrentAccess() {
        logger.log(Level.INFO, "Testing concurrent access scenarios...");
        
        List<String> containers = Arrays.asList(
            "comp20081-files-container1",
            "comp20081-files-container2", 
            "comp20081-files-container3",
            "comp20081-files-container4"
        );
        
        LoadBalancer loadBalancer = new LoadBalancer(containers, LoadBalancer.SchedulingAlgorithm.PRIORITY);
        FileLockManager lockManager = new FileLockManager();
        
        // Simulate concurrent file operations
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 100; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                String fileName = "concurrent_file_" + (threadId % 10) + ".txt";
                String containerName = loadBalancer.getNextStorageContainer();
                
                if (containerName != null) {
                    // Simulate file operations with locking
                    if (lockManager.acquireWriteLock(fileName, containerName)) {
                        try {
                            // Simulate file write operation
                            Thread.sleep(100 + (int)(Math.random() * 200));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            lockManager.releaseWriteLock(fileName, containerName);
                        }
                    }
                }
                return null;
            }));
        }
        
        // Wait for all threads to complete
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.log(Level.WARNING, "Concurrent test thread failed: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        
        // Log lock statistics
        Map<String, Object> lockStats = lockManager.getLockStatistics();
        logger.log(Level.INFO, "Concurrent access test completed. Lock statistics: " + lockStats);
    }
    
    private void logTestResults(TestResults result) {
        logger.log(Level.INFO, String.format("=== Test Results for %s ===", result.getAlgorithm()));
        logger.log(Level.INFO, String.format("Total Requests: %d", result.getTotalRequests()));
        logger.log(Level.INFO, String.format("Total Time: %d ms", result.getTotalTime()));
        logger.log(Level.INFO, String.format("Average Response Time: %.2f ms", result.getAverageResponseTime()));
        logger.log(Level.INFO, String.format("Throughput: %.2f requests/sec", result.getThroughput()));
        logger.log(Level.INFO, String.format("Fairness Index: %.4f", result.getFairnessIndex()));
        
        logger.log(Level.INFO, "Request Distribution:");
        result.getRequestDistribution().forEach((node, count) -> 
            logger.log(Level.INFO, String.format("  %s: %d requests", node, count)));
        
        logger.log(Level.INFO, "Node Utilization:");
        result.getNodeUtilization().forEach((node, util) -> 
            logger.log(Level.INFO, String.format("  %s: %.2f%%", node, util * 100)));
    }
    
    private void generateComparisonReport() {
        logger.log(Level.INFO, "=== Performance Comparison Report ===");
        
        // Sort algorithms by average response time
        List<Map.Entry<LoadBalancer.SchedulingAlgorithm, TestResults>> sortedResults = 
            results.entrySet().stream()
                .sorted(Comparator.comparingDouble(entry -> entry.getValue().getAverageResponseTime()))
                .collect(Collectors.toList());
        
        logger.log(Level.INFO, "Algorithms ranked by average response time:");
        for (int i = 0; i < sortedResults.size(); i++) {
            Map.Entry<LoadBalancer.SchedulingAlgorithm, TestResults> entry = sortedResults.get(i);
            logger.log(Level.INFO, String.format("%d. %s: %.2f ms", 
                i + 1, entry.getKey(), entry.getValue().getAverageResponseTime()));
        }
        
        // Find best algorithm for fairness
        LoadBalancer.SchedulingAlgorithm bestFairness = results.entrySet().stream()
            .max(Comparator.comparingDouble(entry -> entry.getValue().getFairnessIndex()))
            .map(Map.Entry::getKey)
            .orElse(LoadBalancer.SchedulingAlgorithm.ROUND_ROBIN);
        
        logger.log(Level.INFO, String.format("Best algorithm for fairness: %s", bestFairness));
        
        // Find best algorithm for throughput
        LoadBalancer.SchedulingAlgorithm bestThroughput = results.entrySet().stream()
            .max(Comparator.comparingDouble(entry -> entry.getValue().getThroughput()))
            .map(Map.Entry::getKey)
            .orElse(LoadBalancer.SchedulingAlgorithm.ROUND_ROBIN);
        
        logger.log(Level.INFO, String.format("Best algorithm for throughput: %s", bestThroughput));
    }
    
    public Map<LoadBalancer.SchedulingAlgorithm, TestResults> getResults() {
        return new HashMap<>(results);
    }
}
