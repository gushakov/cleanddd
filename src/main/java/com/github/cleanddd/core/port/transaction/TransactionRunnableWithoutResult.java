package com.github.cleanddd.core.port.transaction;

/**
 * Functional interface which will be executed by {@linkplain TransactionOperationsOutputPort}
 * without returning any results.
 *
 * @see TransactionOperationsOutputPort
 */
@FunctionalInterface
public interface TransactionRunnableWithoutResult {
    void run();
}
