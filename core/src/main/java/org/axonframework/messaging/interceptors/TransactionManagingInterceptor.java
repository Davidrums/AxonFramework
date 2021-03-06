/*
 * Copyright (c) 2010-2015. Axon Framework
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.messaging.interceptors;

import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;

/**
 * Interceptor that uses a {@link TransactionManager} to start a new transaction before a Message is handled. When the
 * Unit of Work is committed or rolled back this Interceptor also completes the Transaction.
 *
 * @author Rene de Waele
 */
public class TransactionManagingInterceptor<T extends Message<?>> implements MessageHandlerInterceptor<T> {

    private final TransactionManager transactionManager;

    public TransactionManagingInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(UnitOfWork<T> unitOfWork, InterceptorChain<T> interceptorChain) throws Exception {
        Transaction transaction = transactionManager.startTransaction();
        unitOfWork.onCommit(u -> transaction.commit());
        unitOfWork.onRollback(u -> transaction.rollback());
        return interceptorChain.proceed();
    }
}
