package net.neoforbric.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging for NeoForbric.
 */
public final class NextLog {
    private static final Logger LOG = Logger.getLogger("NeoForbric");

    static {
        LOG.setLevel(Level.ALL);
    }

    private NextLog() {}

    public static void info(String msg, Object... args) {
        LOG.info(() -> format(msg, args));
    }

    public static void warn(String msg, Object... args) {
        LOG.warning(() -> format(msg, args));
    }

    public static void debug(String msg, Object... args) {
        LOG.fine(() -> format(msg, args));
    }

    public static void error(String msg, Object... args) {
        LOG.severe(() -> format(msg, args));
    }

    private static String format(String msg, Object... args) {
        if (args == null || args.length == 0) return msg;
        StringBuilder sb = new StringBuilder(msg);
        for (Object arg : args) {
            int idx = sb.indexOf("{}");
            if (idx < 0) break;
            sb.replace(idx, idx + 2, String.valueOf(arg));
        }
        return sb.toString();
    }
}
