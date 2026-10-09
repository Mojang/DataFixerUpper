// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.serialization.codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonArray;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import org.apache.commons.lang3.mutable.MutableInt;

public final class MapEntryCodec<K, V> implements Codec<Map.Entry<K, V>> {
	private final Codec<K> first;
	private final Codec<V> second;
	
	public static <K, V> Codec<Map.Entry<K, V>> of(final Codec<K> first, final Codec<V> second) {
		return new MapEntryCodec<>(first, second);
	}
	
	public MapEntryCodec(final Codec<K> first, final Codec<V> second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	public <T> DataResult<Pair<Map.Entry<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
		return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(consumer -> {
			List<T> inputs = new ArrayList<>(3);
			consumer.accept(inputs::add);
			if(inputs.size() == 2) {
				inputs.add(ops.empty());
			} else if(inputs.size() < 2) {
				return DataResult.error("Expected atleast 2 elements for map entry, found " + inputs.size());
			}
			return this.first
					.decode(ops, inputs.get(0))
					.flatMap(p -> this.second.decode(ops, inputs.get(1)).map(p2 -> Pair.of(Map.entry(p.getFirst(), p2.getFirst()), inputs.get(2))));
		});
	}
	
	@Override
	public <T> DataResult<T> encode(final Map.Entry<K, V> value, final DynamicOps<T> ops, final T rest) {
		ListBuilder<T> builder = ops.listBuilder();
		builder.add(this.first.encodeStart(ops, value.getKey()));
		builder.add(this.second.encodeStart(ops, value.getValue()));
		return builder.build(rest);
	}
	
	@Override
	public boolean equals(final Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || this.getClass() != o.getClass()) {
			return false;
		}
		final MapEntryCodec<?, ?> pairCodec = (MapEntryCodec<?, ?>) o;
		return Objects.equals(this.first, pairCodec.first) && Objects.equals(this.second, pairCodec.second);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.first, this.second);
	}
	
	@Override
	public String toString() {
		return "Map.EntryCodec[" + this.first + ", " + this.second + ']';
	}
}
