package net.neoforbric.remap;

/**
 * Minimal runtime remapper — returns bytes unchanged (no-op for now).
 */
public final class RuntimeRemapper {
    private RuntimeRemapper() {}

    public static byte[] remap(byte[] classBytes, RemapMappings mappings) {
        return classBytes;
    }
}
