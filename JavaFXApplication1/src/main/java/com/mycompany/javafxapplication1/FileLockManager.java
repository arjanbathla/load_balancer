/*
 * File Lock Manager for concurrent file operations
 * Provides per-file and per-container locking mechanisms
 */
package com.mycompany.javafxapplication1;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Manages file-level and container-level locking for concurrent operations
 */
public class FileLockManager {
    private final Map<String, ReentrantReadWriteLock> fileLocks;
    private final Map<String, ReentrantReadWriteLock> containerLocks;
    private final Map<String, AtomicInteger> fileAccessCount;
    private final Map<String, Set<String>> containerFiles;
    private final Logger logger;
    
    public FileLockManager() {
        this.fileLocks = new ConcurrentHashMap<>();
        this.containerLocks = new ConcurrentHashMap<>();
        this.fileAccessCount = new ConcurrentHashMap<>();
        this.containerFiles = new ConcurrentHashMap<>();
        this.logger = Logger.getLogger(FileLockManager.class.getName());
    }
    
    /**
     * Acquire a read lock for a file
     */
    public boolean acquireReadLock(String fileName, String containerName) {
        ReentrantReadWriteLock fileLock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock());
        ReentrantReadWriteLock containerLock = containerLocks.computeIfAbsent(containerName, k -> new ReentrantReadWriteLock());
        
        // Acquire container read lock first
        if (containerLock.readLock().tryLock()) {
            try {
                // Then acquire file read lock
                if (fileLock.readLock().tryLock()) {
                    fileAccessCount.computeIfAbsent(fileName, k -> new AtomicInteger(0)).incrementAndGet();
                    containerFiles.computeIfAbsent(containerName, k -> new HashSet<>()).add(fileName);
                    logger.log(Level.INFO, String.format("Read lock acquired for file %s on container %s", fileName, containerName));
                    return true;
                } else {
                    containerLock.readLock().unlock();
                    return false;
                }
            } catch (Exception e) {
                containerLock.readLock().unlock();
                logger.log(Level.WARNING, String.format("Failed to acquire read lock for file %s: %s", fileName, e.getMessage()));
                return false;
            }
        }
        return false;
    }
    
    /**
     * Acquire a write lock for a file
     */
    public boolean acquireWriteLock(String fileName, String containerName) {
        ReentrantReadWriteLock fileLock = fileLocks.computeIfAbsent(fileName, k -> new ReentrantReadWriteLock());
        ReentrantReadWriteLock containerLock = containerLocks.computeIfAbsent(containerName, k -> new ReentrantReadWriteLock());
        
        // Acquire container write lock first
        if (containerLock.writeLock().tryLock()) {
            try {
                // Then acquire file write lock
                if (fileLock.writeLock().tryLock()) {
                    fileAccessCount.computeIfAbsent(fileName, k -> new AtomicInteger(0)).incrementAndGet();
                    containerFiles.computeIfAbsent(containerName, k -> new HashSet<>()).add(fileName);
                    logger.log(Level.INFO, String.format("Write lock acquired for file %s on container %s", fileName, containerName));
                    return true;
                } else {
                    containerLock.writeLock().unlock();
                    return false;
                }
            } catch (Exception e) {
                containerLock.writeLock().unlock();
                logger.log(Level.WARNING, String.format("Failed to acquire write lock for file %s: %s", fileName, e.getMessage()));
                return false;
            }
        }
        return false;
    }
    
    /**
     * Release a read lock for a file
     */
    public void releaseReadLock(String fileName, String containerName) {
        ReentrantReadWriteLock fileLock = fileLocks.get(fileName);
        ReentrantReadWriteLock containerLock = containerLocks.get(containerName);
        
        if (fileLock != null && containerLock != null) {
            try {
                fileLock.readLock().unlock();
                containerLock.readLock().unlock();
                
                AtomicInteger count = fileAccessCount.get(fileName);
                if (count != null) {
                    count.decrementAndGet();
                }
                
                logger.log(Level.INFO, String.format("Read lock released for file %s on container %s", fileName, containerName));
            } catch (Exception e) {
                logger.log(Level.WARNING, String.format("Error releasing read lock for file %s: %s", fileName, e.getMessage()));
            }
        }
    }
    
    /**
     * Release a write lock for a file
     */
    public void releaseWriteLock(String fileName, String containerName) {
        ReentrantReadWriteLock fileLock = fileLocks.get(fileName);
        ReentrantReadWriteLock containerLock = containerLocks.get(containerName);
        
        if (fileLock != null && containerLock != null) {
            try {
                fileLock.writeLock().unlock();
                containerLock.writeLock().unlock();
                
                AtomicInteger count = fileAccessCount.get(fileName);
                if (count != null) {
                    count.decrementAndGet();
                }
                
                logger.log(Level.INFO, String.format("Write lock released for file %s on container %s", fileName, containerName));
            } catch (Exception e) {
                logger.log(Level.WARNING, String.format("Error releasing write lock for file %s: %s", fileName, e.getMessage()));
            }
        }
    }
    
    /**
     * Check if a file is currently locked
     */
    public boolean isFileLocked(String fileName) {
        ReentrantReadWriteLock fileLock = fileLocks.get(fileName);
        return fileLock != null && (fileLock.isWriteLocked() || fileLock.getReadLockCount() > 0);
    }
    
    /**
     * Get the number of active readers for a file
     */
    public int getReaderCount(String fileName) {
        ReentrantReadWriteLock fileLock = fileLocks.get(fileName);
        return fileLock != null ? fileLock.getReadLockCount() : 0;
    }
    
    /**
     * Check if a file is write-locked
     */
    public boolean isFileWriteLocked(String fileName) {
        ReentrantReadWriteLock fileLock = fileLocks.get(fileName);
        return fileLock != null && fileLock.isWriteLocked();
    }
    
    /**
     * Get access count for a file
     */
    public int getFileAccessCount(String fileName) {
        AtomicInteger count = fileAccessCount.get(fileName);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Get all files in a container
     */
    public Set<String> getContainerFiles(String containerName) {
        return containerFiles.getOrDefault(containerName, new HashSet<>());
    }
    
    /**
     * Clean up locks for a file (call when file is deleted)
     */
    public void cleanupFileLocks(String fileName) {
        fileLocks.remove(fileName);
        fileAccessCount.remove(fileName);
        
        // Remove from container files
        containerFiles.values().forEach(files -> files.remove(fileName));
        
        logger.log(Level.INFO, String.format("Cleaned up locks for file %s", fileName));
    }
    
    /**
     * Get lock statistics
     */
    public Map<String, Object> getLockStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalFileLocks", fileLocks.size());
        stats.put("totalContainerLocks", containerLocks.size());
        stats.put("totalFileAccesses", fileAccessCount.values().stream().mapToInt(AtomicInteger::get).sum());
        
        Map<String, Integer> fileAccessCounts = new ConcurrentHashMap<>();
        fileAccessCount.forEach((file, count) -> fileAccessCounts.put(file, count.get()));
        stats.put("fileAccessCounts", fileAccessCounts);
        
        return stats;
    }
}
