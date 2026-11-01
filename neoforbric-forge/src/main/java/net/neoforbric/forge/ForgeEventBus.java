package net.neoforbric.forge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.neoforbric.common.NextLog;

/**
 * Forge-style event bus (internal NeoForbric implementation).
 * Mod code uses the net.minecraftforge.eventbus.api.IEventBus wrapper instead.
 */
public class ForgeEventBus {

    private final Map<Class<?>, List<Subscriber>> subscribers = new HashMap<>();

    public void register(Object modInstance) {
        List<Method> handlers = ForgeAnnotationScanner.findSubscribeEventMethods(modInstance.getClass());
        for (Method m : handlers) {
            if (m.getParameterCount() != 1) continue;
            Class<?> eventType = m.getParameterTypes()[0];
            m.setAccessible(true);
            subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(new Subscriber(modInstance, m));
        }
    }

    public void post(Object event) {
        Class<?> eventClass = event.getClass();
        List<Subscriber> all = new ArrayList<>();
        for (Map.Entry<Class<?>, List<Subscriber>> e : subscribers.entrySet()) {
            if (e.getKey().isAssignableFrom(eventClass)) {
                all.addAll(e.getValue());
            }
        }
        for (Subscriber s : all) {
            try {
                s.method.invoke(s.target, event);
            } catch (Exception e) {
                NextLog.warn("ForgeEventBus: handler failed: {}", e.getMessage());
            }
        }
    }

    public int subscriberCount() {
        return subscribers.values().stream().mapToInt(List::size).sum();
    }

    private record Subscriber(Object target, Method method) {}
}
