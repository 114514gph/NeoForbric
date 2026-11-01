package net.neoforged.neoforge.registries;

import net.minecraftforge.registries.IRegistry;

/**
 * NeoForge version of ForgeRegistries — same constants, different package.
 */
public final class NeoForgeRegistries {

    private NeoForgeRegistries() {}

    public static final IRegistry<Object> ITEMS = net.minecraftforge.registries.ForgeRegistries.ITEMS;
    public static final IRegistry<Object> BLOCKS = net.minecraftforge.registries.ForgeRegistries.BLOCKS;
}
