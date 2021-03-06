/*
 * Copyright 2015 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.rx.client.internal;

import com.mongodb.async.client.Observable;
import com.mongodb.async.client.Observer;
import com.mongodb.async.client.Subscription;
import com.mongodb.rx.client.ObservableAdapter;
import rx.Producer;
import rx.Subscriber;

import java.util.concurrent.atomic.AtomicBoolean;

final class RxObservables {

    @SuppressWarnings("deprecation")
    static <TResult> rx.Observable<TResult> create(final Observable<TResult> observable, final ObservableAdapter observableAdapter) {
        return observableAdapter.adapt(rx.Observable.create(new rx.Observable.OnSubscribe<TResult>() {
            @Override
            public void call(final Subscriber<? super TResult> subscriber) {
                new ObservableToProducer<TResult>(observable, subscriber);
            }
        }));
    }

    static final class ObservableToProducer<TResult> implements Producer {
        private volatile Subscription subscription;
        private final Subscriber<? super TResult> rxSubscriber;

        public ObservableToProducer(final Observable<TResult> observable, final Subscriber<? super TResult> rxSubscriber) {
            this.rxSubscriber = rxSubscriber;
            observable.subscribe(new Observer<TResult>() {
                @Override
                public void onSubscribe(final Subscription s) {
                    subscription = s;
                    rxSubscriber.add(new rx.Subscription() {
                        private final AtomicBoolean unsubscribed = new AtomicBoolean();
                        @Override
                        public void unsubscribe() {
                            if (!unsubscribed.getAndSet(true)) {
                                subscription.unsubscribe();
                            }
                        }

                        @Override
                        public boolean isUnsubscribed() {
                            return unsubscribed.get();
                        }
                    });
                }

                @Override
                public void onNext(final TResult tResult) {
                    if (isSubscribed()) {
                        rxSubscriber.onNext(tResult);
                    }
                }

                @Override
                public void onError(final Throwable t) {
                    if (isSubscribed()) {
                        rxSubscriber.onError(t);
                    }
                }

                @Override
                public void onComplete() {
                    if (isSubscribed()) {
                        rxSubscriber.onCompleted();
                    }
                }
            });
            rxSubscriber.setProducer(this);
        }

        @Override
        public void request(final long n) {
            if (n == 0) {
                return;
            }
            if (isSubscribed()) {
                try {
                    subscription.request(n);
                } catch (Throwable t) {
                    rxSubscriber.onError(t);
                }
            }
        }

        private boolean isSubscribed() {
            return !rxSubscriber.isUnsubscribed();
        }
    }

    private RxObservables() {
    }
}
