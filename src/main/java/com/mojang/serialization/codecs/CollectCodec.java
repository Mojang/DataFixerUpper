package com.mojang.serialization.codecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * A codec for any types that have a {@link Collector}
 *
 * @see Collector
 * @param <T> the type of input elements to the reduction operation
 * @param <A> the mutable accumulation type of the reduction operation (often hidden as an implementation detail)
 * @param <R> the result type of the reduction operation
 */
public final class CollectCodec<T, A, R> implements Codec<R> {
	public static <T, A, R> Codec<R> of(Collector<T, A, R> collector, Function<R, Iterator<T>> iteratorFunction, Codec<T> element) {
		return new CollectCodec<>(collector, iteratorFunction, element);
	}
	
	public static <T, A, R extends Collection<T>> Codec<R> of(Collector<T, A, R> collector, Codec<T> element) {
		return new CollectCodec<>(collector, Collection::iterator, element);
	}
	
	/**
	 * @see Collectors#toCollection(Supplier)
	 */
	public static <C extends Collection<T>, T> Codec<C> collection(Supplier<C> supplier, Codec<T> elementCodec) {
		return of(Collectors.toCollection(supplier), elementCodec);
	}
	
	/**
	 * A codec for an immutable list
	 */
	public static <T> Codec<List<T>> list(Codec<T> element) {
		return of(Collectors.toUnmodifiableList(), element);
	}
	
	/**
	 * A codec for an ArrayList
	 */
	public static <T> Codec<List<T>> arrayList(Codec<T> element) {
		return collection(ArrayList::new, element);
	}
	
	/**
	 * A codec for an immutable set
	 */
	public static <T> Codec<Set<T>> set(Codec<T> element) {
		return of(Collectors.toUnmodifiableSet(), element);
	}
	
	public static <T> Codec<Set<T>> hashSet(Codec<T> element) {
		return collection(HashSet::new, element);
	}
	
	public static <T> Codec<Set<T>> concurrentSet(Codec<T> element) {
		return collection(() -> Collections.newSetFromMap(new ConcurrentHashMap<>()), element);
	}
	
	/**
	 * A codec for an immutable map
	 */
	public static <K, V> Codec<Map<K, V>> map(Codec<K> key, Codec<V> value) {
		return of(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue), m -> m.entrySet().iterator(), MapEntryCodec.of(key, value));
	}
	
	public static <K, V> Codec<Map<K, V>> map(Supplier<Map<K, V>> supplier, Codec<K> key, Codec<V> value) {
		return of(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
			throw new IllegalStateException("Key Conflict " + a + " & " + b);
		}, supplier), m -> m.entrySet().iterator(), MapEntryCodec.of(key, value));
	}
	
	public static <K, V> Codec<Map<K, V>> hashMap(Codec<K> key, Codec<V> value) {
		return map(HashMap::new, key, value);
	}
	
	public static <K, V> Codec<ConcurrentMap<K, V>> concurrentMap(Supplier<ConcurrentMap<K, V>> supplier, Codec<K> key, Codec<V> value) {
		return of(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
			throw new IllegalStateException("Key Conflict " + a + " & " + b);
		}, supplier), m -> m.entrySet().iterator(), MapEntryCodec.of(key, value));
	}
	
	public static <K, V> Codec<ConcurrentMap<K, V>> concurrentHashMap(Codec<K> key, Codec<V> value) {
		return concurrentMap(ConcurrentHashMap::new, key, value);
	}
	
	final Collector<T, A, R> collector;
	final Function<R, Iterator<T>> iterate;
	final Codec<T> elementCodec;
	
	public CollectCodec(Collector<T, A, R> collector, Function<R, Iterator<T>> iterate, Codec<T> codec) {
		this.collector = collector;
		this.iterate = iterate;
		this.elementCodec = codec;
	}
	
	@Override
	public <X> DataResult<Pair<R, X>> decode(DynamicOps<X> ops, X input) {
		return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
			BiConsumer<A, T> accumulator = this.collector.accumulator();
			A read = this.collector.supplier().get();
			
			final Stream.Builder<X> failed = Stream.builder();
			final MutableObject<DataResult<Unit>> result = new MutableObject<>(DataResult.success(Unit.INSTANCE, Lifecycle.stable()));
			
			stream.accept(t -> {
				final DataResult<Pair<T, X>> element = this.elementCodec.decode(ops, t);
				element.error().ifPresent(e -> failed.add(t));
				result.setValue(result.getValue().apply2stable((r, v) -> {
					accumulator.accept(read, v.getFirst());
					return r;
				}, element));
			});
			
			final R elements = this.collector.finisher().apply(read);
			final X errors = ops.createList(failed.build());
			
			final Pair<R, X> pair = Pair.of(elements, errors);
			return result.getValue().map(unit -> pair).setPartial(pair);
		});
	}
	
	@Override
	public <X> DataResult<X> encode(R input, DynamicOps<X> ops, X prefix) {
		final ListBuilder<X> builder = ops.listBuilder();
		
		Iterator<T> apply = this.iterate.apply(input);
		while(apply.hasNext()) {
			T next = apply.next();
			DataResult<X> result = this.elementCodec.encodeStart(ops, next);
			builder.add(result);
		}
		return builder.build(prefix);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(!(o instanceof CollectCodec)) {
			return false;
		}
		
		CollectCodec<?, ?, ?> codec = (CollectCodec<?, ?, ?>) o;
		
		if(!this.collector.equals(codec.collector)) {
			return false;
		}
		if(!this.iterate.equals(codec.iterate)) {
			return false;
		}
		return this.elementCodec.equals(codec.elementCodec);
	}
	
	@Override
	public int hashCode() {
		int result = this.collector.hashCode();
		result = 31 * result + this.iterate.hashCode();
		result = 31 * result + this.elementCodec.hashCode();
		return result;
	}
}
