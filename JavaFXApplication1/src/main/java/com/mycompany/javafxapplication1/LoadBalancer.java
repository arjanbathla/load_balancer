/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Enhanced Load Balancer with multiple scheduling algorithms
 * Supports FCFS, SJF, Priority, SRT, Round-Robin, and Multi-Level Queue scheduling
 */
public class LoadBalancer {
    private final List<StorageNode> storageNodes;
    private final AtomicInteger currentIndex;
    private final SchedulingAlgorithm algorithm;
    private final PriorityQueue<Request> requestQueue;
    private final Map<String, AtomicInteger> nodeUtilization;
    private final Map<String, Queue<Request>> nodeQueues;
    private final ReentrantLock lock;
    private final Logger logger;
    private final PerformanceMonitor performanceMonitor;
    
    public enum SchedulingAlgorithm {
        FCFS, SJF, PRIORITY, SRT, ROUND_ROBIN, MULTI_LEVEL_QUEUE
    }
    
    public static class Request {
        private final String id;
        private final String fileName;
        private final int priority;
        private final long estimatedDuration;
        private final long arrivalTime;
        private final long remainingTime;
        private final RequestType type;
        
        public enum RequestType {
            READ, WRITE, DELETE, LIST
        }
        
        public Request(String id, String fileName, int priority, long estimatedDuration, RequestType type) {
            this.id = id;
            this.fileName = fileName;
            this.priority = priority;
            this.estimatedDuration = estimatedDuration;
            this.arrivalTime = System.currentTimeMillis();
            this.remainingTime = estimatedDuration;
            this.type = type;
        }
        
        // Getters
        public String getId() { return id; }
        public String getFileName() { return fileName; }
        public int getPriority() { return priority; }
        public long getEstimatedDuration() { return estimatedDuration; }
        public long getArrivalTime() { return arrivalTime; }
        public long getRemainingTime() { return remainingTime; }
        public RequestType getType() { return type; }
        
        @Override
        public String toString() {
            return String.format("Request{id='%s', file='%s', priority=%d, duration=%dms, type=%s}", 
                id, fileName, priority, estimatedDuration, type);
        }
    }
    
    public static class StorageNode {
        private final String name;
        private final AtomicInteger currentLoad;
        private final AtomicInteger totalRequests;
        private final AtomicInteger totalProcessingTime;
        private final Queue<Request> requestQueue;
        private final ReentrantLock nodeLock;
        private volatile boolean isHealthy;
        
        public StorageNode(String name) {
            this.name = name;
            this.currentLoad = new AtomicInteger(0);
            this.totalRequests = new AtomicInteger(0);
            this.totalProcessingTime = new AtomicInteger(0);
            this.requestQueue = new ConcurrentLinkedQueue<>();
            this.nodeLock = new ReentrantLock();
            this.isHealthy = true;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public int getCurrentLoad() { return currentLoad.get(); }
        public int getTotalRequests() { return totalRequests.get(); }
        public double getAverageProcessingTime() { 
            int total = totalProcessingTime.get();
            int requests = totalRequests.get();
            return requests > 0 ? (double) total / requests : 0.0;
        }
        public boolean isHealthy() { return isHealthy; }
        public void setHealthy(boolean healthy) { this.isHealthy = healthy; }
        public Queue<Request> getRequestQueue() { return requestQueue; }
        public ReentrantLock getNodeLock() { return nodeLock; }
        
        public void addRequest(Request request) {
            currentLoad.incrementAndGet();
            totalRequests.incrementAndGet();
            requestQueue.offer(request);
        }
        
        public Request processRequest() {
            Request request = requestQueue.poll();
            if (request != null) {
                currentLoad.decrementAndGet();
                totalProcessingTime.addAndGet((int) request.getEstimatedDuration());
            }
            return request;
        }
        
        public int getQueueLength() {
            return requestQueue.size();
        }
    }
    
    public static class PerformanceMonitor {
        private final Map<String, List<Long>> responseTimes;
        private final Map<String, Integer> requestCounts;
        private final Map<String, Double> utilizationRates;
        private final Logger logger;
        
        public PerformanceMonitor() {
            this.responseTimes = new ConcurrentHashMap<>();
            this.requestCounts = new ConcurrentHashMap<>();
            this.utilizationRates = new ConcurrentHashMap<>();
            this.logger = Logger.getLogger(PerformanceMonitor.class.getName());
        }
        
