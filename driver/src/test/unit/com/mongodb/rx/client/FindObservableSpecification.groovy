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

import com.mongodb.async.client.FindIterable
import com.mongodb.async.client.MongoIterable
import spock.lang.Specification

class FindObservableSpecification extends Specification {

    def 'should have the same methods as the wrapped FindIterable'() {
        given:
        def wrapped = (FindIterable.methods*.name - MongoIterable.methods*.name).sort()
        def local = (FindObservable.methods*.name - MongoObservable.methods*.name - 'first' - 'batchSize').sort()

        expect:
        wrapped == local
    }

}
