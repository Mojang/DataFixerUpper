// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.serialization;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    public Type<?> getType(final JsonElement input) {
        if (input.isJsonObject()) {
            return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
        }
        if (input.isJsonArray()) {
            return DSL.list(DSL.remainderType());
        }
        if (input.isJsonNull()) {
            return DSL.emptyPartType();
        }
        final JsonPrimitive primitive = input.getAsJsonPrimitive();
        if (primitive.isString()) {
            return DSL.string();
        }
        if (primitive.isBoolean()) {
            return DSL.bool();
        }
        final BigDecimal value = primitive.getAsBigDecimal();
        try {
            final long l = value.longValueExact();
            if ((byte) l == l) {
                return DSL.byteType();
            }
            if ((short) l == l) {
                return DSL.shortType();
            }
            if ((int) l == l) {
                return DSL.intType();
            }
            return DSL.longType();
        } catch (final ArithmeticException e) {
            final double d = value.doubleValue();
            if ((float) d == d) {
                return DSL.floatType();
            }
            return DSL.doubleType();
        }
    }

    @Override
    public Optional<Number> getNumberValue(final JsonElement input) {
        if (input.isJsonPrimitive()) {
            if (input.getAsJsonPrimitive().isNumber()) {
                return Optional.of(input.getAsNumber());
            } else if (input.getAsJsonPrimitive().isBoolean()) {
                return Optional.of(input.getAsBoolean() ? 1 : 0);
            }
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createNumeric(final Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public Optional<Boolean> getBooleanValue(final JsonElement input) {
        if (input.isJsonPrimitive()) {
            if (input.getAsJsonPrimitive().isBoolean()) {
                return Optional.of(input.getAsBoolean());
            } else if (input.getAsJsonPrimitive().isNumber()) {
                return Optional.of(input.getAsNumber().byteValue() != 0);
            }
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createBoolean(final boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public Optional<String> getStringValue(final JsonElement input) {
        if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isString()) {
            return Optional.of(input.getAsString());
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createString(final String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<JsonElement> mergeInto(final JsonElement list, final JsonElement value) {
       if (!list.isJsonArray() && list != empty()) {
            return DataResult.error("mergeInto called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
       if (list != empty()) {
           result.addAll(list.getAsJsonArray());
       }
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeInto(final JsonElement list, final List<JsonElement> values) {
        if (!list.isJsonArray() && list != empty()) {
            return DataResult.error("mergeInto called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
        if (list != empty()) {
            result.addAll(list.getAsJsonArray());
        }
        values.forEach(result::add);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeInto(final JsonElement map, final JsonElement key, final JsonElement value) {
        if (!map.isJsonObject() && map != empty()) {
            return DataResult.error("mergeInto called with not a map: " + map, map);
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
    public DataResult<JsonElement> mergeInto(final JsonElement map, final Map<JsonElement, JsonElement> values) {
        if (!map.isJsonObject() && map != empty()) {
            return DataResult.error("mergeInto called with not a map: " + map, map);
        }

        final JsonObject output = new JsonObject();
        if (map != empty()) {
            map.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        }

        final List<JsonElement> missed = Lists.newArrayList();

        for (final Map.Entry<JsonElement, JsonElement> entry : values.entrySet()) {
            final JsonElement key = entry.getKey();
            if (!key.isJsonPrimitive() || !key.getAsJsonPrimitive().isString()) {
                missed.add(key);
                continue;
            }

            output.add(key.getAsString(), entry.getValue());
        }

        if (!missed.isEmpty()) {
            return DataResult.error("some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }

    @Override
    public Optional<Stream<Pair<JsonElement, JsonElement>>> getMapValues(final JsonElement input) {
        if (input.isJsonObject()) {
            return Optional.of(input.getAsJsonObject().entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue())));
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createMap(final Map<JsonElement, JsonElement> map) {
        final JsonObject result = new JsonObject();
        for (final Map.Entry<JsonElement, JsonElement> entry : map.entrySet()) {
            result.add(entry.getKey().getAsString(), entry.getValue());
        }
        return result;
    }

    @Override
    public Optional<Stream<JsonElement>> getStream(final JsonElement input) {
        if (input.isJsonArray()) {
            return Optional.of(StreamSupport.stream(input.getAsJsonArray().spliterator(), false));
        }
        return Optional.empty();
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
            builder = builder.flatMap(b -> value.map(element -> {
                b.add(element);
                return b;
            }));
            return this;
        }

        @Override
        public DataResult<JsonElement> build(final JsonElement prefix) {
            final DataResult<JsonElement> result = builder.flatMap(b -> INSTANCE.mergeInto(prefix, b));
            builder = DataResult.success(new JsonArray());
            return result;
        }
    }
}
