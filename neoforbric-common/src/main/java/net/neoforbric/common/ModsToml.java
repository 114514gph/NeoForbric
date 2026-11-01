package net.neoforbric.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * Minimal mods.toml parser.
 */
public final class ModsToml {

    public record Mod(String modId, String version) {}

    private ModsToml() {}

    public static Optional<List<Mod>> readFromJar(Path jarPath, String tomlPath) {
        List<Mod> mods = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            JarEntry entry = jar.getJarEntry(tomlPath);
            if (entry == null) return Optional.empty();
            try (InputStream is = jar.getInputStream(entry)) {
                String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String modId = null;
                String version = "1.0";
                boolean inModsSection = false;
                for (String line : content.split("\n")) {
                    line = line.trim();
                    // Track which TOML section we're in
                    if (line.startsWith("[[")) {
                        inModsSection = line.equals("[[mods]]");
                        continue;
                    }
                    if (!inModsSection) continue;
                    // Only parse modId/version inside [[mods]] section
                    if (line.startsWith("modId") && line.contains("=")) {
                        int eq = line.indexOf('=');
                        modId = line.substring(eq + 1).replace("\"", "").trim();
                    } else if (line.startsWith("version") && line.contains("=") && !line.contains("versionRange")) {
                        int eq = line.indexOf('=');
                        version = line.substring(eq + 1).replace("\"", "").trim();
                    }
                }
                if (modId != null) {
                    mods.add(new Mod(modId, version));
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(mods);
    }
}
