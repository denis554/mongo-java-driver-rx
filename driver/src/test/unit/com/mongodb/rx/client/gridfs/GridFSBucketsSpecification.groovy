/*
 * Copyright 2016 MongoDB, Inc.
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

package com.mongodb.rx.client.gridfs

import com.mongodb.async.client.MongoDatabase as WrappedMongoDatabase
import com.mongodb.async.client.gridfs.GridFSBuckets as WrappedGridFSBuckets
import com.mongodb.rx.client.MongoDatabase
import com.mongodb.rx.client.ObservableAdapter
import com.mongodb.rx.client.internal.MongoDatabaseImpl
import spock.lang.Specification

class GridFSBucketsSpecification extends Specification {

    def wrapped = Stub(WrappedMongoDatabase)
    def observableAdapter = Stub(ObservableAdapter)

    def 'should have the same methods as the wrapped GridFSBuckets'() {
        given:
        def wrapped = (WrappedGridFSBuckets.methods*.name).sort()
        def local = (GridFSBuckets.methods*.name).sort()

        expect:
        wrapped == local
    }

    def 'should have the default bucket name'() {
        when:
        def database = new MongoDatabaseImpl(wrapped, observableAdapter)
        GridFSBuckets.create(database)

        then:
        def gridFSBucket = GridFSBuckets.create(database)
        gridFSBucket.getBucketName() == 'fs'
    }

    def 'should set the bucket name'() {
        when:
        def database = new MongoDatabaseImpl(wrapped, observableAdapter)
        def gridFSBucket = GridFSBuckets.create(database, 'test')

        then:
        gridFSBucket.getBucketName() == 'test'
    }

    def 'should set throw an IllegalArgumentException not passed a MongoDatabaseImpl'() {
        given:
        def database = Stub(MongoDatabase)

        when:
        GridFSBuckets.create(database)

        then:
        thrown(IllegalArgumentException)

        when:
        GridFSBuckets.create(database, 'test')

        then:
        thrown(IllegalArgumentException)
    }

}
