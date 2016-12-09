/*
 * Copyright 2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.rx.client.gridfs;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.annotations.ThreadSafe;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.rx.client.ObservableAdapter;
import com.mongodb.rx.client.Success;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import rx.Observable;

/**
 * Represents a GridFS Bucket
 *
 * @since 1.3
 */
@ThreadSafe
public interface GridFSBucket {

    /**
     * The bucket name.
     *
     * @return the bucket name
     */
    String getBucketName();

    /**
     * Sets the chunk size in bytes. Defaults to 255.
     *
     * @return the chunk size in bytes.
     */
    int getChunkSizeBytes();

    /**
     * Get the ObservableAdapter for the GridFSBucket.
     *
     * @return the {@link ObservableAdapter}
     */
    ObservableAdapter getObservableAdapter();

    /**
     * Get the write concern for the GridFSBucket.
     *
     * @return the {@link com.mongodb.WriteConcern}
     */
    WriteConcern getWriteConcern();

    /**
     * Get the read preference for the GridFSBucket.
     *
     * @return the {@link com.mongodb.ReadPreference}
     */
    ReadPreference getReadPreference();

    /**
     * Get the read concern for the GridFSBucket.
     *
     * @return the {@link com.mongodb.ReadConcern}
     * @mongodb.server.release 3.2
     * @mongodb.driver.manual reference/readConcern/ Read Concern
     */
    ReadConcern getReadConcern();

    /**
     * Create a new GridFSBucket instance with a new chunk size in bytes.
     *
     * @param chunkSizeBytes the new chunk size in bytes.
     * @return a new GridFSBucket instance with the different chunk size in bytes
     */
    GridFSBucket withChunkSizeBytes(int chunkSizeBytes);

    /**
     * Create a new GridFSBucket instance with a different read preference.
     *
     * @param readPreference the new {@link ReadPreference} for the database
     * @return a new GridFSBucket instance with the different readPreference
     */
    GridFSBucket withReadPreference(ReadPreference readPreference);

    /**
     * Create a new GridFSBucket instance with a different write concern.
     *
     * @param writeConcern the new {@link WriteConcern} for the database
     * @return a new GridFSBucket instance with the different writeConcern
     */
    GridFSBucket withWriteConcern(WriteConcern writeConcern);

    /**
     * Create a new MongoDatabase instance with a different read concern.
     *
     * @param readConcern the new {@link ReadConcern} for the database
     * @return a new GridFSBucket instance with the different ReadConcern
     * @mongodb.server.release 3.2
     * @mongodb.driver.manual reference/readConcern/ Read Concern
     */
    GridFSBucket withReadConcern(ReadConcern readConcern);

    /**
     * Opens a AsyncOutputStream that the application can write the contents of the file to.
     * <p>
     * As the application writes the contents to the returned Stream, the contents are uploaded as chunks in the chunks collection. When
     * the application signals it is done writing the contents of the file by calling close on the returned Stream, a files collection
     * document is created in the files collection.
     * </p>
     *
     * @param filename the filename for the stream
     * @return the GridFSUploadStream that provides the ObjectId for the file to be uploaded and the Stream to which the
     * application will write the contents.
     */
    GridFSUploadStream openUploadStream(String filename);

    /**
     * Opens a AsyncOutputStream that the application can write the contents of the file to.
     * <p>
     * As the application writes the contents to the returned Stream, the contents are uploaded as chunks in the chunks collection. When
     * the application signals it is done writing the contents of the file by calling close on the returned Stream, a files collection
     * document is created in the files collection.
     * </p>
     *
     * @param filename the filename for the stream
     * @param options  the GridFSUploadOptions
     * @return the GridFSUploadStream that provides the ObjectId for the file to be uploaded and the Stream to which the
     * application will write the contents.
     */
    GridFSUploadStream openUploadStream(String filename, GridFSUploadOptions options);

