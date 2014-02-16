package com.cep.messaging.util.collection;

import java.util.*;

import com.cep.messaging.util.Pair;
import com.google.common.base.Function;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

public class ExpiringMap<K, V> {
	private final Function<Pair<K, V>, ?> postExpireHook;

	private static class CacheableObject<T> {
		private final T value;
		private final long age;
		private final long expiration;

		CacheableObject(T o, long e) {
			assert o != null;
			value = o;
			expiration = e;
			age = System.currentTimeMillis();
		}

		T getValue() {
			return value;
		}

		boolean isReadyToDie(long start) {
			return ((start - age) > expiration);
		}
	}

	private class CacheMonitor extends TimerTask {

		public void run() {
			long start = System.currentTimeMillis();
			for (Map.Entry<K, CacheableObject<V>> entry : cache.entrySet()) {
				if (entry.getValue().isReadyToDie(start)) {
					cache.remove(entry.getKey());
					if (postExpireHook != null) {
						postExpireHook.apply(new Pair<K, V>(entry.getKey(), entry.getValue().getValue()));
					}
				}
			}
		}
	}

	private final NonBlockingHashMap<K, CacheableObject<V>> cache = new NonBlockingHashMap<K, CacheableObject<V>>();
	private final Timer timer;
	private static int counter = 0;
	private final long expiration;

	public ExpiringMap(long expiration) {
		this(expiration, null);
	}

	/**
	 * 
	 * @param expiration
	 *            the TTL for objects in the cache in milliseconds
	 */
	public ExpiringMap(long expiration, Function<Pair<K, V>, ?> postExpireHook) {
		this.postExpireHook = postExpireHook;
		this.expiration = expiration;

		if (expiration <= 0) {
			throw new IllegalArgumentException("Argument specified must be a positive number");
		}

		timer = new Timer("EXPIRING-MAP-TIMER-" + (++counter), true);
		timer.schedule(new CacheMonitor(), expiration / 2, expiration / 2);
	}

	public void shutdown() {
		timer.cancel();
	}

	public V put(K key, V value) {
		return put(key, value, this.expiration);
	}

	public V put(K key, V value, long timeout) {
		CacheableObject<V> previous = cache.put(key, new CacheableObject<V>(value, timeout));
		return (previous == null) ? null : previous.getValue();
	}

	public V get(K key) {
		CacheableObject<V> co = cache.get(key);
		return co == null ? null : co.getValue();
	}

	public V remove(K key) {
		CacheableObject<V> co = cache.remove(key);
		return co == null ? null : co.getValue();
	}

	public long getAge(K key) {
		CacheableObject<V> co = cache.get(key);
		return co == null ? 0 : co.age;
	}

	public int size() {
		return cache.size();
	}

	public boolean containsKey(K key) {
		return cache.containsKey(key);
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public Set<K> keySet() {
		return cache.keySet();
	}
}
