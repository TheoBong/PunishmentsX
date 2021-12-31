package io.github.punishu.utils;

import io.github.punishu.PunishU;

public class ThreadUtil {
    public static void runTask(boolean async, PunishU plugin, Runnable runnable) {
        if(async) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        } else {
            runnable.run();
        }
    }
}
