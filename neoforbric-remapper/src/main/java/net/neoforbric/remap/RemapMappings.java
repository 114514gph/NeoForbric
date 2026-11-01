package net.neoforbric.remap;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class mapping table for runtime remapping.
 */
public class RemapMappings {
    private final Map<String, String> mappings = new HashMap<>();

    public void addClassMapping(String oldName, String newName) {
        mappings.put(oldName, newName);
    }

    public String mapClass(String internalName) {
        return mappings.getOrDefault(internalName, internalName);
    }

    public boolean isEmpty() { return mappings.isEmpty(); }
}
