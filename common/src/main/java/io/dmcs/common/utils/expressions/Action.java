package io.dmcs.common.utils.expressions;

@FunctionalInterface
public interface Action<T> {
    T apply() throws Exception;
}
