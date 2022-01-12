package io.github.punishmentsx.utils;

import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;

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
