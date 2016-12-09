/*
 * Copyright 2014-2015 MongoDB, Inc.
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

package com.mongodb.rx.client.internal

import com.mongodb.async.client.ListDatabasesIterable
import com.mongodb.async.client.MongoClient as WrappedMongoClient
import com.mongodb.rx.client.ObservableAdapter
import org.bson.BsonDocument
import org.bson.Document
import spock.lang.Specification

import static com.mongodb.rx.client.CustomMatchers.isTheSameAs
import static spock.util.matcher.HamcrestSupport.expect

class MongoClientImplSpecification extends Specification {

    def wrapped = Mock(WrappedMongoClient)
    def observableAdapter = Stub(ObservableAdapter)
    def mongoClient = new MongoClientImpl(wrapped, observableAdapter)

    def 'should call the underlying getSettings'(){
        when:
        mongoClient.getSettings()

        then:
        1 * wrapped.getSettings()
    }

    def 'should call the underlying listDatabases'() {
        given:
        def wrappedResult = Stub(ListDatabasesIterable)
        def wrapped = Mock(WrappedMongoClient) {
            1 * listDatabases(Document) >> wrappedResult
            1 * listDatabases(BsonDocument) >> wrappedResult
        }
        def mongoClient = new MongoClientImpl(wrapped, observableAdapter)


        when:
        def observable = mongoClient.listDatabases()

        then:
        expect observable, isTheSameAs(new ListDatabasesObservableImpl(wrappedResult, observableAdapter))

        when:
        observable = mongoClient.listDatabases(BsonDocument)

        then:
        expect observable, isTheSameAs(new ListDatabasesObservableImpl(wrappedResult, observableAdapter))
    }

    def 'should call the underlying listDatabaseNames'() {
        when:
        mongoClient.listDatabaseNames()

        then:
        1 * wrapped.listDatabaseNames()
    }

}
