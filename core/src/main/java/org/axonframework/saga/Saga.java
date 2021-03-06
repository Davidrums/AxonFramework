/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.saga;

import org.axonframework.eventhandling.EventMessage;

/**
 * Interface describing an implementation of a Saga. Sagas are instances that handle events and may possibly produce
 * new commands or have other side effects. Typically, Sagas are used to manage long running business transactions.
 * <p/>
 * Multiple instances of a single type of Saga may exist. In that case, each Saga will be managing a different
 * transaction. Sagas need to be associated with concepts in order to receive specific events. These associations are
 * managed through AssociationValues. For example, to associate a saga with an Order with ID 1234, this saga needs an
 * association value with key <code>"orderId"</code> and value <code>"1234"</code>.
 *
 * @author Allard Buijze
 * @since 0.7
 */
public interface Saga {

    /**
     * Returns the unique identifier of this saga.
     *
     * @return the unique identifier of this saga
     */
    String getSagaIdentifier();

    /**
     * Returns a view on the Association Values for this saga instance. The returned instance is mutable.
     *
     * @return a view on the Association Values for this saga instance
     */
    AssociationValues getAssociationValues();

    /**
     * Handle the given event. The actual result of processing depends on the implementation of the saga.
     * <p/>
     * Implementations are highly discouraged from throwing exceptions.
     *
     * @param event the event to handle
     */
    void handle(EventMessage event) throws Exception;

    /**
     * Indicates whether or not this saga is active. A Saga is active when its life cycle has not been ended.
     *
     * @return <code>true</code> if this saga is active, <code>false</code> otherwise.
     */
    boolean isActive();
}