        public void recordResponseTime(String nodeName, long responseTime) {
            responseTimes.computeIfAbsent(nodeName, k -> new ArrayList<>()).add(responseTime);
            logger.log(Level.INFO, String.format("Node %s response time: %dms", nodeName, responseTime));
        }
        
        public void recordRequest(String nodeName) {
            requestCounts.merge(nodeName, 1, Integer::sum);
        }
        
        public void updateUtilization(String nodeName, double utilization) {
            utilizationRates.put(nodeName, utilization);
            logger.log(Level.INFO, String.format("Node %s utilization: %.2f%%", nodeName, utilization * 100));
        }
        
        public Map<String, Double> getAverageResponseTimes() {
            Map<String, Double> averages = new HashMap<>();
            responseTimes.forEach((node, times) -> {
                double avg = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
                averages.put(node, avg);
            });
            return averages;
        }
        
        public Map<String, Integer> getRequestCounts() {
            return new HashMap<>(requestCounts);
        }
        
        public Map<String, Double> getUtilizationRates() {
            return new HashMap<>(utilizationRates);
        }
        
        public void logPerformanceReport() {
            logger.log(Level.INFO, "=== Performance Report ===");
            getAverageResponseTimes().forEach((node, avgTime) -> 
                logger.log(Level.INFO, String.format("Node %s: Avg Response Time = %.2fms", node, avgTime)));
            getRequestCounts().forEach((node, count) -> 
                logger.log(Level.INFO, String.format("Node %s: Total Requests = %d", node, count)));
            getUtilizationRates().forEach((node, util) -> 
                logger.log(Level.INFO, String.format("Node %s: Utilization = %.2f%%", node, util * 100)));
        }
    }
    
    public LoadBalancer(List<String> storageContainerNames, SchedulingAlgorithm algorithm) {
        this.storageNodes = new ArrayList<>();
        for (String name : storageContainerNames) {
            this.storageNodes.add(new StorageNode(name));
        }
        this.currentIndex = new AtomicInteger(0);
        this.algorithm = algorithm;
        this.requestQueue = new PriorityQueue<>();
        this.nodeUtilization = new ConcurrentHashMap<>();
        this.nodeQueues = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        this.logger = Logger.getLogger(LoadBalancer.class.getName());
        this.performanceMonitor = new PerformanceMonitor();
        
        // Initialize node utilization tracking
        for (StorageNode node : storageNodes) {
            nodeUtilization.put(node.getName(), new AtomicInteger(0));
            nodeQueues.put(node.getName(), new ConcurrentLinkedQueue<>());
        }
        
        logger.log(Level.INFO, String.format("LoadBalancer initialized with %d nodes using %s algorithm", 
            storageNodes.size(), algorithm));
    }
    
    public String getNextStorageContainer() {
        return getNextStorageContainer(new Request(
            UUID.randomUUID().toString(), 
            "default", 
            1, 
            1000, 
            Request.RequestType.READ
        ));
    }
    
