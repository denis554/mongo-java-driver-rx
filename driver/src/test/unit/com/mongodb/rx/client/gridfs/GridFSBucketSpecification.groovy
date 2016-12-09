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

import spock.lang.Specification
import com.mongodb.async.client.gridfs.GridFSBucket as WrappedGridFSBucket

class GridFSBucketSpecification extends Specification {

    def 'should have the same methods as the wrapped GridFSBucket'() {
        given:
        def wrapped = (WrappedGridFSBucket.methods*.name).sort().toSet()
        def local = (GridFSBucket.methods*.name - 'getObservableAdapter').sort().toSet()

        expect:
        wrapped == local
    }
}
