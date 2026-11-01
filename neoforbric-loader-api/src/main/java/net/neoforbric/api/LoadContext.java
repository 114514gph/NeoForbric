package net.neoforbric.api;

import java.nio.file.Path;

public record LoadContext(
    Path gameDir,
    Path modsDir,
    Path mcJar,
    Environment environment
) {
    public enum Environment { CLIENT, SERVER }
}
