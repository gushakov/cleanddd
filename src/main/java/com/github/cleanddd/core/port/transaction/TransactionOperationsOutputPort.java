package com.github.cleanddd.core.port.transaction;

/**
 * Core interface for demarcation of transactional (consistency) boundary.
 * This interface may be called through an output port by a use case which
 * needs to perform some operations within or outside a transaction.
 * May also be used by any other adapter or configuration component directly
 * for any calls which need access to transactional demarcation.
 *
 * @see TransactionRunnableWithoutResult
 * @see TransactionRunnableWithResult
 */
public interface TransactionOperationsOutputPort {

    /**
     * Rolls back any current transaction. Does nothing if not called in
     * a transactional context.
     */
    void rollback();

    /**
     * Always executes the provided runnable. Makes sure the runnable is executed
     * within the currently active transaction if any is available.
     *
     * @param runnableWithoutResult a runnable which does not return anything
     */
    void doInTransaction(TransactionRunnableWithoutResult runnableWithoutResult);

    /**
     * Always executes the provided runnable returning the result of the execution. Makes sure
     * the runnable is executed within the currently active transaction if any is available.
     *
     * @param runnableWithResult a runnable with some return object
     * @param <R>                any type
     * @return object returned by the runnable
     */
    <R> R doInTransactionWithResult(TransactionRunnableWithResult<R> runnableWithResult);

    /**
     * Executes the provided runnable if called outside any transactional context. Otherwise, executes
     * the runnable only after a successful commit of the current transaction.
     *
     * @param runnableWithoutResult a runnable which does not return anything
     */
    void doAfterCommit(TransactionRunnableWithoutResult runnableWithoutResult);

    /**
     * Executes the provided runnable, returning the result of the execution, if called outside
     * any transactional context. Otherwise, executes the runnable only after a successful
     * commit of the current transaction.
     *
     * @param runnableWithResult a runnable with some return object
     * @param <R>                any type
     * @return object returned by the runnable
     */
    <R> R doAfterCommitWithResult(TransactionRunnableWithResult<R> runnableWithResult);

    /**
     * Executes the provided runnable if called outside any transactional context.
     * Otherwise, executes the runnable only after the current transaction was rolled back.
     *
     * @param runnableWithoutResult a runnable which does not return anything
     */
    void doAfterRollback(TransactionRunnableWithoutResult runnableWithoutResult);

    /**
     * Executes the provided runnable, returning the result of the execution, if called outside
     * any transactional context. Otherwise, executes the runnable only after the current
     * transaction was rolled back.
     *
     * @param runnableWithResult a runnable with some return object
     * @param <R>                any type
     * @return object returned by the runnable
     */
    <R> R doAfterRollbackWithResult(TransactionRunnableWithResult<R> runnableWithResult);
}
