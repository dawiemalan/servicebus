package io.dmcs.common.utils.expressions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class OptionalExt<T> {

    private final Optional<T> optional;

    public OptionalExt(T t) {
        this.optional = Optional.ofNullable(t);
    }

    public static <T> OptionalExt<T> of(Optional<T> t) {
        return ofNullable(t.orElse(null));
    }

    public static <T> OptionalExt<T> ofNullable(T t) {
        return new OptionalExt<>(t);
    }

    public ElseOptional ifPresent(Consumer<T> consumer) {
        optional.ifPresent(consumer);
        return this.new ElseOptional();
    }

    public class ElseOptional {

        private ElseOptional() {
        }

        public void orElse(Runnable r) {
            if (optional.isEmpty())
                r.run();
        }

        public <X extends Throwable> T orElseThrow(Supplier<? extends X> supplier) throws X {
            return optional.orElseThrow(supplier);
        }
    }
}
