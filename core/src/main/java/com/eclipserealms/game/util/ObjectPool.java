package com.eclipserealms.game.util;

import java.util.ArrayDeque;

/**
 * Generic reusable-object pool. Spawning/despawning enemies, projectiles and
 * hit-particles with `new` every frame is one of the most common causes of
 * stutter on low-RAM devices (Java garbage collection pauses). This pool
 * avoids that by recycling instances instead of allocating and discarding
 * them.
 */
public class ObjectPool<T extends ObjectPool.Poolable> {

    public interface Poolable {
        void reset();
    }

    /** Own factory interface instead of the JDK's functional Supplier type,
     *  which is not available below Android API 24 (minSdk here is 21). */
    public interface Factory<T> {
        T create();
    }

    private final ArrayDeque<T> free = new ArrayDeque<>();
    private final Factory<T> factory;
    private final int maxSize;

    public ObjectPool(Factory<T> factory, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
    }

    public T obtain() {
        T item = free.poll();
        if (item == null) {
            item = factory.create();
        }
        return item;
    }

    public void free(T item) {
        if (item == null) return;
        item.reset();
        if (free.size() < maxSize) {
            free.add(item);
        }
    }

    public int freeCount() {
        return free.size();
    }
}
