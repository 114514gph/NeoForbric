package net.minecraftforge.registries;

import java.util.function.Supplier;

/**
 * RegistryObject<T> — a reference to a registered object.
 */
public final class RegistryObject<T> implements Supplier<T> {

    private final String name;
    private final IRegistry<T> registry;
    private T value;
    private boolean resolved;

    RegistryObject(String name, IRegistry<T> registry) {
        this.name = name;
        this.registry = registry;
    }

    @Override
    public T get() {
        if (!resolved) throw new IllegalStateException(
            "RegistryObject " + name + " not yet resolved");
        return value;
    }

    public boolean isPresent() { return resolved; }

    void resolve(T val) {
        this.value = val;
        this.resolved = true;
    }

    public String getId() { return name; }
    public IRegistry<T> getRegistry() { return registry; }

    @Override
    public String toString() {
        return "RegistryObject[" + name + (resolved ? "=" + value : " (unresolved)") + "]";
    }
}
