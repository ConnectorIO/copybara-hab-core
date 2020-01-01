/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.storage.mapdb.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mapdb.DB;
import org.openhab.core.items.ManagedItemProvider.PersistedItem;
import org.openhab.core.items.ManagedItemProvider.PersistedItemInstanceCreator;
import org.openhab.core.storage.DeletableStorage;
import org.openhab.core.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The MapDbStorage is concrete implementation of the {@link Storage} interface.
 * It stores the key-value pairs in files. This Storage serializes and deserializes
 * the given values using their JSON representation (generated by {@code Gson}.
 * This transformation should help maintaining version compatibility of the stored
 * data.
 *
 * @author Thomas Eichstaedt-Engelen - Initial contribution
 * @author Alex Tugarev - Loading with Class.forName() if classLoader is null
 * @author Markus Rathgeb - Made the MapDB storage a disposable one
 */
@NonNullByDefault
public class MapDbStorage<T> implements DeletableStorage<T> {

    static final String TYPE_SEPARATOR = "@@@";

    private final Logger logger = LoggerFactory.getLogger(MapDbStorage.class);

    private final String name;
    private final DB db;
    private final @Nullable ClassLoader classLoader;
    private Map<String, String> map;

    private transient Gson mapper;

    /**
     * Constructor.
     *
     * @param db the database
     * @param name the name
     * @param classLoader the classloader used for deserialization
     */
    public MapDbStorage(final DB db, final String name, final @Nullable ClassLoader classLoader) {
        this.name = name;
        this.db = db;
        this.classLoader = classLoader;
        this.map = db.createTreeMap(name).makeOrGet();
        this.mapper = new GsonBuilder().registerTypeAdapterFactory(new PropertiesTypeAdapterFactory())
                .registerTypeAdapter(PersistedItem.class, new PersistedItemInstanceCreator()).create();
    }

    @Override
    public void delete() {
        // Use an unmodifiable map. After deletion no operation / modification should be called anymore.
        map = Collections.emptyMap();
        db.delete(name);
    }

    @Override
    public @Nullable T put(String key, @Nullable T value) {
        if (value == null) {
            return remove(key);
        }
        String previousValue = map.put(key, serialize(value));
        db.commit();
        return deserialize(previousValue);
    }

    @Override
    public @Nullable T remove(String key) {
        String removedElement = map.remove(key);
        db.commit();
        return deserialize(removedElement);
    }

    @Override
    public boolean containsKey(final String key) {
        return map.containsKey(key);
    }

    @Override
    public @Nullable T get(String key) {
        return deserialize(map.get(key));
    }

    @Override
    public Collection<String> getKeys() {
        return new HashSet<>(map.keySet());
    }

    @Override
    public Collection<@Nullable T> getValues() {
        Collection<@Nullable T> values = new ArrayList<>();
        for (String key : getKeys()) {
            values.add(get(key));
        }
        return values;
    }

    /**
     * Transforms the given {@code value} into its JSON representation using {@code Gson}.
     *
     * <p>
     * Since we do not know the type of {@code value} while deserializing it afterwards we prepend its qualified type
     * name to the JSON String.
     *
     * @param value the {@code value} to store
     * @return the JSON document prepended with the qualified type name of {@code value}
     */
    private String serialize(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot serialize NULL");
        }

        String valueTypeName = value.getClass().getName();
        String valueAsString = mapper.toJson(value);
        String concatValue = valueTypeName + TYPE_SEPARATOR + valueAsString;

        logger.trace("serialized value '{}' to MapDB", concatValue);
        return concatValue;
    }

    /**
     * Deserializes and instantiates an object of type {@code T} out of the given JSON String.
     *
     * <p>
     * A special classloader (other than the one of the MapDB bundle) is used in order to load the classes in the
     * context of the calling bundle.
     *
     * @param json the JSON String
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    public @Nullable T deserialize(@Nullable String json) {
        if (json == null) {
            // nothing to deserialize
            return null;
        }

        String[] concatValue = json.split(TYPE_SEPARATOR, 2);
        String valueTypeName = concatValue[0];
        String valueAsString = concatValue[1];

        @Nullable
        T value = null;
        try {
            final ClassLoader classLoader = this.classLoader;

            // load required class within the given bundle context
            final Class<T> loadedValueType;
            if (classLoader == null) {
                loadedValueType = (Class<T>) Class.forName(valueTypeName);
            } else {
                loadedValueType = (Class<T>) classLoader.loadClass(valueTypeName);
            }

            value = mapper.fromJson(valueAsString, loadedValueType);
            logger.trace("deserialized value '{}' from MapDB", value);
        } catch (final JsonSyntaxException | ClassNotFoundException ex) {
            logger.warn("Couldn't deserialize value '{}'. Root cause is: {}", json, ex.getMessage());
        }

        return value;
    }

}
