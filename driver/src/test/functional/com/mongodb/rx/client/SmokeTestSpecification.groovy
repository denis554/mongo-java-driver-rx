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

import com.mongodb.MongoNamespace
import com.mongodb.client.model.IndexModel
import com.mongodb.diagnostics.logging.Loggers
import org.bson.Document
import rx.Observable
import rx.Subscriber
import rx.observers.TestSubscriber

import java.util.concurrent.atomic.AtomicInteger

import static Fixture.getMongoClient
import static java.util.concurrent.TimeUnit.SECONDS

class SmokeTestSpecification extends FunctionalSpecification {

    private static final LOGGER = Loggers.getLogger('smokeTest')

    def 'should handle common scenarios without error'() {
        given:
        def mongoClient = getMongoClient()
        def database = mongoClient.getDatabase(databaseName)
        def document = new Document('_id', 1)
        def updatedDocument = new Document('_id', 1).append('a', 1)

        when:
        run('clean up old database', mongoClient.getDatabase(databaseName).drop()).head() == Success.SUCCESS
        def names = run('get database names', mongoClient.listDatabaseNames())

        then: 'Get Database Names'
        !names.contains(null)

        then:
        run('Create a collection and the created database is in the list',
                database.createCollection(collectionName)).head() == Success.SUCCESS

        when:
        def updatedNames = run('get database names', mongoClient.listDatabaseNames())

        then: 'The database names should contain the database and be one bigger than before'
        updatedNames.contains(databaseName)
        updatedNames.size() == names.size() + 1

        when:
        def collectionNames = run('The collection name should be in the collection names list', database.listCollectionNames())

        then:
        !collectionNames.contains(null)
        collectionNames.contains(collectionName)

        then:
        run('The count is zero', collection.count()).head() == 0

        then:
        run('find first should return nothing if no documents', collection.find().first()) == []

        then:
        run('Insert a document', collection.insertOne(document)).head() == Success.SUCCESS

        then:
        run('The count is one', collection.count()).head() == 1

        then:
        run('find that document', collection.find().first()).head() == document

        then:
        run('update that document', collection.updateOne(document, new Document('$set', new Document('a', 1)))).head().wasAcknowledged()

        then:
        run('find the updated document', collection.find().first()).head() == updatedDocument

        then:
        run('aggregate the collection', collection.aggregate([new Document('$match', new Document('a', 1))])).head() == updatedDocument

        then:
        run('remove all documents', collection.deleteOne(new Document())).head().getDeletedCount() == 1

        then:
        run('The count is zero', collection.count()).head() == 0

        then:
        run('create an index', collection.createIndex(new Document('test', 1))).head() == 'test_1'

        then:
        def indexNames = run('has the newly created index', collection.listIndexes())*.name

        then:
        indexNames.containsAll('_id_', 'test_1')

        then:
        run('create multiple indexes', collection.createIndexes([new IndexModel(new Document('multi', 1))])).head() == 'multi_1'

        then:
        def indexNamesUpdated = run('has the newly created index', collection.listIndexes())*.name

        then:
        indexNamesUpdated.containsAll('_id_', 'test_1', 'multi_1')

        then:
        run('drop the index', collection.dropIndex('multi_1')).head() == Success.SUCCESS

        then:
        run('has a single index left "_id" ', collection.listIndexes()).size == 2

        then:
        run('drop the index', collection.dropIndex('test_1')).head() == Success.SUCCESS

        then:
        run('has a single index left "_id" ', collection.listIndexes()).size == 1

        then:
        def newCollectionName = 'new' + collectionName.capitalize()
        run('can rename the collection', collection.renameCollection(new MongoNamespace(databaseName, newCollectionName))
            ).head() == Success.SUCCESS

        then:
        !run('the new collection name is in the collection names list', database.listCollectionNames()).contains(collectionName)
        run('get collection names', database.listCollectionNames()).contains(newCollectionName)

        when:
        collection = database.getCollection(newCollectionName)

        then:
        run('drop the collection', collection.drop()).head() == Success.SUCCESS

        then:
        run('there are no indexes', collection.listIndexes()).size == 0

        then:
        !run('the collection name is no longer in the collectionNames list', database.listCollectionNames()).contains(collectionName)
    }

    @SuppressWarnings('BusyWait')
    def 'should visit all documents from a cursor with multiple batches'() {
        given:
        def totalReceived = new AtomicInteger();
        def batches = new AtomicInteger();
        def received = new AtomicInteger();
        def batchSize = 100
        def total = 1000
        def documents = (1..total).collect { new Document('_id', it) }
        run('Insert 10000 documents', collection.insertMany(documents))

        def subscriber = new TestSubscriber(new Subscriber<Document>() {

            @Override
            void onStart() {
                request(batchSize);
            }

            @Override
            void onCompleted() {
            }

            @Override
            void onError(Throwable t) {
            }

            @Override
            void onNext(Document doc) {
                def recievedTotal = totalReceived.incrementAndGet();
                received.incrementAndGet();
                if (recievedTotal >= total) {
                    unsubscribe();
                }
                if (received.get() == batchSize) {
                    batches.incrementAndGet();
                    request(batchSize);
                    received.set(0);
                }
            }

        })

        when:
        collection.find(new Document()).sort(new Document('_id', 1)).subscribe(subscriber);

        then:
        subscriber.awaitTerminalEvent(30, SECONDS)
        totalReceived.get() == total
        batches.get() == (total / batchSize).intValue()
    }

    def run(String log, MongoObservable mongoObservable) {
        run(log, mongoObservable.toObservable())
    }

    def run(String log, Observable observable) {
        LOGGER.debug(log);
        observable.timeout(30, SECONDS).toList().toBlocking().first()
    }

}
