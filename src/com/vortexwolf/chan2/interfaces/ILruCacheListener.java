package com.vortexwolf.chan2.interfaces;

public interface ILruCacheListener<K, V> {
    void onEntryRemoved(K key, V value);
}
