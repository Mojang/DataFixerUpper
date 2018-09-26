// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.
package com.mojang.datafixers.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.util.Pair;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
            return DSL.nilType();
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
        if (input.isJsonPrimitive() && input.getAsJsonPrimitive().isNumber()) {
            return Optional.of(input.getAsNumber());
        }
        return Optional.empty();
    }

    @Override
    public JsonElement createNumeric(final Number i) {
        return new JsonPrimitive(i);
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
    public JsonElement mergeInto(final JsonElement input, final JsonElement value) {
        final JsonArray result;
        if (value.isJsonNull()) {
            return input;
        }
        if (input.isJsonObject()) {
            if (value.isJsonObject()) {
                final JsonObject resultObject = new JsonObject();
                final JsonObject first = input.getAsJsonObject();
                for (final Map.Entry<String, JsonElement> entry : first.entrySet()) {
                    resultObject.add(entry.getKey(), entry.getValue());
                }
                final JsonObject second = value.getAsJsonObject();
                for (final Map.Entry<String, JsonElement> entry : second.entrySet()) {
                    resultObject.add(entry.getKey(), entry.getValue());
                }
                return resultObject;
            }
            return input;
        } else if (input.isJsonNull()) {
            throw new IllegalArgumentException("mergeInto called with null input.");
        } else if (input.isJsonArray()) {
            result = new JsonArray();
            StreamSupport.stream(input.getAsJsonArray().spliterator(), false).forEach(result::add);
        } else {
            return input;
        }
        result.add(value);
        return result;
    }

    @Override
    public JsonElement mergeInto(final JsonElement input, final JsonElement key, final JsonElement value) {
        final JsonObject output;
        if (input.isJsonNull()) {
            output = new JsonObject();
        } else if (input.isJsonObject()) {
            output = new JsonObject();
            input.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        } else {
            return input;
        }
        output.add(key.getAsString(), value);
        return output;
    }

    @Override
    public JsonElement merge(final JsonElement first, final JsonElement second) {
        if (first.isJsonNull()) {
            return second;
        }
        if (second.isJsonNull()) {
            return first;
        }
        if (first.isJsonObject() && second.isJsonObject()) {
            JsonObject result = new JsonObject();
            first.getAsJsonObject().entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            second.getAsJsonObject().entrySet().forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return result;
        }
        if (first.isJsonArray() && second.isJsonArray()) {
            JsonArray result = new JsonArray();
            first.getAsJsonArray().forEach(result::add);
            second.getAsJsonArray().forEach(result::add);
            return result;
        }
        throw new IllegalArgumentException("Could not merge " + first + " and " + second);
    }

    @Override
    public Optional<Map<JsonElement, JsonElement>> getMapValues(final JsonElement input) {
        if (input.isJsonObject()) {
            return Optional.of(input.getAsJsonObject().entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
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
}
