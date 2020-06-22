package com.safframework.rxcache.memory;

import com.safframework.rxcache.domain.CacheStatistics;
import com.safframework.rxcache.domain.Record;

import java.util.Set;

/**
 * Created by tony on 2018/9/29.
 */
public interface Memory {

    <T> Record<T> getIfPresent(String key);

    <T> void put(String key, T value);

    <T> void put(String key, T value, long expireTime);

    Set<String> keySet();

    boolean containsKey(String key);

    void evict(String key);

    void evictAll();

    /**
     * 部分 Memory 的实现类还未实现这个方法，原先使用 default 方法，并默认为 null
     * 但是，android 需要26才支持Java 8的语法
     * 所以，先去掉 default，没有实现该方法的 Memory 的实现类，先返回 null
     * @return
     */
    CacheStatistics getCacheStatistics();
}
