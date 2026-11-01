package net.fabricmc.loader.api.entrypoint;

/**
 * Fabric PreLaunchEntrypoint — runs before MC bootstrap.
 */
public interface PreLaunchEntrypoint {
    void onPreLaunch();
}
