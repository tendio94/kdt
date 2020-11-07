package com.tendio.kdt.configurator.model;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

//extending to gain functional interfaces default implementation from super class
public final class TestSuiteParameters implements Map<String, String> {
    private static Map<String, String> delegatee = Maps.newHashMap();

    public TestSuiteParameters(Map<String, String> map) {
        delegatee.putAll(map);
    }

    @Nullable
    public static String getParameter(@Nonnull String key) {
        return delegatee.get(key);
    }

    public static void addParameter(@Nonnull String key, @Nonnull String value) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "Parameter key can`t be null or empty!");
        delegatee.put(key, value);
    }

    @Override
    public int size() {
        return delegatee.size();
    }

    @Override
    public boolean isEmpty() {
        return delegatee.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegatee.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegatee.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return delegatee.get(key);
    }

    @Override
    public String put(String key, String value) {
        return delegatee.put(key, value);
    }

    @Override
    public String remove(Object key) {
        return delegatee.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        delegatee.putAll(m);
    }

    @Override
    public void clear() {
        delegatee.clear();
    }

    @Override
    public Set<String> keySet() {
        return delegatee.keySet();
    }

    @Override
    public Collection<String> values() {
        return delegatee.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return delegatee.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return delegatee.equals(o);
    }

    @Override
    public int hashCode() {
        return delegatee.hashCode();
    }

    @Override
    public String getOrDefault(Object key, String defaultValue) {
        return delegatee.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        delegatee.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        delegatee.replaceAll(function);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return delegatee.remove(key, value);
    }

    @Override
    public String replace(String key, String value) {
        return delegatee.replace(key, value);
    }

    @Override
    public String toString() {
        return delegatee.toString();
    }


}
