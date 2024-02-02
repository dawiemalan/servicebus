/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package dmcs.common.listeners;

import java.util.concurrent.Executor;

/**
 * Abstracts a listenable object
 */
@SuppressWarnings("unchecked")
public interface Listenable<T> {

    @SuppressWarnings("rawtypes")
    ListenerManager getListenerManager();

    /**
     * Add the given listener. The listener will be executed in the containing
     * instance's thread.
     *
     * @param listener listener to add
     */
    default void addListener(T listener) {
        getListenerManager().addListener(listener);
    }

    /**
     * Add the given listener. The listener will be executed using the given
     * executor
     *
     * @param listener listener to add
     * @param executor executor to run listener in
     */
    default void addListener(T listener, Executor executor) {
        getListenerManager().addListener(listener, executor);
    }

    /**
     * Remove the given listener
     *
     * @param listener listener to remove
     */
    default void removeListener(T listener) {
        getListenerManager().removeListener(listener);
    }
}
