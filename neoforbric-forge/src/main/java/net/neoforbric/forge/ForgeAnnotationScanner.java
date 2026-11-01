package net.neoforbric.forge;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.neoforbric.common.NextLog;

/**
 * Scans a Forge mod JAR for classes annotated with @Mod.
 */
public class ForgeAnnotationScanner {

    public static List<String> findModClasses(java.nio.file.Path jarPath) {
        List<String> result = new ArrayList<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) continue;
                if (hasModAnnotation(jar, entry)) {
                    String className = entry.getName()
                        .replace('/', '.')
                        .replace(".class", "");
                    result.add(className);
                }
            }
        } catch (IOException e) {
            NextLog.warn("ForgeAnnotationScanner: failed to scan {}: {}", jarPath, e.getMessage());
        }
        return result;
    }

    private static boolean hasModAnnotation(JarFile jar, JarEntry entry) throws IOException {
        try (DataInputStream dis = new DataInputStream(jar.getInputStream(entry))) {
            dis.readInt();
            dis.readUnsignedShort();
            dis.readUnsignedShort();
            int cpCount = dis.readUnsignedShort();
            for (int i = 1; i < cpCount; i++) {
                int tag = dis.readUnsignedByte();
                switch (tag) {
                    case 1 -> {
                        int length = dis.readUnsignedShort();
                        byte[] bytes = dis.readNBytes(length);
                        String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                        if (s.contains("fml/common/Mod") || s.contains("neoforged/fml/common/Mod")) {
                            return true;
                        }
                    }
                    case 3, 4 -> dis.readInt();
                    case 5, 6 -> { dis.readLong(); i++; }
                    case 7, 8 -> dis.readUnsignedShort();
                    case 9, 10, 11, 12 -> dis.readInt();
                    case 15 -> { dis.readUnsignedByte(); dis.readUnsignedShort(); }
                    case 16 -> dis.readUnsignedShort();
                    case 17, 18 -> dis.readInt();
                    case 19, 20 -> dis.readInt();
                    default -> { return false; }
                }
            }
        }
        return false;
    }

    public static List<Method> findSubscribeEventMethods(Class<?> modClass) {
        List<Method> result = new ArrayList<>();
        Method[] methods;
        try {
            methods = modClass.getDeclaredMethods();
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            return result;
        }
        for (Method m : methods) {
            for (Annotation a : m.getAnnotations()) {
                String typeName = a.annotationType().getName();
                if (typeName.contains("SubscribeEvent")) {
                    result.add(m);
                    break;
                }
            }
        }
        return result;
    }
}
