package net.neoforbric.api;

import java.nio.file.Path;
import java.util.List;

public record ModCandidate(
    String id,
    String version,
    Ecosystem ecosystem,
    Path jarPath,
    List<String> dependencies
) {}
