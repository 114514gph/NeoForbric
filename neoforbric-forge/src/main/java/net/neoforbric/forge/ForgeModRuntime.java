package net.neoforbric.forge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import net.neoforbric.api.ModCandidate;
import net.neoforbric.common.NextLog;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge mod runtime: @Mod class loading + lifecycle scheduling + event bus + registry.
 *
 * <p>Fires the Forge lifecycle in correct order:
 * <ol>
 *   <li>Load/instantiate @Mod classes (their constructors call
 *       DeferredRegister.register(eventBus))</li>
 *   <li>Fire RegistryEvent.NewRegistry</li>
 *   <li>Fire RegistryEvent.Register&lt;Item&gt; — DeferredRegister creates items
 *       and calls BuiltInRegistries.ITEM.register() via reflection</li>
 *   <li>Fire RegistryEvent.Register&lt;Block&gt;</li>
 *   <li>Fire FMLCommonSetupEvent</li>
 *   <li>Fire FMLClientSetupEvent</li>
 * </ol>
 */
public class ForgeModRuntime {

    private final List<Object> modInstances = new ArrayList<>();
    private final List<URLClassLoader> classLoaders = new ArrayList<>();
    private final List<ModCandidate> candidates = new ArrayList<>();
    private int registeredItemCount = 0;

    /** The shared mod event bus that DeferredRegisters hook into. */
    private IEventBus modBus() {
        return FMLJavaModLoadingContext.sharedModBus();
    }

    /**
     * Discover and instantiate all @Mod classes from a candidate jar.
     */
    public void loadMod(ModCandidate candidate) {
        candidates.add(candidate);
        NextLog.info("ForgeModRuntime: loading mod {} from {}", candidate.id(), candidate.jarPath());

        List<String> modClasses = ForgeAnnotationScanner.findModClasses(candidate.jarPath());
        if (modClasses.isEmpty()) {
            NextLog.warn("ForgeModRuntime: no @Mod class found in {}, skipping", candidate.jarPath());
            return;
        }

        try {
            URL[] urls = { candidate.jarPath().toUri().toURL() };
            RemappingForgeClassLoader cl = new RemappingForgeClassLoader(urls, ForgeModRuntime.class.getClassLoader());
            classLoaders.add(cl);

            for (String className : modClasses) {
                try {
                    Class<?> modClass = Class.forName(className, true, cl);
                    Constructor<?> ctor = modClass.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    Object instance = ctor.newInstance();
                    modInstances.add(instance);

                    // Register @SubscribeEvent methods on the old internal bus (for game events)
                    ForgeEventBus legacyBus = new ForgeEventBus();
                    legacyBus.register(instance);

                    NextLog.info("ForgeModRuntime: instantiated {}", className);
                } catch (Throwable e) {
                    NextLog.warn("ForgeModRuntime: failed to instantiate {}: {}", className, e.toString());
                }
            }
        } catch (Exception e) {
            NextLog.warn("ForgeModRuntime: failed to create classloader for {}: {}",
                candidate.id(), e.getMessage());
        }
    }

    /**
     * Fire the full registry + setup event sequence on the shared mod bus.
     * This is where DeferredRegister actually creates and registers items.
     */
    public void fireRegistryEvents() {
        IEventBus bus = modBus();

        NextLog.info("ForgeModRuntime: firing RegistryEvent.NewRegistry");
        bus.post(new RegistryEvent.NewRegistry());

        // Fire Register<Item> — this triggers DeferredRegister to create items
        // and call BuiltInRegistries.ITEM.register()
        NextLog.info("ForgeModRuntime: firing RegistryEvent.Register<Item>");
        bus.post(new RegistryEvent.Register<>(resolveClass("net.minecraft.world.item.Item")));

        // Fire Register<Block>
        NextLog.info("ForgeModRuntime: firing RegistryEvent.Register<Block>");
        bus.post(new RegistryEvent.Register<>(resolveClass("net.minecraft.world.level.block.Block")));

        // Count registered items by checking the MC registry size
        countRegisteredItems();
    }

    private void countRegisteredItems() {
        try {
            Class<?> bir = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Object itemRegistry = bir.getField("ITEM").get(null);
            Method sizeMethod = itemRegistry.getClass().getMethod("size");
            int totalItems = (int) sizeMethod.invoke(itemRegistry);
            NextLog.info("ForgeModRuntime: BuiltInRegistries.ITEM now has {} total entries", totalItems);
            registeredItemCount = totalItems;
        } catch (Throwable t) {
            NextLog.debug("ForgeModRuntime: could not count items: {}", t.toString());
        }
    }

    public void fireSetupEvents() {
        IEventBus bus = modBus();
        NextLog.info("ForgeModRuntime: firing FMLCommonSetupEvent");
        bus.post(new FMLCommonSetupEvent());

        NextLog.info("ForgeModRuntime: firing FMLClientSetupEvent");
        bus.post(new FMLClientSetupEvent());
    }

    public void runFullLifecycle() {
        fireRegistryEvents();
        fireSetupEvents();
    }

    public void shutdown() {
        for (URLClassLoader cl : classLoaders) {
            try { cl.close(); } catch (Exception ignored) {}
        }
        classLoaders.clear();
        modInstances.clear();
        candidates.clear();
    }

    public int modCount() { return modInstances.size(); }
    public int getRegisteredItemCount() { return registeredItemCount; }

    private static Class<?> resolveClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            NextLog.warn("ForgeModRuntime: class not found: {}", name);
            return Object.class;
        }
    }
}
