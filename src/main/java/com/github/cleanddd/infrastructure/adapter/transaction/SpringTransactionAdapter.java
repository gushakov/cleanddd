package com.github.cleanddd.infrastructure.adapter.transaction;

import com.github.cleanddd.core.port.transaction.TransactionOperationsOutputPort;
import com.github.cleanddd.core.port.transaction.TransactionRunnableWithResult;
import com.github.cleanddd.core.port.transaction.TransactionRunnableWithoutResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicReference;

/*
    References:
    ----------
    1.  Spring, TransactionTemplate: source code and JavaDoc
    2.  Spring transaction synchronization: https://azagorneanu.blogspot.com/2013/06/transaction-synchronization-callbacks.html
    3.  Do after commit with Spring transactions: https://stackoverflow.com/questions/15026142/creating-a-post-commit-when-using-transaction-in-spring
    4.  Rollback transaction: https://stackoverflow.com/a/23502214
    5.  Programmatic transactions with String, Baeldung: https://www.baeldung.com/spring-programmatic-transaction-management
    6.  Programmatic transactions with Spring, Spring documentation: https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html
 */

/**
 * Default implementation of {@linkplain TransactionOperationsOutputPort} using Spring's
 * transaction SPI.
 *
 * @see TransactionTemplate
 * @see TransactionInterceptor
 * @see TransactionSynchronizationManager
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
@Service
public class SpringTransactionAdapter implements TransactionOperationsOutputPort {

    TransactionTemplate transactionTemplate;

    @Qualifier("read-only")
    TransactionTemplate readOnlyTransactionTemplate;

    @Override
    public void rollback() {
        try {
            TransactionInterceptor.currentTransactionStatus().setRollbackOnly();
            log.debug("[Transaction] Will roll back current transaction");
        } catch (NoTransactionException e) {
            // do nothing if not in transaction
        }
    }

    @Override
    public void doInTransaction(TransactionRunnableWithoutResult runnableWithoutResult, boolean readOnly) {
        log.debug("[Transaction] Executing runnable (without a result) in a transaction, read-only: {}", readOnly);
        if (readOnly) {
            readOnlyTransactionTemplate.executeWithoutResult(transactionStatus -> runnableWithoutResult.run());
        } else {
            transactionTemplate.executeWithoutResult(transactionStatus -> runnableWithoutResult.run());
        }
    }

    @Override
    public <R> R doInTransactionWithResult(TransactionRunnableWithResult<R> runnableWithResult, boolean readOnly) {
        log.debug("[Transaction] Executing runnable (with a result) in a transaction, read-only: {}", readOnly);
        if (readOnly) {
            return readOnlyTransactionTemplate.execute(transactionStatus -> runnableWithResult.run());
        } else {
            return transactionTemplate.execute(transactionStatus -> runnableWithResult.run());
        }
    }

    @Override
    public void doAfterCommit(TransactionRunnableWithoutResult runnableWithoutResult) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // not in transaction, just execute the runnable
            log.debug("[Transaction] Not in transaction, executing runnable (without a result) from \"doAfterCommit\" directly");
            runnableWithoutResult.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    log.debug("[Transaction][After commit] Executing runnable (without a result) after commit");
                    runnableWithoutResult.run();
                }
            }
        });
    }

    @Override
    public <R> R doAfterCommitWithResult(TransactionRunnableWithResult<R> runnableWithResult) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // not in transaction, just execute the runnable
            log.debug("[Transaction] Not in transaction, executing runnable (with a result) from \"doAfterCommitWithResult\" directly");
            return runnableWithResult.run();
        }

        final AtomicReference<R> result = new AtomicReference<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    log.debug("[Transaction][After commit] Executing runnable (with a result) after commit");
                    result.set(runnableWithResult.run());
                }
            }

        });
        return result.get();
    }

    @Override
    public void doAfterRollback(TransactionRunnableWithoutResult runnableWithoutResult) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // not in transaction, just execute the runnable
            log.debug("[Transaction] Not in transaction, executing runnable (without a result) from \"doAfterRollback\" directly");
            runnableWithoutResult.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    log.debug("[Transaction][After rollback] Executing runnable (without a result) after rollback");
                    runnableWithoutResult.run();
                }
            }
        });
    }

    @Override
    public <R> R doAfterRollbackWithResult(TransactionRunnableWithResult<R> runnableWithResult) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            // not in transaction, just execute the runnable
            log.debug("[Transaction] Not in transaction, executing runnable (with a result) from \"doAfterRollbackWithResult\" directly");
            return runnableWithResult.run();
        }

        final AtomicReference<R> result = new AtomicReference<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    log.debug("[Transaction] Executing runnable (with a result) after rollback");
                    result.set(runnableWithResult.run());
                }
            }
        });
        return result.get();
    }

}
