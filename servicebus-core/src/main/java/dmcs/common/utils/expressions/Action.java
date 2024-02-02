package dmcs.common.utils.expressions;

public interface Action<T> {
    public T apply() throws Exception;
}
