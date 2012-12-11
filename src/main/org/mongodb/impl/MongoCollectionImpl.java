/**
 * Copyright (c) 2008 - 2012 10gen, Inc. <http://10gen.com>
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
 *
 */

package org.mongodb.impl;

import org.mongodb.MongoClient;
import org.mongodb.MongoCollection;
import org.mongodb.MongoCursor;
import org.mongodb.MongoNamespace;
import org.mongodb.ReadPreference;
import org.mongodb.WriteConcern;
import org.mongodb.command.CountCommand;
import org.mongodb.command.FindAndRemoveCommand;
import org.mongodb.command.FindAndReplaceCommand;
import org.mongodb.command.FindAndUpdateCommand;
import org.mongodb.operation.MongoFind;
import org.mongodb.operation.MongoFindAndRemove;
import org.mongodb.operation.MongoFindAndReplace;
import org.mongodb.operation.MongoFindAndUpdate;
import org.mongodb.operation.MongoInsert;
import org.mongodb.operation.MongoRemove;
import org.mongodb.result.InsertResult;
import org.mongodb.result.RemoveResult;
import org.mongodb.serialization.Serializers;

class MongoCollectionImpl<T> implements MongoCollection<T> {
    private final String name;
    private final MongoDatabaseImpl database;
    private final Class<T> clazz;
    private WriteConcern writeConcern;
    private ReadPreference readPreference;
    private final Serializers serializers;

    public MongoCollectionImpl(final String name, MongoDatabaseImpl database, Class<T> clazz) {
        this(name, database, clazz, null, null, null);
    }

    public MongoCollectionImpl(final String name, final MongoDatabaseImpl database, final Class<T> clazz,
                               final WriteConcern writeConcern, ReadPreference readPreference, Serializers serializers) {
        this.name = name;
        this.database = database;
        this.clazz = clazz;
        this.writeConcern = writeConcern;
        this.readPreference = readPreference;
        this.serializers = serializers;
    }

    public MongoCollectionImpl(final String name, final MongoDatabaseImpl database, final Class<T> clazz,
                               final Serializers serializers) {
        this(name, database, clazz, null, null, serializers);
    }

    @Override
    public MongoDatabaseImpl getDatabase() {
        return database;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MongoCursor<T> find(MongoFind find) {
        return new MongoCursor<T>(this, find.readPreferenceIfAbsent(readPreference), clazz);
    }

    @Override
    public T findOne(final MongoFind find) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        return new CountCommand(getClient(), getNamespace()).execute().getCount();
    }

    @Override
    public long count(final MongoFind find) {
        return new CountCommand(getClient(), getNamespace(), find).execute().getCount();
    }

    @Override
    public T findAndUpdate(final MongoFindAndUpdate findAndUpdate) {
        return new FindAndUpdateCommand<T>(getClient(), getNamespace(), findAndUpdate, getSerializers(), clazz).execute().getValue();
    }

    @Override
    public T findAndReplace(final MongoFindAndReplace<T> findAndReplace) {
        return new FindAndReplaceCommand<T>(getClient(), getNamespace(), findAndReplace, getSerializers(), clazz).execute().getValue();
    }

    @Override
    public T findAndRemove(final MongoFindAndRemove findAndRemove) {
        return new FindAndRemoveCommand<T>(getClient(), getNamespace(), findAndRemove, getSerializers(), clazz).execute().getValue();
    }

    @Override
    public InsertResult insert(final MongoInsert<T> insert) {
        return getClient().getOperations().insert(getNamespace(), insert.writeConcernIfAbsent(getWriteConcern()), clazz, getSerializers());
    }

    @Override
    public RemoveResult remove(final MongoRemove remove) {
        return getClient().getOperations().delete(getNamespace(), remove.writeConcernIfAbsent(getWriteConcern()), getSerializers());
    }

    @Override
    public MongoCollection<T> withWriteConcern(final WriteConcern writeConcern) {
        return new MongoCollectionImpl<T>(name, database, clazz, writeConcern, readPreference, getSerializers());
    }

    @Override
    public Serializers getSerializers() {
        if (serializers != null) {
            return serializers;
        }
        return getDatabase().getSerializers();
    }

    @Override
    public MongoClient getClient() {
        return getDatabase().getClient();
    }

    @Override
    public WriteConcern getWriteConcern() {
        if (writeConcern != null) {
            return writeConcern;
        }
        return getDatabase().getWriteConcern();
    }

    @Override
    public ReadPreference getReadPreference() {
        if (readPreference != null) {
            return readPreference;
        }
        return getDatabase().getReadPreference();
    }

    @Override
    public MongoNamespace getNamespace() {
        return new MongoNamespace(getDatabase().getName(), getName());
    }

}