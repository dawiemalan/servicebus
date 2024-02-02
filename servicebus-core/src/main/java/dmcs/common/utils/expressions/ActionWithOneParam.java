package dmcs.common.utils.expressions;

public interface ActionWithOneParam<T, U> {
    T apply(U param) throws Exception;
}
