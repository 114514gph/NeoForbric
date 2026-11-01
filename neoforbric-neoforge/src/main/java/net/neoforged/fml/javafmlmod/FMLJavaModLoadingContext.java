package net.neoforged.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * NeoForge version of FMLJavaModLoadingContext — same API, different package.
 * Delegates to the shared Forge implementation.
 */
public class FMLJavaModLoadingContext {

    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();

    private FMLJavaModLoadingContext() {}

    public static FMLJavaModLoadingContext get() {
        return INSTANCE;
    }

    public IEventBus getModEventBus() {
        return net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.sharedModBus();
    }
}
