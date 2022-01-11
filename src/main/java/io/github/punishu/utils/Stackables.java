package io.github.punishu.utils;

import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;

public class Stackables {
    public static int offenseNumber(Profile profile, String stack) {
        int offenses = 0;
        for (Punishment punishment : profile.getPunishmentsHistory()) {
            if (punishment.getStack().equals(stack)) {
                offenses++;
            }
        }

        return offenses;
    }
}