    /**
     * Opens a AsyncOutputStream that the application can write the contents of the file to.
     * <p>
     * As the application writes the contents to the returned Stream, the contents are uploaded as chunks in the chunks collection. When
     * the application signals it is done writing the contents of the file by calling close on the returned Stream, a files collection
     * document is created in the files collection.
     * </p>
     *
     * @param id the custom id value of the file
     * @param filename the filename for the stream
     * @return the GridFSUploadStream that provides the ObjectId for the file to be uploaded and the Stream to which the
     * application will write the contents.
     */
    GridFSUploadStream openUploadStream(BsonValue id, String filename);

    /**
     * Opens a AsyncOutputStream that the application can write the contents of the file to.
     * <p>
     * As the application writes the contents to the returned Stream, the contents are uploaded as chunks in the chunks collection. When
     * the application signals it is done writing the contents of the file by calling close on the returned Stream, a files collection
     * document is created in the files collection.
     * </p>
     *
     * @param id the custom id value of the file
     * @param filename the filename for the stream
     * @param options  the GridFSUploadOptions
     * @return the GridFSUploadStream that provides the ObjectId for the file to be uploaded and the Stream to which the
     * application will write the contents.
     */
    GridFSUploadStream openUploadStream(BsonValue id, String filename, GridFSUploadOptions options);

    /**
     * Uploads the contents of the given {@code AsyncInputStream} to a GridFS bucket.
     * <p>
     * Reads the contents of the user file from the {@code source} and uploads it as chunks in the chunks collection. After all the
     * chunks have been uploaded, it creates a files collection document for {@code filename} in the files collection.
     * </p>
     *
     * @param filename the filename for the stream
     * @param source   the Stream providing the file data
     * @return an observable with a single element, the ObjectId of the uploaded file.
     */
    Observable<ObjectId> uploadFromStream(String filename, AsyncInputStream source);

    /**
     * Uploads the contents of the given {@code AsyncInputStream} to a GridFS bucket.
     * <p>
     * Reads the contents of the user file from the {@code source} and uploads it as chunks in the chunks collection. After all the
     * chunks have been uploaded, it creates a files collection document for {@code filename} in the files collection.
     * </p>
     *
     * @param filename the filename for the stream
     * @param source   the Stream providing the file data
     * @param options  the GridFSUploadOptions
     * @return an observable with a single element, the ObjectId of the uploaded file.
     */
    Observable<ObjectId> uploadFromStream(String filename, AsyncInputStream source, GridFSUploadOptions options);

    /**
     * Uploads the contents of the given {@code AsyncInputStream} to a GridFS bucket.
     * <p>
     * Reads the contents of the user file from the {@code source} and uploads it as chunks in the chunks collection. After all the
     * chunks have been uploaded, it creates a files collection document for {@code filename} in the files collection.
     * </p>
     *
     * @param id the custom id value of the file
     * @param filename the filename for the stream
     * @param source   the Stream providing the file data
     * @return an observable with a single element, representing when the successful upload of the source.
     */
    Observable<Success> uploadFromStream(BsonValue id, String filename, AsyncInputStream source);

    /**
     * Uploads the contents of the given {@code AsyncInputStream} to a GridFS bucket.
     * <p>
     * Reads the contents of the user file from the {@code source} and uploads it as chunks in the chunks collection. After all the
     * chunks have been uploaded, it creates a files collection document for {@code filename} in the files collection.
     * </p>
     *
     * @param id       the custom id value of the file
     * @param filename the filename for the stream
     * @param source   the Stream providing the file data
     * @param options  the GridFSUploadOptions
     * @return an observable with a single element, representing when the successful upload of the source.
     */
    Observable<Success> uploadFromStream(BsonValue id, String filename, AsyncInputStream source, GridFSUploadOptions options);

    /**
     * Opens a AsyncInputStream from which the application can read the contents of the stored file specified by {@code id}.
     *
     * @param id the ObjectId of the file to be put into a stream.
     * @return the stream
     */
    GridFSDownloadStream openDownloadStream(ObjectId id);

    /**
     * Downloads the contents of the stored file specified by {@code id} and writes the contents to the {@code destination}
     * AsyncOutputStream.
     *
     * @param id          the ObjectId of the file to be written to the destination stream
     * @param destination the destination stream
     * @return an observable with a single element, representing the amount of data written
     */
    Observable<Long> downloadToStream(ObjectId id, AsyncOutputStream destination);

