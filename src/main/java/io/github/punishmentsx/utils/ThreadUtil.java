package io.github.punishmentsx.utils;

import io.github.punishmentsx.PunishmentsX;

public class ThreadUtil {
    public static void runTask(boolean async, PunishmentsX plugin, Runnable runnable) {
        if(async) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }
}
