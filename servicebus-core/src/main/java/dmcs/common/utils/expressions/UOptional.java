package dmcs.common.utils.expressions;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class UOptional<T> {

    private final Optional<T> optional;

    public UOptional(T t) {
        this.optional = Optional.ofNullable(t);
    }

    public static <T> UOptional<T> of(Optional<T> t) {
        return ofNullable(t.orElse(null));
    }

    public static <T> UOptional<T> ofNullable(T t) {
        return new UOptional<>(t);
    }

    public UElseOptional ifPresent(Consumer<T> consumer) {
        optional.ifPresent(consumer);
        return this.new UElseOptional();
    }

    public class UElseOptional {

        private UElseOptional() {
        }

        public void orElse(Runnable r) {
            if (!optional.isPresent())
                r.run();
        }

        public <X extends Throwable> T orElseThrow(Supplier<? extends X> supplier) throws X {
            return optional.orElseThrow(supplier);
        }
    }
}
