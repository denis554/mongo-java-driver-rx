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

import org.bson.conversions.Bson;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

import java.util.concurrent.TimeUnit;

import static com.mongodb.assertions.Assertions.notNull;

final class ListCollectionsObservableImpl<TResult> implements ListCollectionsObservable<TResult> {

    private final com.mongodb.async.client.ListCollectionsIterable<TResult> wrapped;

    ListCollectionsObservableImpl(final com.mongodb.async.client.ListCollectionsIterable<TResult> wrapped) {
        this.wrapped = notNull("wrapped", wrapped);
    }

    @Override
    public ListCollectionsObservable<TResult> filter(final Bson filter) {
        notNull("filter", filter);
        wrapped.filter(filter);
        return this;
    }

    @Override
    public ListCollectionsObservable<TResult> maxTime(final long maxTime, final TimeUnit timeUnit) {
        notNull("timeUnit", timeUnit);
        wrapped.maxTime(maxTime, timeUnit);
        return this;
    }

    @Override
    public Observable<TResult> toObservable() {
        return MongoIterableObservable.create(wrapped);
    }

    @Override
    public Subscription subscribe(final Subscriber<? super TResult> subscriber) {
        return toObservable().subscribe(subscriber);
    }
}
