package server.Model;

import java.util.function.Supplier;

public class LazyReference<T>{
    private T object;
    private Supplier<T> loader;

    public LazyReference(Supplier<T> loader) {
        this.loader = loader;
    }

    public LazyReference(T object) {
        this.object = object;
    }

    public T get() {
        if (object != null)
            return object;
        object = loader.get();
        return object;
    }
}
