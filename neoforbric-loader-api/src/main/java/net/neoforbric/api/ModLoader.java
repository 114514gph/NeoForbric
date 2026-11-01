package net.neoforbric.api;

import java.util.List;

public interface ModLoader {
    Ecosystem ecosystem();
    List<ModCandidate> discover(LoadContext context);
    void resolve(List<ModCandidate> candidates, LoadContext context);
    void runPhase(LoadPhase phase);
}
