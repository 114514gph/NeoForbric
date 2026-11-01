package net.neoforbric.forge;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.neoforbric.api.Ecosystem;
import net.neoforbric.api.LoadContext;
import net.neoforbric.api.LoadPhase;
import net.neoforbric.api.ModCandidate;
import net.neoforbric.api.ModLoader;
import net.neoforbric.common.ModsToml;
import net.neoforbric.api.util.ModDependencySorter;
import net.neoforbric.common.NextLog;

/**
 * MinecraftForge ecosystem loader.
 */
public class ForgeModLoader implements ModLoader {

    private final List<ModCandidate> discovered = new ArrayList<>();
    private final ForgeModRuntime runtime = new ForgeModRuntime();

    @Override
    public Ecosystem ecosystem() {
        return Ecosystem.MINECRAFTFORGE;
    }

    @Override
    public List<ModCandidate> discover(LoadContext context) {
        NextLog.info("ForgeModLoader: scanning {} for META-INF/mods.toml", context.modsDir());
        List<ModCandidate> out = new ArrayList<>();
        Path modsDir = context.modsDir();
        if (modsDir == null || !Files.isDirectory(modsDir)) return out;

        try (DirectoryStream<Path> jars = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path jar : jars) {
                var mods = ModsToml.readFromJar(jar, "META-INF/mods.toml");
                Ecosystem eco = Ecosystem.MINECRAFTFORGE;
                if (mods.isEmpty()) {
                    mods = ModsToml.readFromJar(jar, "META-INF/neoforge.mods.toml");
                    eco = Ecosystem.NEOFORGE;
                }
                if (mods.isEmpty()) continue;

                for (ModsToml.Mod m : mods.get()) {
                    out.add(new ModCandidate(m.modId(), m.version(), eco, jar, List.of()));
                    NextLog.info("  Found {} mod: {} {} ({})", eco, m.modId(), m.version(), jar.getFileName());
                }
            }
        } catch (IOException e) {
            NextLog.error("ForgeModLoader: scan failed: {}", e.toString());
        }
        return out;
    }

    @Override
    public void resolve(List<ModCandidate> candidates, LoadContext context) {
        discovered.clear();
        discovered.addAll(ModDependencySorter.sort(candidates, Map.of()));
        NextLog.info("ForgeModLoader: resolved {} forge mod(s)", discovered.size());
    }

    @Override
    public void runPhase(LoadPhase phase) {
        switch (phase) {
            case CONSTRUCT -> {
                NextLog.info("ForgeModLoader: CONSTRUCT - loading {} mods", discovered.size());
                for (ModCandidate c : discovered) {
                    runtime.loadMod(c);
                }
            }
            case REGISTRY -> {
                NextLog.info("ForgeModLoader: REGISTRY - firing register events");
                runtime.fireRegistryEvents();
            }
            case COMMON_SETUP -> {
                NextLog.info("ForgeModLoader: COMMON_SETUP + SIDED_SETUP");
                runtime.fireSetupEvents();
            }
            case LOAD_COMPLETE -> {
                NextLog.info("ForgeModLoader: {} mods loaded, {} registered items",
                    runtime.modCount(), runtime.getRegisteredItemCount());
            }
            default -> { /* no-op */ }
        }
    }

    public ForgeModRuntime runtime() { return runtime; }
}
