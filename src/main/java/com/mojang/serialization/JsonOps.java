// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonOps implements DynamicOps<JsonElement> {
    public static final JsonOps INSTANCE = new JsonOps();

    protected JsonOps() {
    }

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public Codec<?> getType(final JsonElement input) {
        if (input.isJsonObject()) {
            return Codec.compoundList(Codec.SAVING, Codec.SAVING);
        }
        if (input.isJsonArray()) {
            return Codec.list(Codec.SAVING);
        }
        if (input.isJsonNull()) {
            return Codec.EMPTY;
        }
        final JsonPrimitive primitive = input.getAsJsonPrimitive();
        if (primitive.isString()) {
            return Codec.STRING;
        }
        if (primitive.isBoolean()) {
            return Codec.BOOL;
        }
        final BigDecimal value = primitive.getAsBigDecimal();
        try {
            final long l = value.longValueExact();
            if ((byte) l == l) {
                return Codec.BYTE;
            }
            if ((short) l == l) {
                return Codec.SHORT;
            }
            if ((int) l == l) {
                return Codec.INT;
            }
            return Codec.LONG;
        } catch (final ArithmeticException e) {
            final double d = value.doubleValue();
            if ((float) d == d) {
                return Codec.FLOAT;
            }
            return Codec.DOUBLE;
        }
    }

    @Override
    public DataResult<Number> getNumberValue(final JsonElement input) {
        if (input.isJsonPrimitive()) {
            if (input.getAsJsonPrimitive().isNumber()) {
                return DataResult.success(input.getAsNumber());
            } else if (input.getAsJsonPrimitive().isBoolean()) {
                return DataResult.success(input.getAsBoolean() ? 1 : 0);
            }
        }
        if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isBoolean()) {
            return DataResult.success(input.getAsJsonPrimitive().getAsBoolean() ? 1 : 0);
        }
        return DataResult.error("Not a number: " + input);
    }

    @Override
    public JsonElement createNumeric(final Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final JsonElement input) {
        if (input.isJsonPrimitive()) {
            if (input.getAsJsonPrimitive().isBoolean()) {
                return DataResult.success(input.getAsBoolean());
            } else if (input.getAsJsonPrimitive().isNumber()) {
                return DataResult.success(input.getAsNumber().byteValue() != 0);
            }
        }
        return DataResult.error("Not a boolean: " + input);
    }

    @Override
    public JsonElement createBoolean(final boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<String> getStringValue(final JsonElement input) {
        if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isString()) {
            return DataResult.success(input.getAsString());
        }
        return DataResult.error("Not a string: " + input);
    }

    @Override
    public JsonElement createString(final String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<JsonElement> mergeToList(final JsonElement list, final JsonElement value) {
       if (!list.isJsonArray() && list != empty()) {
            return DataResult.error("mergeToList called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
       if (list != empty()) {
           result.addAll(list.getAsJsonArray());
       }
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeToList(final JsonElement list, final List<JsonElement> values) {
        if (!list.isJsonArray() && list != empty()) {
            return DataResult.error("mergeToList called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
        if (list != empty()) {
            result.addAll(list.getAsJsonArray());
        }
        values.forEach(result::add);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(final JsonElement map, final JsonElement key, final JsonElement value) {
        if (!map.isJsonObject() && map != empty()) {
            return DataResult.error("mergeToMap called with not a map: " + map, map);
        }
        if (!key.isJsonPrimitive() || !key.getAsJsonPrimitive().isString()) {
            return DataResult.error("key is not a string: " + key, map);
        }

        final JsonObject output = new JsonObject();
        if (map != empty()) {
            map.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        }
        output.add(key.getAsString(), value);

        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(final JsonElement map, final MapLike<JsonElement> values) {
        if (!map.isJsonObject() && map != empty()) {
            return DataResult.error("mergeToMap called with not a map: " + map, map);
        }

        final JsonObject output = new JsonObject();
        if (map != empty()) {
            map.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        }

        final List<JsonElement> missed = Lists.newArrayList();

        values.entries().forEach(entry -> {
            final JsonElement key = entry.getFirst();
            if (!key.isJsonPrimitive() || !key.getAsJsonPrimitive().isString()) {
                missed.add(key);
                return;
            }
            output.add(key.getAsString(), entry.getSecond());
        });

        if (!missed.isEmpty()) {
            return DataResult.error("some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(final JsonElement input) {
        if (!input.isJsonObject()) {
            return DataResult.error("Not a JSON object: " + input);
        }
        return DataResult.success(input.getAsJsonObject().entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue())));
    }

    @Override
    public DataResult<MapLike<JsonElement>> getMap(final JsonElement input) {
        if (!input.isJsonObject()) {
            return DataResult.error("Not a JSON object: " + input);
        }
        final JsonObject object = input.getAsJsonObject();
        return DataResult.success(new MapLike<JsonElement>() {
            @Nullable
            @Override
            public JsonElement get(final JsonElement key) {
                return object.get(key.getAsString());
            }

            @Override
            public Stream<Pair<JsonElement, JsonElement>> entries() {
                return object.entrySet().stream().map(e -> Pair.of(new JsonPrimitive(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public JsonElement createMap(final Stream<Pair<JsonElement, JsonElement>> map) {
        final JsonObject result = new JsonObject();
        map.forEach(p -> result.add(p.getFirst().getAsString(), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(final JsonElement input) {
        if (input.isJsonArray()) {
            return DataResult.success(StreamSupport.stream(input.getAsJsonArray().spliterator(), false));
        }
        return DataResult.error("Not a json array: " + input);
    }

    @Override
    public JsonElement createList(final Stream<JsonElement> input) {
        final JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
    }

    @Override
    public JsonElement remove(final JsonElement input, final String key) {
        if (input.isJsonObject()) {
            final JsonObject result = new JsonObject();
            input.getAsJsonObject().entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return result;
        }
        return input;
    }

    @Override
    public String toString() {
        return "JSON";
    }

    @Override
    public ListBuilder<JsonElement> listBuilder() {
        return new ArrayBuilder();
    }

    private static final class ArrayBuilder implements ListBuilder<JsonElement> {
        private DataResult<JsonArray> builder = DataResult.success(new JsonArray());

        @Override
        public DynamicOps<JsonElement> ops() {
            return INSTANCE;
        }

        @Override
        public ListBuilder<JsonElement> add(final JsonElement value) {
            builder = builder.map(b -> {
                b.add(value);
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<JsonElement> add(final DataResult<JsonElement> value) {
            builder = builder.ap2(value, (b, element) -> {
                b.add(element);
                return b;
            });
            return this;
        }

        @Override
        public DataResult<JsonElement> build(final JsonElement prefix) {
            final DataResult<JsonElement> result = builder.flatMap(b -> {
                if (!prefix.isJsonArray() && prefix != ops().empty()) {
                    return DataResult.error("Cannot append a list to not a list: " + prefix, prefix);
                }

                final JsonArray array = new JsonArray();
                if (prefix != ops().empty()) {
                    array.addAll(prefix.getAsJsonArray());
                }
                array.addAll(b);
                return DataResult.success(array);
            });

            builder = DataResult.success(new JsonArray());
            return result;
        }
    }
}
