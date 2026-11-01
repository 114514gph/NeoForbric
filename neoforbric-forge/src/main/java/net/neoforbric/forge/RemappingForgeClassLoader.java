package net.neoforbric.forge;

import net.neoforbric.common.NextLog;
import net.neoforbric.remap.RuntimeRemapper;
import net.neoforbric.remap.RemapMappings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassLoader that remaps Forge mod class references at runtime.
 */
public class RemappingForgeClassLoader extends URLClassLoader {

    private final RemapMappings mappings = new RemapMappings();

    public RemappingForgeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classPath = name.replace('.', '/') + ".class";
        URL resource = findResource(classPath);
        if (resource == null) {
            throw new ClassNotFoundException(name);
        }
        byte[] classBytes;
        try (InputStream is = resource.openStream()) {
            classBytes = readAll(is);
        } catch (IOException e) {
            throw new ClassNotFoundException("Cannot read " + name, e);
        }
        byte[] remapped = RuntimeRemapper.remap(classBytes, mappings);
        return defineClass(name, remapped, 0, remapped.length);
    }

    private byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = is.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        return bos.toByteArray();
    }
}
