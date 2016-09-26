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

package com.mongodb.rx.client;


import com.mongodb.client.model.Collation;
import com.mongodb.client.model.MapReduceAction;
import org.bson.conversions.Bson;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Observable for map reduce.
 *
 * @param <TResult> The type of the result.
 * @since 1.0
 */
public interface MapReduceObservable<TResult> extends MongoObservable<TResult> {

    /**
     * Sets the collectionName for the output of the MapReduce
     *
     * <p>The default action is replace the collection if it exists, to change this use {@link #action}.</p>
     *
     * @param collectionName the name of the collection that you want the map-reduce operation to write its output.
     * @return this
     */
    MapReduceObservable<TResult> collectionName(String collectionName);

    /**
     * Sets the JavaScript function that follows the reduce method and modifies the output.
     *
     * @param finalizeFunction the JavaScript function that follows the reduce method and modifies the output.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#mapreduce-finalize-cmd Requirements for the finalize Function
     */
    MapReduceObservable<TResult> finalizeFunction(String finalizeFunction);

    /**
     * Sets the global variables that are accessible in the map, reduce and finalize functions.
     *
     * @param scope the global variables that are accessible in the map, reduce and finalize functions.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce mapReduce
     */
    MapReduceObservable<TResult> scope(Bson scope);

    /**
     * Sets the sort criteria to apply to the query.
     *
     * @param sort the sort criteria, which may be null.
     * @return this
     * @mongodb.driver.manual reference/method/cursor.sort/ Sort
     */
    MapReduceObservable<TResult> sort(Bson sort);

    /**
     * Sets the query filter to apply to the query.
     *
     * @param filter the filter to apply to the query.
     * @return this
     * @mongodb.driver.manual reference/method/db.collection.find/ Filter
     */
    MapReduceObservable<TResult> filter(Bson filter);

    /**
     * Sets the limit to apply.
     *
     * @param limit the limit, which may be null
     * @return this
     * @mongodb.driver.manual reference/method/cursor.limit/#cursor.limit Limit
     */
    MapReduceObservable<TResult> limit(int limit);

    /**
     * Sets the flag that specifies whether to convert intermediate data into BSON format between the execution of the map and reduce
     * functions. Defaults to false.
     *
     * @param jsMode the flag that specifies whether to convert intermediate data into BSON format between the execution of the map and
     *               reduce functions
     * @return jsMode
     * @mongodb.driver.manual reference/command/mapReduce mapReduce
     */
    MapReduceObservable<TResult> jsMode(boolean jsMode);

    /**
     * Sets whether to include the timing information in the result information.
     *
     * @param verbose whether to include the timing information in the result information.
     * @return this
     */
    MapReduceObservable<TResult> verbose(boolean verbose);

    /**
     * Sets the maximum execution time on the server for this operation.
     *
     * @param maxTime  the max time
     * @param timeUnit the time unit, which may not be null
     * @return this
     * @mongodb.driver.manual reference/method/cursor.maxTimeMS/#cursor.maxTimeMS Max Time
     */
    MapReduceObservable<TResult> maxTime(long maxTime, TimeUnit timeUnit);

    /**
     * Specify the {@code MapReduceAction} to be used when writing to a collection.
     *
     * @param action an {@link com.mongodb.client.model.MapReduceAction} to perform on the collection
     * @return this
     */
    MapReduceObservable<TResult> action(MapReduceAction action);

    /**
     * Sets the name of the database to output into.
     *
     * @param databaseName the name of the database to output into.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    MapReduceObservable<TResult> databaseName(String databaseName);
    /**
     * Sets if the output database is sharded
     *
     * @param sharded if the output database is sharded
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    MapReduceObservable<TResult> sharded(boolean sharded);

    /**
     * Sets if the post-processing step will prevent MongoDB from locking the database.
     *
     * Valid only with the {@code MapReduceAction.MERGE} or {@code MapReduceAction.REDUCE} actions.
     *
     * @param nonAtomic if the post-processing step will prevent MongoDB from locking the database.
     * @return this
     * @mongodb.driver.manual reference/command/mapReduce/#output-to-a-collection-with-an-action output with an action
     */
    MapReduceObservable<TResult> nonAtomic(boolean nonAtomic);

    /**
     * Sets the bypass document level validation flag.
     *
     * <p>Note: This only applies when an $out stage is specified</p>.
     *
     * @param bypassDocumentValidation If true, allows the write to opt-out of document level validation.
     * @return this
     * @since 1.2
     * @mongodb.driver.manual reference/command/mapReduce mapReduce
     * @mongodb.server.release 3.2
     */
    MapReduceObservable<TResult> bypassDocumentValidation(Boolean bypassDocumentValidation);

    /**
     * Aggregates documents to a collection according to the specified map-reduce function with the given options, which must specify a
     * non-inline result.
     *
     * @return an Observable with a single element indicating when the operation has completed
     * @mongodb.driver.manual aggregation/ Aggregation
     */
    Observable<Success> toCollection();

    /**
     * Sets the collation options
     *
     * <p>A null value represents the server default.</p>
     * @param collation the collation options to use
     * @return this
     * @since 1.3
     * @mongodb.server.release 3.4
     */
    MapReduceObservable<TResult> collation(Collation collation);
}
