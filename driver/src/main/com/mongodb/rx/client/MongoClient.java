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

package com.mongodb.rx.client;

import com.mongodb.annotations.Immutable;
import com.mongodb.async.client.MongoClientSettings;
import org.bson.Document;
import rx.Observable;

import java.io.Closeable;

/**
 * A client-side representation of a MongoDB cluster.  Instances can represent either a standalone MongoDB instance, a replica set,
 * or a sharded cluster.  Instance of this class are responsible for maintaining an up-to-date state of the cluster,
 * and possibly cache resources related to this, including background threads for monitoring, and connection pools.
 * <p>
 * Instance of this class server as factories for {@code MongoDatabase} instances.
 * </p>
 *
 * @since 1.0
 */
@Immutable
public interface MongoClient extends Closeable {

    /**
     * Gets the database with the given name.
     *
     * @param name the name of the database
     * @return the database
     */
    MongoDatabase getDatabase(String name);

    /**
     * Close the client, which will close all underlying cached resources, including, for example,
     * sockets and background monitoring threads.
     */
    void close();

    /**
     * Gets the settings that this client uses to connect to server.
     *
     * <p>Note: {@link MongoClientSettings} is immutable.</p>
     *
     * @return the settings
     */
    MongoClientSettings getSettings();

    /**
     * Get a list of the database names
     *
     * @mongodb.driver.manual reference/commands/listDatabases List Databases
     * @return an iterable containing all the names of all the databases
     */
    Observable<String> listDatabaseNames();

    /**
     * Gets the list of databases
     *
     * @return the fluent list databases interface
     */
    ListDatabasesObservable<Document> listDatabases();

    /**
     * Gets the list of databases
     *
     * @param clazz the class to cast the database documents to
     * @param <TResult>   the type of the class to use instead of {@code Document}.
     * @return the fluent list databases interface
     */
    <TResult> ListDatabasesObservable<TResult> listDatabases(Class<TResult> clazz);

}
