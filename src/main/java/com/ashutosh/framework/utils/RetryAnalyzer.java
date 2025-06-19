package com.ashutosh.framework.utils;

import java.util.function.Supplier;

public class RetryAnalyzer {

  private int totalRetries = 0;
  private int successfulRetries = 0;
  private int totalOperations = 0;

  // Default retry settings
  private static final int DEFAULT_ATTEMPTS = 3;
  private static final int DEFAULT_DELAY = 2;

  /**
   * Retry an operation with default settings
   */
  public <T> T retry(Supplier<T> operation) {
    return retry(operation, DEFAULT_ATTEMPTS, DEFAULT_DELAY);
  }

  /**
   * Retry an operation with custom settings
   */
  public <T> T retry(Supplier<T> operation, int maxAttempts, int delaySeconds) {
    totalOperations++;
    RuntimeException lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        System.out.println("Attempt " + attempt + " of " + maxAttempts);
        T result = operation.get();

        if (attempt > 1) {
          successfulRetries++;
          System.out.println("Retry succeeded on attempt " + attempt);
        }
        return result;

      } catch (RuntimeException e) {
        lastException = e;
        totalRetries++;
        System.out.println("Attempt " + attempt + " failed: " + e.getMessage());

        if (attempt < maxAttempts) {
          System.out.println("Waiting " + delaySeconds + " seconds before retry...");
          sleep(delaySeconds);
        }
      }
    }

    System.out.println("All " + maxAttempts + " attempts failed");
    throw lastException;
  }

  /**
   * Retry a void operation with default settings
   */
  public void retry(Runnable operation) {
    retry(operation, DEFAULT_ATTEMPTS, DEFAULT_DELAY);
  }

  /**
   * Retry a void operation with custom settings
   */
  public void retry(Runnable operation, int maxAttempts, int delaySeconds) {
    retry(() -> {
      operation.run();
      return null;
    }, maxAttempts, delaySeconds);
  }

  /**
   * Get retry success rate percentage
   */
  public double getRetrySuccessRate() {
    return totalRetries > 0 ? (double) successfulRetries / totalRetries * 100 : 0;
  }

  /**
   * Get total number of retries
   */
  public int getTotalRetries() {
    return totalRetries;
  }

  /**
   * Get total number of successful retries
   */
  public int getSuccessfulRetries() {
    return successfulRetries;
  }

  /**
   * Get total number of operations
   */
  public int getTotalOperations() {
    return totalOperations;
  }

  /**
   * Print retry metrics
   */
  public void printMetrics() {
    System.out.println("=== Retry Metrics ===");
    System.out.println("Total operations: " + totalOperations);
    System.out.println("Total retries: " + totalRetries);
    System.out.println("Successful retries: " + successfulRetries);
    System.out.println("Retry success rate: " + String.format("%.2f", getRetrySuccessRate()) + "%");
    System.out.println("====================");
  }

  /**
   * Reset all metrics
   */
  public void resetMetrics() {
    totalRetries = 0;
    successfulRetries = 0;
    totalOperations = 0;
  }

  /**
   * Simple sleep method
   */
  private void sleep(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Retry interrupted", e);
    }
  }
}
