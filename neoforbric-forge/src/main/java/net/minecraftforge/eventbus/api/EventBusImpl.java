package net.minecraftforge.eventbus.api;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.neoforbric.common.NextLog;

/**
 * Concrete IEventBus implementation.
 */
public class EventBusImpl implements IEventBus {

    private final Map<Class<?>, List<Subscriber>> subscribers = new HashMap<>();

    @Override
    public void register(Object target) {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getAnnotation(SubscribeEvent.class) == null) continue;
            if (m.getParameterCount() != 1) continue;
            Class<?> eventType = m.getParameterTypes()[0];
            m.setAccessible(true);
            subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
                .add(new ReflectiveSubscriber(target, m));
        }
    }

    @Override
    public void unregister(Object target) {
        subscribers.values().forEach(list ->
            list.removeIf(s -> s.target() == target));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> void addListener(Consumer<T> consumer) {
        Class<?> eventType = resolveListenerType(consumer);
        if (eventType == null) return;
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(new LambdaSubscriber(consumer));
    }

    private Class<?> resolveListenerType(Consumer<?> consumer) {
        try {
            Class<?> cls = consumer.getClass();
            for (Type iface : cls.getGenericInterfaces()) {
                if (iface instanceof ParameterizedType pt
                        && pt.getRawType() == Consumer.class) {
                    Type t = pt.getActualTypeArguments()[0];
                    if (t instanceof Class<?> c) return c;
                }
            }
            for (Method m : cls.getDeclaredMethods()) {
                if (m.getName().equals("accept") && m.getParameterCount() == 1) {
                    return m.getParameterTypes()[0];
                }
            }
        } catch (Throwable t) {
            NextLog.debug("EventBus: listener type resolution failed: {}", t.toString());
        }
        return null;
    }

    @Override
    public boolean post(Object event) {
        Class<?> eventClass = event.getClass();
        List<Subscriber> all = new ArrayList<>();
        for (Map.Entry<Class<?>, List<Subscriber>> e : subscribers.entrySet()) {
            if (e.getKey().isAssignableFrom(eventClass)) {
                all.addAll(e.getValue());
            }
        }
        for (Subscriber s : all) {
            try {
                s.invoke(event);
            } catch (Throwable t) {
                NextLog.warn("EventBus: listener failed: {}", t.toString());
            }
        }
        return true;
    }

    @Override
    public boolean hasListeners(Class<?> eventType) {
        for (Class<?> c : subscribers.keySet()) {
            if (c.isAssignableFrom(eventType)) return true;
        }
        return false;
    }

    public int subscriberCount() {
        return subscribers.values().stream().mapToInt(List::size).sum();
    }

    private sealed interface Subscriber permits ReflectiveSubscriber, LambdaSubscriber {
        Object target();
        void invoke(Object event) throws Exception;
    }

    private record ReflectiveSubscriber(Object target, Method method) implements Subscriber {
        @Override
        public void invoke(Object event) throws Exception {
            method.invoke(target, event);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private record LambdaSubscriber(Consumer consumer) implements Subscriber {
        @Override
        public Object target() { return consumer; }
        @Override
        public void invoke(Object event) {
            consumer.accept(event);
        }
    }
}
