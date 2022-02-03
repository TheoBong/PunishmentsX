package io.github.punishmentsx.filter;

import com.google.common.collect.ImmutableList;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.utils.StringUtil;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class Filter {
    public enum Reason {
        ADVERTISING, NEGATIVE_WORD_PAIR, BLACKLISTED_WORD;
    }

    private final Configuration config;
    private final boolean antiAdvertising;
    private final boolean blacklistedWords;
    private final boolean negativeWordPair;
    private final boolean advanced;

    public Filter(PunishmentsX plugin) {
        this.config = plugin.getConfig();

        this.antiAdvertising = config.getBoolean("FILTER.ANTI_ADVERTISING.ENABLED");
        this.blacklistedWords = config.getBoolean("FILTER.BLACKLISTED_WORDS.ENABLED");
        this.negativeWordPair = config.getBoolean("FILTER.NEGATIVE_WORD_PAIR.ENABLED");
        this.advanced = config.getBoolean("FILTER.ADVANCED");
    }

    private List<String> getBlacklistedWords() {
        return config.getStringList("FILTER.BLACKLISTED_WORDS.LIST");
    }

    private List<NegativeWordPair> getNegativeWordPairs() {
        final String[] words = config.getStringList("FILTER.NEGATIVE_WORD_PAIR.NEGATIVE_WORDS").toArray(new String[0]);
        final String[] matches = config.getStringList("FILTER.NEGATIVE_WORD_PAIR.NEGATIVE_WORDS").toArray(new String[0]);

        List<NegativeWordPair> pairs = new ArrayList<>();
        Arrays.stream(words).forEach(word -> pairs.add(new NegativeWordPair(word, matches)));

        return ImmutableList.copyOf(pairs);
    }

    private String[] getWhitelistedLinks() {
        return config.getStringList("FILTER.ANTI_ADVERTISING.WHITELISTED_LINKS").toArray(new String[0]);
    }

    public Reason filteredMessage(String message) {
        message = message.toLowerCase().trim();

        if (antiAdvertising) {
            if (Arrays.stream(message.split(" ")).map(StringUtil.IP_REGEX::matcher).anyMatch(Matcher::matches)) {
                return Reason.ADVERTISING;
            }

            String[] words = advanced ? message.replace("dot", ".").trim().split(" ") : message.trim().split(" ");

            for (String word : words) {
                Matcher matcher = StringUtil.URL_REGEX.matcher(word);

                boolean filtered = false;

                if (matcher.matches()) {
                    int matches = (int) Arrays.stream(getWhitelistedLinks()).filter(word::contains).count();

                    filtered = matches == 0;
                }

                if (filtered) {
                    return Reason.ADVERTISING;
                }
            }
        }

        String parsed = message;
        if (advanced) {
            parsed = message
                    .replace("3", "e")
                    .replace("1", "i")
                    .replace("!", "i")
                    .replace("/\\", "a")
                    .replace("/-\\", "a")
                    .replace("()", "o")
                    .replace("2", "z")
                    .replace("@", "a")
                    .replace("|", "l")
                    .replace("7", "t")
                    .replace("4", "a")
                    .replace("0", "o")
                    .replace("5", "s")
                    .replace("8", "b")
                    .replace("|\\|", "n")
                    .replace("[\\]", "n")
                    .replace("(\\)", "n")
                    .trim();
        }

        String noPuncParsed = parsed.replaceAll("\\p{Punct}|\\d", "").trim();

        if (negativeWordPair) {
            for (NegativeWordPair pair : getNegativeWordPairs()) {
                for (String match : pair.getMatches()) {
                    if (noPuncParsed.contains(pair.getWord()) && noPuncParsed.contains(match)) {
                        return Reason.NEGATIVE_WORD_PAIR;
                    }
                }
            }
        }

        if (blacklistedWords) {
            for (String phrase : getBlacklistedWords()) {
                if (parsed.contains(phrase)) {
                    return Reason.BLACKLISTED_WORD;
                }
            }

            Optional<String> filterablePhrase = getBlacklistedWords().stream().map(phrase -> phrase.replaceAll(" ", "")).filter(parsed::contains).findFirst();

            if (filterablePhrase.isPresent()) {
                return Reason.BLACKLISTED_WORD;
            }

            String[] split = parsed.trim().split(" ");

            if (Arrays.stream(split).anyMatch(getBlacklistedWords()::contains)) {
                return Reason.BLACKLISTED_WORD;
            }
        }

        return null;
    }
}

