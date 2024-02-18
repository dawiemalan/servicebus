package io.dmcs.common.utils.expressions;

@FunctionalInterface
public interface ActionWithOneParam<T, U> {
    T apply(U param) throws Exception;
}
