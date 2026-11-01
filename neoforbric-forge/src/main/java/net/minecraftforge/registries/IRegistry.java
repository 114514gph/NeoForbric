package net.minecraftforge.registries;

/**
 * Forge's IRegistry<T> — a typed handle to a specific registry.
 */
public interface IRegistry<T> {
    String key();
    Object mcRegistry();
}
