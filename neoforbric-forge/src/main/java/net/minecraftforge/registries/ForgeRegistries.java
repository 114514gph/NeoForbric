package net.minecraftforge.registries;

import net.neoforbric.common.NextLog;

/**
 * ForgeRegistries — entry point mods use to reference registries.
 * Uses reflection to access MC's BuiltInRegistries at runtime.
 */
public final class ForgeRegistries {

    private ForgeRegistries() {}

    public static final IRegistry<Object> ITEMS = create("item", "minecraft:item");
    public static final IRegistry<Object> BLOCKS = create("block", "minecraft:block");

    private static <T> IRegistry<T> create(String fieldName, String key) {
        Object mcReg = resolveBuiltInRegistry(fieldName);
        return new McIRegistry<>(key, mcReg);
    }

    private static Object resolveBuiltInRegistry(String fieldName) {
        try {
            Class<?> bir = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            return bir.getField(fieldName.toUpperCase()).get(null);
        } catch (Throwable t) {
            NextLog.debug("ForgeRegistries: BuiltInRegistries.{} not available: {}", fieldName, t.toString());
            return null;
        }
    }

    private record McIRegistry<T>(String key, Object mcRegistry) implements IRegistry<T> {}
}
