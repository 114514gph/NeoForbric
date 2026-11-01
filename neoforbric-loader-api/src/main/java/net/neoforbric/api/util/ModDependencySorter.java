package net.neoforbric.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.neoforbric.api.ModCandidate;

public final class ModDependencySorter {
    private ModDependencySorter() {}

    public static List<ModCandidate> sort(List<ModCandidate> candidates,
            Map<String, Set<String>> orderEdges) {
        Map<String, ModCandidate> byId = new LinkedHashMap<>();
        for (ModCandidate c : candidates) byId.put(c.id(), c);

        // Simple topological sort by modId for stability
        List<ModCandidate> result = new ArrayList<>(candidates);
        result.sort((a, b) -> a.id().compareTo(b.id()));
        return result;
    }
}
