package io.github.punishmentsx.utils;

import io.github.punishmentsx.punishments.Punishment;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslatePunishment {
    public static Punishment.Type type(String punishmentString) {
        String[] newString = punishmentString.split(":");

        Punishment.Type type;
        switch (newString[0]) {
            case "ban":
                type = Punishment.Type.BAN;
                break;
            case "blacklist":
                type = Punishment.Type.BLACKLIST;
                break;
            case "kick":
                type = Punishment.Type.KICK;
                break;
            case "mute":
                type = Punishment.Type.MUTE;
                break;
            case "warn":
                type = Punishment.Type.WARN;
                break;
            default:
                type = null;

        }

        return type;
    }

    public static Date expires(String punishmentString) {
        String[] newString = punishmentString.split(":");

        if (newString[1].equals("permanent")) {
            return null;
        }

        Pattern p = Pattern.compile("[a-z]+|\\d+");
        Matcher m = p.matcher(newString[1].toLowerCase());

        int time = -1;
        String type = null;
        boolean b = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        while (m.find()) {
            String a = m.group();
            try {
                time = Integer.parseInt(a);
                if(time < 1) {
                    time = -1;
                }
            } catch(NumberFormatException e) {
                type = a;
            }

            if(time > 0 && type != null) {
                switch(type) {
                    case "seconds": case "second": case "sec": case "s":
                        calendar.add(Calendar.SECOND, time);
                        break;
                    case "minutes": case "minute": case "m":
                        calendar.add(Calendar.MINUTE, time);
                        break;
                    case "hours": case "hrs": case "hr": case "h":
                        calendar.add(Calendar.HOUR, time);
                        break;
                    case "days": case "day": case "d":
                        calendar.add(Calendar.HOUR, time * 24);
                        break;
                    case "weeks": case "week": case "w":
                        calendar.add(Calendar.HOUR, time * 24 * 7);
                        break;
                    case "months": case "month": case "mo":
                        calendar.add(Calendar.MONTH, time);
                        break;
                }

                b = true;
                time = -1;
                type = null;
            }
        }

        if (b) {
            return calendar.getTime();
        } else {
            return null;
        }
    }
}
