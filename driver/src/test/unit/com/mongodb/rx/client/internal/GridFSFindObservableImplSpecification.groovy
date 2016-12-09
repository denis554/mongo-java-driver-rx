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

package com.mongodb.rx.client.internal

import com.mongodb.async.client.gridfs.GridFSFindIterable
import com.mongodb.client.model.Collation
import com.mongodb.rx.client.ObservableAdapter
import org.bson.Document
import rx.observers.TestSubscriber
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.MILLISECONDS

class GridFSFindObservableImplSpecification extends Specification {

    def 'should call the wrapped methods'() {
        given:
        def collation = Collation.builder().locale('en').build()
        def filter = new Document('filter', 2)
        def sort = new Document('sort', 2)
        def subscriber = { new TestSubscriber() }

        def wrapped = Mock(GridFSFindIterable)
        def observableAdapter = Mock(ObservableAdapter)
        def observable = new GridFSFindObservableImpl(wrapped, observableAdapter)

        when:
        observable = observable
                .filter(filter)
                .sort(sort)
                .maxTime(999, MILLISECONDS)
                .limit(99)
                .skip(9)
                .noCursorTimeout(true)
                .collation(collation)

        then:
        1 * wrapped.filter(filter) >> { wrapped }
        1 * wrapped.sort(sort) >> { wrapped }
        1 * wrapped.maxTime(999, MILLISECONDS) >> { wrapped }
        1 * wrapped.limit(99) >> { wrapped }
        1 * wrapped.skip(9) >> { wrapped }
        1 * wrapped.noCursorTimeout(true) >> { wrapped }
        1 * wrapped.collation(collation) >> { wrapped }

        when:
        observable.subscribe(subscriber())

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }
        1 * wrapped.batchCursor(_)

        when:
        observable = observable.first()

        then:
        1 * observableAdapter.adapt(_) >> { args -> args[0] }

        when:
        observable.subscribe(subscriber())

        then:
        1 * wrapped.first(_) >> { wrapped }
    }

}
