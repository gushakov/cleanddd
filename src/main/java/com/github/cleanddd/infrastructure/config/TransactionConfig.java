package com.github.cleanddd.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/*
    References:
    ----------
    1. Programmatic transaction management, Spring Docs: https://docs.spring.io/spring-framework/reference/data-access/transaction/programmatic.html
 */

@Configuration
public class TransactionConfig {

    @Bean
    @Qualifier("read-only")
    public TransactionTemplate readOnlyTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setReadOnly(true);
        return template;
    }

}
