package com.safframework.rxcache;

import com.safframework.bytekit.utils.Preconditions;
import com.safframework.rxcache.config.Constant;
import com.safframework.rxcache.domain.CacheStrategy;
import com.safframework.rxcache.domain.Record;
import com.safframework.rxcache.memory.Memory;
import com.safframework.rxcache.persistence.Persistence;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by tony on 2018/9/28.
 */
class CacheRepository {

    private Memory memory;
    private Persistence persistence;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    CacheRepository(Memory memory, Persistence persistence) {

        this.memory = memory;
        this.persistence = persistence;
    }

    <T> Record<T> get(String key, Type type, CacheStrategy cacheStrategy) {

        try {
            readLock.lock();

            Record<T> record = null;

            if (Preconditions.isNotBlanks(key, type)) {

                switch (cacheStrategy) {

                    case MEMORY: {

                        if (memory!=null) {

                            record = memory.getIfPresent(key);
                        }

                        break;
                    }

                    case PERSISTENCE: {

                        if (persistence!=null) {

                            record = persistence.retrieve(key, type);
                        }

                        break;
                    }

                    case ALL: {

                        if (memory != null) {

                            record = memory.getIfPresent(key);
                        }

                        if (record == null && persistence != null) {

                            record = persistence.retrieve(key, type);

                            if (memory!=null && record!=null && !record.isExpired()) { // 如果 memory 不为空，record 不为空，并且没有过期

                                readLock.unlock(); // 先释放读锁
                                writeLock.lock();  // 再获取写锁

                                try {

                                    if (record.isNeverExpire()) { // record永不过期的话，直接保存不需要计算ttl

                                        memory.put(record.getKey(),record.getData());
                                    } else {

                                        long ttl = record.getExpireTime()- (System.currentTimeMillis() - record.getCreateTime());
                                        memory.put(record.getKey(),record.getData(), ttl);
                                    }

                                    readLock.lock();    // 写锁在没有释放之前，获得读锁 (锁降级)
                                } finally {

                                    writeLock.unlock(); // 释放写锁
                                }
                            }
                        }
                        break;
                    }
                }
            }

            return record;
        } finally {

            readLock.unlock();
        }
    }

    <T> void save(String key, T value) {

        save(key,value, Constant.NEVER_EXPIRE);
    }

    <T> void save(String key, T value, long expireTime) {

        try {
            writeLock.lock();

            if (Preconditions.isNotBlanks(key, value)) {

                if (memory != null) {
                    memory.put(key, value, expireTime);
                }

                if (persistence != null) {
                    persistence.save(key, value, expireTime);
                }
            }

        } finally {

            writeLock.unlock();
        }
    }

    boolean containsKey(String key) {

        try {
            readLock.lock();

            if (Preconditions.isBlank(key)) return false;

            return (memory != null && memory.containsKey(key)) || (persistence != null && persistence.containsKey(key));

        } finally {

            readLock.unlock();
        }
    }

    Set<String> getAllKeys() {

        try {
            readLock.lock();

            Set<String> result = new HashSet<>();

            if (memory != null) {

                result.addAll(memory.keySet());
            }

            if (persistence != null) {

                result.addAll(persistence.allKeys());
            }

            return result;

        } finally {

            readLock.unlock();
        }
    }

    void remove(String key) {

        try {
            writeLock.lock();

            if (Preconditions.isNotBlank(key)) {

                if (memory != null) {
                    memory.evict(key);
                }

                if (persistence != null) {
                    persistence.evict(key);
                }
            }

        } finally {

            writeLock.unlock();
        }
    }

    long ttl(String key, Type type) {

        try {
            readLock.lock();

            Record record = null;

            if (Preconditions.isNotBlanks(key, type)) {

                if (memory != null) {

                    record =  memory.getIfPresent(key);
                }

                if (persistence != null) {

                    record = persistence.retrieve(key, type);
                }
            }

            if (record == null) {

                return Constant.NO_RECORD;
            }

            return  record.ttl();

        } finally {

            readLock.unlock();
        }
    }

    void clear() {

        try {
            writeLock.lock();

            if (memory != null) {
                memory.evictAll();
            }

            if (persistence != null) {
                persistence.evictAll();
            }

        } finally {

            writeLock.unlock();
        }
    }
}
