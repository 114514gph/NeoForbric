package net.neoforged.neoforge.registries;

import java.util.function.Supplier;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IRegistry;
import net.minecraftforge.registries.RegistryObject;

/**
 * NeoForge version of DeferredRegister — same API, different package.
 * Delegates to the shared Forge implementation.
 */
public final class DeferredRegister<T> {

    private final net.minecraftforge.registries.DeferredRegister<T> delegate;

    private DeferredRegister(IRegistry<T> registry, String modid) {
        this.delegate = net.minecraftforge.registries.DeferredRegister.create(registry, modid);
    }

    public static <T> DeferredRegister<T> create(IRegistry<T> registry, String modid) {
        return new DeferredRegister<>(registry, modid);
    }

    public <I extends T> RegistryObject<I> register(
            String name, Supplier<? extends I> supplier) {
        return delegate.register(name, supplier);
    }

    public void register(IEventBus bus) {
        delegate.register(bus);
    }
}
