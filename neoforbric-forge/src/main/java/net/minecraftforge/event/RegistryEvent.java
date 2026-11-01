package net.minecraftforge.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * RegistryEvent fired during the registry phase.
 */
public abstract class RegistryEvent extends Event {

    public static class NewRegistry extends RegistryEvent {}

    public static class Register<T> extends RegistryEvent {
        private final Class<T> type;

        public Register(Class<T> type) {
            this.type = type;
        }

        public Class<T> getRegistryType() { return type; }
    }
}
