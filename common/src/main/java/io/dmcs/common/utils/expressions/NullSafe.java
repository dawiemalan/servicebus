package io.dmcs.common.utils.expressions;

import java.util.Optional;
import java.util.function.Supplier;

public class NullSafe {

    public static <T> Optional<T> of(Supplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }
}