    public String getNextStorageContainer(Request request) {
        lock.lock();
        try {
            StorageNode selectedNode = selectNode(request);
            if (selectedNode != null) {
                selectedNode.addRequest(request);
                performanceMonitor.recordRequest(selectedNode.getName());
                updateNodeUtilization(selectedNode);
                logger.log(Level.INFO, String.format("Request %s assigned to node %s", request.getId(), selectedNode.getName()));
                return selectedNode.getName();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    private StorageNode selectNode(Request request) {
        List<StorageNode> healthyNodes = storageNodes.stream()
            .filter(StorageNode::isHealthy)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        if (healthyNodes.isEmpty()) {
            logger.log(Level.WARNING, "No healthy nodes available");
            return null;
        }
        
        switch (algorithm) {
            case FCFS:
                return selectFCFS(healthyNodes);
            case SJF:
                return selectSJF(healthyNodes, request);
            case PRIORITY:
                return selectPriority(healthyNodes, request);
            case SRT:
                return selectSRT(healthyNodes, request);
            case ROUND_ROBIN:
                return selectRoundRobin(healthyNodes);
            case MULTI_LEVEL_QUEUE:
                return selectMultiLevelQueue(healthyNodes, request);
            default:
                return selectRoundRobin(healthyNodes);
        }
    }
    
    private StorageNode selectFCFS(List<StorageNode> nodes) {
        // First-Come-First-Served: Select node with shortest queue
        return nodes.stream()
            .min(Comparator.comparingInt(StorageNode::getQueueLength))
            .orElse(nodes.get(0));
    }
    
    private StorageNode selectSJF(List<StorageNode> nodes, Request request) {
        // Shortest Job First: Select node with shortest estimated processing time
        return nodes.stream()
            .min(Comparator.comparingInt(node -> node.getQueueLength() + 
                (int) request.getEstimatedDuration()))
            .orElse(nodes.get(0));
    }
    
    private StorageNode selectPriority(List<StorageNode> nodes, Request request) {
        // Priority: Select node with highest priority (lowest number = higher priority)
        return nodes.stream()
            .min(Comparator.comparingInt(node -> 
                node.getQueueLength() + request.getPriority()))
            .orElse(nodes.get(0));
    }
    
    private StorageNode selectSRT(List<StorageNode> nodes, Request request) {
        // Shortest Remaining Time: Consider both queue length and remaining time
        return nodes.stream()
            .min(Comparator.comparingInt(node -> 
                node.getQueueLength() + (int) request.getRemainingTime()))
            .orElse(nodes.get(0));
    }
    
    private StorageNode selectRoundRobin(List<StorageNode> nodes) {
        // Round Robin: Cycle through nodes
        int index = currentIndex.getAndUpdate(i -> (i + 1) % nodes.size());
        return nodes.get(index);
    }
    
    private StorageNode selectMultiLevelQueue(List<StorageNode> nodes, Request request) {
        // Multi-Level Queue: Different queues for different priority levels
        int priorityLevel = Math.min(request.getPriority() / 2, 2); // 0, 1, 2 levels
        
        // For simplicity, distribute across nodes based on priority level
        int nodeIndex = priorityLevel % nodes.size();
        return nodes.get(nodeIndex);
    }
    
    private void updateNodeUtilization(StorageNode node) {
        int currentLoad = node.getCurrentLoad();
        int maxCapacity = 10; // Assume max capacity per node
        double utilization = Math.min((double) currentLoad / maxCapacity, 1.0);
        performanceMonitor.updateUtilization(node.getName(), utilization);
    }
    
    public void simulateLoad(int numRequests, int delayMs) {
        logger.log(Level.INFO, String.format("Starting load simulation: %d requests with %dms delay", numRequests, delayMs));
        
        for (int i = 0; i < numRequests; i++) {
            Request request = new Request(
                "req_" + i,
                "file_" + i + ".txt",
                (int) (Math.random() * 5) + 1, // Priority 1-5
                (long) (Math.random() * 5000) + 1000, // Duration 1-6 seconds
                Request.RequestType.values()[(int) (Math.random() * 4)]
            );
            
            long startTime = System.currentTimeMillis();
            String nodeName = getNextStorageContainer(request);
            long endTime = System.currentTimeMillis();
            
            if (nodeName != null) {
                performanceMonitor.recordResponseTime(nodeName, endTime - startTime);
            }
            
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        performanceMonitor.logPerformanceReport();
    }
    
    public Map<String, Object> getNodeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        for (StorageNode node : storageNodes) {
            Map<String, Object> nodeStats = new HashMap<>();
            nodeStats.put("name", node.getName());
            nodeStats.put("currentLoad", node.getCurrentLoad());
            nodeStats.put("totalRequests", node.getTotalRequests());
            nodeStats.put("averageProcessingTime", node.getAverageProcessingTime());
            nodeStats.put("queueLength", node.getQueueLength());
            nodeStats.put("isHealthy", node.isHealthy());
            stats.put(node.getName(), nodeStats);
        }
        return stats;
    }
    
    public void setNodeHealth(String nodeName, boolean healthy) {
        storageNodes.stream()
            .filter(node -> node.getName().equals(nodeName))
            .findFirst()
            .ifPresent(node -> {
                node.setHealthy(healthy);
                logger.log(Level.INFO, String.format("Node %s health set to: %s", nodeName, healthy));
            });
    }
    
    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }
}
