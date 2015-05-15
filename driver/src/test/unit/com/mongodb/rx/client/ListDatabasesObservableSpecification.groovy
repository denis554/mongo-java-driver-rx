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

package com.mongodb.rx.client

import com.mongodb.async.client.ListDatabasesIterableImpl as WrappedListDatabasesIterableImpl
import com.mongodb.async.client.MongoIterable
import com.mongodb.operation.ListDatabasesOperation
import org.bson.Document
import org.bson.codecs.DocumentCodec
import org.bson.codecs.DocumentCodecProvider
import rx.observers.TestSubscriber
import spock.lang.Specification

import static com.mongodb.ReadPreference.secondary
import static com.mongodb.rx.client.CustomMatchers.isTheSameAs
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static org.bson.codecs.configuration.CodecRegistries.fromProviders
import static spock.util.matcher.HamcrestSupport.expect

class ListDatabasesObservableSpecification extends Specification {

    def 'should have the same methods as the wrapped ListDatabasesIterable'() {
        given:
        def wrapped = (com.mongodb.async.client.ListDatabasesIterable.methods*.name - MongoIterable.methods*.name).sort()
        def local = (ListDatabasesObservable.methods*.name - MongoObservable.methods*.name  - 'batchSize').sort()

        expect:
        wrapped == local
    }

    def 'should build the expected ListDatabasesOperation'() {
        given:
        def subscriber = new TestSubscriber()
        subscriber.requestMore(100)
        def codecRegistry = fromProviders([new DocumentCodecProvider()])
        def executor = new TestOperationExecutor([null, null]);
        def wrapped = new WrappedListDatabasesIterableImpl(Document, codecRegistry, secondary(), executor)
        def listDatabasesObservable = new ListDatabasesObservableImpl<Document>(wrapped)

        when: 'default input should be as expected'
        listDatabasesObservable.subscribe(subscriber)

        def operation = executor.getReadOperation() as ListDatabasesOperation<Document>
        def readPreference = executor.getReadPreference()

        then:
        expect operation, isTheSameAs(new ListDatabasesOperation<Document>(new DocumentCodec()))
        readPreference == secondary()

        when: 'overriding initial options'
        subscriber = new TestSubscriber()
        subscriber.requestMore(100)
        listDatabasesObservable.maxTime(999, MILLISECONDS).subscribe(subscriber)

        operation = executor.getReadOperation() as ListDatabasesOperation<Document>

        then: 'should use the overrides'
        expect operation, isTheSameAs(new ListDatabasesOperation<Document>(new DocumentCodec()).maxTime(999, MILLISECONDS))
    }

}
