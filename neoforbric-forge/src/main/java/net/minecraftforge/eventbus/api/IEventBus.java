package net.minecraftforge.eventbus.api;

import java.util.function.Consumer;

/**
 * Forge event bus interface as seen by mod code.
 */
public interface IEventBus {
    void register(Object target);
    void unregister(Object target);
    <T> void addListener(Consumer<T> consumer);
    boolean post(Object event);
    boolean hasListeners(Class<?> eventType);
}
