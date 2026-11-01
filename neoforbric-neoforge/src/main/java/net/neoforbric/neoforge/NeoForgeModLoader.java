package net.neoforbric.neoforge;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.neoforbric.api.Ecosystem;
import net.neoforbric.api.LoadContext;
import net.neoforbric.api.LoadPhase;
import net.neoforbric.api.ModCandidate;
import net.neoforbric.api.ModLoader;
import net.neoforbric.common.ModsToml;
import net.neoforbric.common.NextLog;
import net.neoforbric.api.util.ModDependencySorter;

/**
 * NeoForge ecosystem loader.
 */
public class NeoForgeModLoader implements ModLoader {

    private final List<ModCandidate> discovered = new ArrayList<>();

    @Override
    public Ecosystem ecosystem() {
        return Ecosystem.NEOFORGE;
    }

    @Override
    public List<ModCandidate> discover(LoadContext context) {
        NextLog.info("NeoForgeModLoader: scanning {} for neoforge.mods.toml", context.modsDir());
        List<ModCandidate> out = new ArrayList<>();
        Path modsDir = context.modsDir();
        if (modsDir == null || !Files.isDirectory(modsDir)) return out;
        try (DirectoryStream<Path> jars = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path jar : jars) {
                var mods = ModsToml.readFromJar(jar, "META-INF/neoforge.mods.toml");
                if (mods.isEmpty()) continue;
                for (ModsToml.Mod m : mods.get()) {
                    out.add(new ModCandidate(m.modId(), m.version(),
                        Ecosystem.NEOFORGE, jar, List.of()));
                }
            }
        } catch (IOException e) {
            NextLog.error("NeoForgeModLoader: scan failed: {}", e.toString());
        }
        return out;
    }

    @Override
    public void resolve(List<ModCandidate> candidates, LoadContext context) {
        discovered.clear();
        discovered.addAll(ModDependencySorter.sort(candidates, java.util.Map.of()));
    }

    @Override
    public void runPhase(LoadPhase phase) {
        // NeoForge mods are loaded through ForgeModRuntime which handles both
        // Forge and NeoForge packages. This loader just discovers them.
        switch (phase) {
            case CONSTRUCT -> NextLog.info("NeoForgeModLoader: constructing {} mods", discovered.size());
            case REGISTRY -> NextLog.info("NeoForgeModLoader: REGISTRY phase");
            case LOAD_COMPLETE -> NextLog.info("NeoForgeModLoader: load complete");
            default -> {}
        }
    }
}
