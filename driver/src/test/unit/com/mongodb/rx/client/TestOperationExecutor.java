/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.rx.client;

import com.mongodb.ReadPreference;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.operation.AsyncOperationExecutor;
import com.mongodb.operation.AsyncReadOperation;
import com.mongodb.operation.AsyncWriteOperation;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestOperationExecutor implements AsyncOperationExecutor {

    private final List<Object> responses;
    private final boolean queueExecution;
    private final List<ReadPreference> readPreferences = new ArrayList<ReadPreference>();
    private final List<AsyncReadOperation> queuedReadOperations = new ArrayList<AsyncReadOperation>();
    private final List<AsyncWriteOperation> queuedWriteOperations = new ArrayList<AsyncWriteOperation>();
    private final List<SingleResultCallback> queuedReadCallbacks = new ArrayList<SingleResultCallback>();
    private final List<SingleResultCallback> queuedWriteCallbacks = new ArrayList<SingleResultCallback>();
    private final List<AsyncReadOperation> readOperations = new ArrayList<AsyncReadOperation>();
    private final List<AsyncWriteOperation> writeOperations = new ArrayList<AsyncWriteOperation>();

    TestOperationExecutor(final List<Object> responses) {
        this(responses, false);
    }

    TestOperationExecutor(final List<Object> responses, final boolean queueExecution) {
        this.responses = responses;
        this.queueExecution = queueExecution;
    }

    @Override
    public <T> void execute(final AsyncReadOperation<T> operation, final ReadPreference readPreference,
                            final SingleResultCallback<T> callback) {
        readPreferences.add(readPreference);
        if (queueExecution) {
            queuedReadOperations.add(operation);
            queuedReadCallbacks.add(callback);
        } else {
            readOperations.add(operation);
            callResult(callback);
        }
    }


    @Override
    public <T> void execute(final AsyncWriteOperation<T> operation, final SingleResultCallback<T> callback) {
        if (queueExecution) {
            queuedWriteOperations.add(operation);
            queuedWriteCallbacks.add(callback);
        } else {
            writeOperations.add(operation);
            callResult(callback);
        }
    }

    <T> void callResult(final SingleResultCallback<T> callback) {
        Object response = responses.remove(0);
        if (response instanceof Throwable) {
            callback.onResult(null, (Throwable) response);
        } else {
            callback.onResult((T) response, null);
        }
    }

    AsyncReadOperation getReadOperation() {
        return readOperations.isEmpty() ? null : readOperations.remove(0);
    }

    ReadPreference getReadPreference() {
        return readPreferences.isEmpty() ? null : readPreferences.remove(0);
    }

    AsyncWriteOperation getWriteOperation() {
        return writeOperations.isEmpty() ? null : writeOperations.remove(0);
    }

}
