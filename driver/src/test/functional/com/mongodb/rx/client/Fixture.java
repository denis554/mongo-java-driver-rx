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

import com.mongodb.ConnectionString;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoNamespace;
import com.mongodb.rx.client.internal.MongoClientImpl;
import org.bson.Document;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Helper class for asynchronous tests.
 */
public final class Fixture {
    public static final String DEFAULT_URI = "mongodb://localhost:27017";
    public static final String MONGODB_URI_SYSTEM_PROPERTY_NAME = "org.mongodb.test.uri";
    private static final String DEFAULT_DATABASE_NAME = "JavaDriverReactiveTest";

    private static ConnectionString connectionString;
    private static MongoClientImpl mongoClient;

    private Fixture() {
    }

    public static synchronized MongoClient getMongoClient() {
        if (mongoClient == null) {
            mongoClient = (MongoClientImpl) MongoClients.create(getConnectionString());
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }
        return mongoClient;
    }

    public static synchronized ConnectionString getConnectionString() {
        if (connectionString == null) {
            String mongoURIProperty = System.getProperty(MONGODB_URI_SYSTEM_PROPERTY_NAME);
            String mongoURIString = mongoURIProperty == null || mongoURIProperty.isEmpty()
                    ? DEFAULT_URI : mongoURIProperty;
            connectionString = new ConnectionString(mongoURIString);
        }
        return connectionString;
    }

    public static String getDefaultDatabaseName() {
        return DEFAULT_DATABASE_NAME;
    }

    public static MongoDatabase getDefaultDatabase() {
        return getMongoClient().getDatabase(getDefaultDatabaseName());
    }

    public static MongoCollection<Document> initializeCollection(final MongoNamespace namespace) throws Throwable {
        MongoDatabase database = getMongoClient().getDatabase(namespace.getDatabaseName());
        try {
            database.runCommand(new Document("drop", namespace.getCollectionName())).timeout(10, SECONDS).toBlocking().first();
        } catch (MongoCommandException e) {
            if (!e.getErrorMessage().startsWith("ns not found")) {
                throw e;
            }
        }
        return database.getCollection(namespace.getCollectionName());
    }

    public static void dropDatabase(final String name) throws Throwable {
        if (name == null) {
            return;
        }
        try {
            getMongoClient().getDatabase(name).runCommand(new Document("dropDatabase", 1)).timeout(10, SECONDS).toBlocking().first();
        } catch (MongoCommandException e) {
            if (!e.getErrorMessage().startsWith("ns not found")) {
                throw e;
            }
        }
    }

    public static void drop(final MongoNamespace namespace) throws Throwable {
        try {
            getMongoClient().getDatabase(namespace.getDatabaseName())
                    .runCommand(new Document("drop", namespace.getCollectionName())).timeout(10, SECONDS).toBlocking().first();
        } catch (MongoCommandException e) {
            if (!e.getErrorMessage().contains("ns not found")) {
                throw e;
            }
        }
    }

    static class ShutdownHook extends Thread {
        @Override
        public void run() {
            try {
                dropDatabase(getDefaultDatabaseName());
            } catch (Throwable e) {
                // ignore
            }
            mongoClient.close();
            mongoClient = null;
        }
    }

}
