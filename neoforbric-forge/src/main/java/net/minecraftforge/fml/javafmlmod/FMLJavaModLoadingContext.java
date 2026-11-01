package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.EventBusImpl;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * FMLJavaModLoadingContext — the main entry point mod code uses.
 * Mod code calls: FMLJavaModLoadingContext.get().getModEventBus()
 */
public class FMLJavaModLoadingContext {

    private static final IEventBus MOD_BUS = new EventBusImpl();
    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();

    private FMLJavaModLoadingContext() {}

    public static FMLJavaModLoadingContext get() {
        return INSTANCE;
    }

    public IEventBus getModEventBus() {
        return MOD_BUS;
    }

    public static IEventBus sharedModBus() {
        return MOD_BUS;
    }
}
