package net.minecraftforge.registries;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforbric.common.NextLog;

/**
 * DeferredRegister<T> — the central mod-facing API for registering objects.
 *
 * <p>Registration chain:
 * <ol>
 *   <li>Static init: register(name, supplier) stores entry in pending list</li>
 *   <li>Constructor: register(IEventBus) subscribes to Register event</li>
 *   <li>Event fires: iterate pending entries, call supplier, then call
 *       Registry.register() via reflection</li>
 * </ol>
 */
public final class DeferredRegister<T> {

    private final IRegistry<T> registry;
    private final String modid;
    private final List<PendingEntry<? extends T>> pending = new ArrayList<>();
    private int registeredCount;

    private DeferredRegister(IRegistry<T> registry, String modid) {
        this.registry = registry;
        this.modid = modid;
    }

    public static <T> DeferredRegister<T> create(IRegistry<T> registry, String modid) {
        return new DeferredRegister<>(registry, modid);
    }

    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Registry name cannot be empty");
        if (name.contains(":"))
            throw new IllegalArgumentException("Name should not contain ':': " + name);

        RegistryObject<I> ro = new RegistryObject<I>(modid + ":" + name, (IRegistry<I>) registry);
        pending.add(new PendingEntry<>(name, supplier, ro));
        return ro;
    }

    public void register(IEventBus bus) {
        NextLog.debug("DeferredRegister[{}#{}]: hooking to event bus, {} pending entries",
            modid, registry.key(), pending.size());
        bus.addListener((RegistryEvent.Register<?> event) -> handleRegisterEvent(event));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleRegisterEvent(RegistryEvent.Register<?> event) {
        if (pending.isEmpty()) return;

        String key = registry.key();
        Class<?> expectedType = switch (key) {
            case "minecraft:item" -> resolveClass("net.minecraft.world.item.Item");
            case "minecraft:block" -> resolveClass("net.minecraft.world.level.block.Block");
            default -> null;
        };

        if (expectedType == null) return;
        if (!expectedType.equals(event.getRegistryType())) return;

        Object mcReg = registry.mcRegistry();
        if (mcReg == null) {
            NextLog.warn("DeferredRegister[{}]: MC registry not available, skipping", modid);
            return;
        }

        NextLog.info("DeferredRegister[{}#{}]: registering {} entries", modid, key, pending.size());

        int count = 0;
        for (PendingEntry<? extends T> entry : pending) {
            try {
                T instance = entry.supplier().get();
                Object identifier = createIdentifier(modid, entry.name());
                if (identifier == null) continue;

                callRegistryRegister(mcReg, identifier, instance);

                RegistryObject<T> ro = (RegistryObject<T>) entry.ror();
                ro.resolve(instance);
                count++;
                registeredCount++;
                NextLog.debug("DeferredRegister: registered {}:{} -> {}",
                    modid, entry.name(), instance.getClass().getSimpleName());
            } catch (Throwable t) {
                NextLog.warn("DeferredRegister[{}]: failed to register '{}': {}",
                    modid, entry.name(), t.toString());
            }
        }

        NextLog.info("DeferredRegister[{}#{}]: successfully registered {} of {} entries",
            modid, key, count, pending.size());
    }

    private static Class<?> resolveClass(String name) {
        try { return Class.forName(name); }
        catch (ClassNotFoundException e) { return null; }
    }

    private static Object createIdentifier(String namespace, String path) {
        try {
            Class<?> idClass = Class.forName("net.minecraft.resources.Identifier");
            Method m = idClass.getMethod("fromNamespaceAndPath", String.class, String.class);
            return m.invoke(null, namespace, path);
        } catch (Throwable t) {
            NextLog.warn("DeferredRegister: createIdentifier failed: {}", t.toString());
            return null;
        }
    }

    private static void callRegistryRegister(Object mcRegistry, Object identifier, Object instance) {
        try {
            Class<?> registryClass = Class.forName("net.minecraft.core.Registry");
            Class<?> idClass = Class.forName("net.minecraft.resources.Identifier");
            Method m = registryClass.getMethod("register", registryClass, idClass, Object.class);
            m.invoke(null, mcRegistry, identifier, instance);
        } catch (Throwable t) {
            NextLog.warn("DeferredRegister: Registry.register call failed: {}", t.toString());
        }
    }

    public int getRegisteredCount() { return registeredCount; }
    public int getPendingCount() { return pending.size(); }

    private record PendingEntry<T>(
        String name,
        Supplier<? extends T> supplier,
        RegistryObject<T> ror
    ) {}
}
