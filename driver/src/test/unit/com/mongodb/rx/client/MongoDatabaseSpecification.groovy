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

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.async.client.MongoCollection as WrappedMongoCollection
import com.mongodb.async.client.MongoDatabase as WrappedMongoDatabase
import com.mongodb.client.model.CreateCollectionOptions
import org.bson.BsonDocument
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import rx.observers.TestSubscriber
import spock.lang.Specification

import static com.mongodb.rx.client.CustomMatchers.isTheSameAs
import static spock.util.matcher.HamcrestSupport.expect

class MongoDatabaseSpecification extends Specification {

    def subscriber = {
        def subscriber = new TestSubscriber()
        subscriber.requestMore(1)
        subscriber
    }

    def 'should have the same methods as the wrapped MongoDatabase'() {
        given:
        def wrapped = WrappedMongoDatabase.methods*.name.sort()
        def local = MongoDatabase.methods*.name.sort()

        expect:
        wrapped == local
    }

    def 'should return the a collection'() {
        given:
        def wrappedCollection = Mock(WrappedMongoCollection)
        def wrapped = Mock(WrappedMongoDatabase) {
            getCollection(_) >> wrappedCollection
            getCollection(_, _) >> wrappedCollection
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def collection = mongoDatabase.getCollection('collectionName')

        then:
        expect collection, isTheSameAs(new MongoCollectionImpl(wrappedCollection))

        when:
        collection = mongoDatabase.getCollection('collectionName', Document)

        then:
        expect collection, isTheSameAs(new MongoCollectionImpl(wrappedCollection))
    }

    def 'should call the underlying getName'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.getName()

        then:
        1 * wrapped.getName()
    }

    def 'should call the underlying getCodecRegistry'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.getCodecRegistry()

        then:
        1 * wrapped.getCodecRegistry()
    }
    def 'should call the underlying getReadPreference'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.getReadPreference()

        then:
        1 * wrapped.getReadPreference()

    }
    def 'should call the underlying getWriteConcern'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.getWriteConcern()

        then:
        1 * wrapped.getWriteConcern()
    }

    def 'should call the underlying getReadConcern'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.getReadConcern()

        then:
        1 * wrapped.getReadConcern()
    }

    def 'should call the underlying withCodecRegistry'() {
        given:
        def codecRegistry = Stub(CodecRegistry)
        def wrappedResult = Stub(WrappedMongoDatabase)
        def wrapped = Mock(WrappedMongoDatabase) {
            1 * withCodecRegistry(codecRegistry) >> wrappedResult
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def result = mongoDatabase.withCodecRegistry(codecRegistry)

        then:
        expect result, isTheSameAs(new MongoDatabaseImpl(wrappedResult))
    }

    def 'should call the underlying withReadPreference'() {
        given:
        def readPreference = Stub(ReadPreference)
        def wrappedResult = Stub(WrappedMongoDatabase)
        def wrapped = Mock(WrappedMongoDatabase) {
            1 * withReadPreference(readPreference) >> wrappedResult
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def result = mongoDatabase.withReadPreference(readPreference)

        then:
        expect result, isTheSameAs(new MongoDatabaseImpl(wrappedResult))
    }

    def 'should call the underlying withWriteConcern'() {
        given:
        def writeConcern = Stub(WriteConcern)
        def wrappedResult = Stub(WrappedMongoDatabase)
        def wrapped = Mock(WrappedMongoDatabase) {
            1 * withWriteConcern(writeConcern) >> wrappedResult
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def result = mongoDatabase.withWriteConcern(writeConcern)

        then:
        expect result, isTheSameAs(new MongoDatabaseImpl(wrappedResult))
    }

    def 'should call the underlying withReadConcern'() {
        given:
        def readConcern = ReadConcern.MAJORITY
        def wrappedResult = Stub(WrappedMongoDatabase)
        def wrapped = Mock(WrappedMongoDatabase) {
            1 * withReadConcern(readConcern) >> wrappedResult
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def result = mongoDatabase.withReadConcern(readConcern)

        then:
        expect result, isTheSameAs(new MongoDatabaseImpl(wrappedResult))
    }

    def 'should call the underlying runCommand when writing'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.runCommand(new Document())

        then: 'only executed when requested'
        0 * wrapped.runCommand(_, _, _)

        when:
        mongoDatabase.runCommand(new Document()).subscribe(subscriber())

        then:
        1 * wrapped.runCommand(new Document(), Document, _)

        when:
        mongoDatabase.runCommand(new BsonDocument(), BsonDocument).subscribe(subscriber())

        then:
        1 * wrapped.runCommand(new BsonDocument(), BsonDocument, _)
    }
    def 'should call the underlying runCommand for read operations'() {
        given:
        def readPreference = Stub(ReadPreference)
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.runCommand(new Document(), readPreference)

        then: 'only executed when requested'
        0 * wrapped.runCommand(_, _, _, _)

        when:
        mongoDatabase.runCommand(new Document(), readPreference).subscribe(subscriber())

        then:
        1 * wrapped.runCommand(new Document(), readPreference, Document, _)

        when:
        mongoDatabase.runCommand(new BsonDocument(), readPreference, BsonDocument).subscribe(subscriber())

        then:
        1 * wrapped.runCommand(new BsonDocument(), readPreference, BsonDocument, _)
    }

    def 'should call the underlying drop'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.drop()

        then: 'only executed when requested'
        0 * wrapped.drop(_)

        when:
        mongoDatabase.drop().subscribe(subscriber())

        then:
        1 * wrapped.drop(_)
    }
    def 'should call the underlying listCollectionNames'() {
        given:
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.listCollectionNames()

        then:
        1 * wrapped.listCollectionNames()

    }
    def 'should call the underlying listCollections'() {
        given:
        def wrappedResult = Stub(com.mongodb.async.client.ListCollectionsIterable)
        def wrapped = Mock(WrappedMongoDatabase) {
            1 * listCollections(Document) >> wrappedResult
            1 * listCollections(BsonDocument) >> wrappedResult
        }
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        def observable = mongoDatabase.listCollections()

        then:
        expect observable, isTheSameAs(new ListCollectionsObservableImpl(wrappedResult))

        when:
        observable = mongoDatabase.listCollections(BsonDocument)

        then:
        expect observable, isTheSameAs(new ListCollectionsObservableImpl(wrappedResult))
    }

    def 'should call the underlying createCollection'() {
        given:
        def createCollectionOptions = Stub(CreateCollectionOptions)
        def wrapped = Mock(WrappedMongoDatabase)
        def mongoDatabase = new MongoDatabaseImpl(wrapped)

        when:
        mongoDatabase.createCollection('collectionName')

        then: 'only executed when requested'
        0 * wrapped.createCollection(_, _, _)

        when:
        mongoDatabase.createCollection('collectionName').subscribe(subscriber())

        then:
        1 * wrapped.createCollection('collectionName', _, _)

        when:
        mongoDatabase.createCollection('collectionName', createCollectionOptions).subscribe(subscriber())

        then:
        1 * wrapped.createCollection('collectionName', createCollectionOptions, _)
    }

}
