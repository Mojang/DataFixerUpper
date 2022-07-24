// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT license.

package com.mojang.serialization.codecs;

import java.util.Map;
import java.util.Objects;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

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
		return this.first
				.decode(ops, input)
				.flatMap(p1 -> this.second.decode(ops, p1.getSecond()).map(p2 -> Pair.of(Map.entry(p1.getFirst(), p2.getFirst()), p2.getSecond())));
	}
	
	@Override
	public <T> DataResult<T> encode(final Map.Entry<K, V> value, final DynamicOps<T> ops, final T rest) {
		return this.second.encode(value.getValue(), ops, rest).flatMap(f -> this.first.encode(value.getKey(), ops, f));
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