    /**
     * Opens a AsyncInputStream from which the application can read the contents of the stored file specified by {@code id}.
     *
     * @param id the custom id value of the file, to be put into a stream.
     * @return the stream
     */
    GridFSDownloadStream openDownloadStream(BsonValue id);

    /**
     * Downloads the contents of the stored file specified by {@code id} and writes the contents to the {@code destination}
     * AsyncOutputStream.
     *
     * @param id          the custom id of the file, to be written to the destination stream
     * @param destination the destination stream
     * @return an observable with a single element, representing the amount of data written
     */
    Observable<Long> downloadToStream(BsonValue id, AsyncOutputStream destination);

    /**
     * Opens a Stream from which the application can read the contents of the latest version of the stored file specified by the
     * {@code filename}.
     *
     * @param filename the name of the file to be downloaded
     * @return the stream
     */
    GridFSDownloadStream openDownloadStream(String filename);

    /**
     * Opens a Stream from which the application can read the contents of the stored file specified by {@code filename} and the revision
     * in {@code options}.
     *
     * @param filename the name of the file to be downloaded
     * @param options  the download options
     * @return the stream
     */
    GridFSDownloadStream openDownloadStream(String filename, GridFSDownloadOptions options);

    /**
     * Downloads the contents of the latest version of the stored file specified by {@code filename} and writes the contents to
     * the {@code destination} Stream.
     *
     * @param filename    the name of the file to be downloaded
     * @param destination the destination stream
     * @return an observable with a single element, representing the amount of data written
     */
    Observable<Long> downloadToStream(String filename, AsyncOutputStream destination);

    /**
     * Downloads the contents of the stored file specified by {@code filename} and by the revision in {@code options} and writes the
     * contents to the {@code destination} Stream.
     *
     * @param filename    the name of the file to be downloaded
     * @param destination the destination stream
     * @param options     the download options
     * @return an observable with a single element, representing the amount of data written
     */
    Observable<Long> downloadToStream(String filename, AsyncOutputStream destination, GridFSDownloadOptions options);

    /**
     * Finds all documents in the files collection.
     *
     * @return the GridFS find iterable interface
     * @mongodb.driver.manual tutorial/query-documents/ Find
     */
    GridFSFindObservable find();

    /**
     * Finds all documents in the collection that match the filter.
     * <p>
     * Below is an example of filtering against the filename and some nested metadata that can also be stored along with the file data:
     * <pre>
     *  {@code
     *      Filters.and(Filters.eq("filename", "mongodb.png"), Filters.eq("metadata.contentType", "image/png"));
     *  }
     *  </pre>
     *
     * @param filter the query filter
     * @return the GridFS find iterable interface
     * @see com.mongodb.client.model.Filters
     */
    GridFSFindObservable find(Bson filter);

    /**
     * Given a {@code id}, delete this stored file's files collection document and associated chunks from a GridFS bucket.
     *
     * @param id       the ObjectId of the file to be deleted
     * @return an observable with a single element, representing that the file has been deleted
     */
    Observable<Success> delete(ObjectId id);

    /**
     * Given a {@code id}, delete this stored file's files collection document and associated chunks from a GridFS bucket.
     *
     * @param id       the ObjectId of the file to be deleted
     * @return an observable with a single element, representing that the file has been deleted
     */
    Observable<Success> delete(BsonValue id);

    /**
     * Renames the stored file with the specified {@code id}.
     *
     * @param id          the id of the file in the files collection to rename
     * @param newFilename the new filename for the file
     * @return an observable with a single element, representing that the file has been renamed
     */
    Observable<Success> rename(ObjectId id, String newFilename);

    /**
     * Renames the stored file with the specified {@code id}.
     *
     * @param id          the id of the file in the files collection to rename
     * @param newFilename the new filename for the file
     * @return an observable with a single element, representing that the file has been renamed
     */
    Observable<Success> rename(BsonValue id, String newFilename);

    /**
     * Drops the data associated with this bucket from the database.
     *
     * @return an observable with a single element, representing that the collections have been dropped
     */
    Observable<Success> drop();

}
