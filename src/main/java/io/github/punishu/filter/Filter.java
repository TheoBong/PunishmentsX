package io.github.punishu.filter;

import com.google.common.collect.ImmutableList;
import io.github.punishu.utils.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class Filter {
    private static final List<String> FILTERED_PHRASES = ImmutableList.of(
            "nigger", "whore", "faggot"
    );

    private static final List<NegativeWordPair> NEGATIVE_WORD_PAIRS;
    private static final String[] SINGLE_FILTERED_WORDS = {};
    private static final String[] WHITELISTED_LINKS = {
            "youtube.com", "youtu.be", "imgur.com", "prntscr.com", "prnt.sc", "gfycat.com", "gyazo.com",
            "twitter.com", "spotify.com", "twitch.tv", "tinypic.com"
    };

    static {
        final String[] words = new String[]{};
        final String[] matches = new String[]{};

        final List<NegativeWordPair> pairs = Arrays.stream(words).map(word -> new NegativeWordPair(word, matches)).collect(Collectors.toList());

        pairs.add(new NegativeWordPair("server", "ass", "trash", "horrible", "garbage", "terrible", "awful"));

        NEGATIVE_WORD_PAIRS = ImmutableList.copyOf(pairs);
    }

    public String isFiltered(String msg) {
        msg = msg.toLowerCase().trim();

        if (Arrays.stream(msg.split(" ")).map(StringUtil.IP_REGEX::matcher).anyMatch(Matcher::matches)) {
            return "advertising";
        }

        for (final String word : msg
                .replace("9", "g")
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
                .trim().split(" ")) {
            final Matcher matcher = StringUtil.URL_REGEX.matcher(word);

            boolean filtered = false;

            if (matcher.matches()) {
                final int matches = (int) Arrays.stream(WHITELISTED_LINKS).filter(word::contains).count();

                filtered = matches == 0;
            }

            if (filtered) {
                return "advertising";
            }
        }

        for (final String word : msg
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
                .replace(" ", "")
                .trim().split(" ")) {
            final Matcher matcher = StringUtil.URL_REGEX.matcher(word);

            boolean filtered = false;

            if (matcher.matches()) {
                final int matches = (int) Arrays.stream(WHITELISTED_LINKS).filter(word::contains).count();

                filtered = matches == 0;
            }

            if (filtered) {
                return "advertising";
            }
        }

        for (final String word : msg
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
                .trim().replaceAll("\\p{Punct}|\\d", ":").replace(":dot:", ".").split(" ")) {
            final Matcher matcher = StringUtil.URL_REGEX.matcher(word);

            boolean filtered = false;

            if (matcher.matches()) {
                final int matches = (int) Arrays.stream(WHITELISTED_LINKS).filter(word::contains).count();

                filtered = matches == 0;
            }

            if (filtered) {
                return "advertising";
            }
        }


        for (final String word : msg
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
                .replace(" ", "")
                .trim().replaceAll("\\p{Punct}|\\d", ":").replace(":dot:", ".").split(" ")) {
            final Matcher matcher = StringUtil.URL_REGEX.matcher(word);

            boolean filtered = false;

            if (matcher.matches()) {
                final int matches = (int) Arrays.stream(WHITELISTED_LINKS).filter(word::contains).count();

                filtered = matches == 0;
            }

            if (filtered) {
                return "advertising";
            }
        }

        final String parsed = msg
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

        final String noPuncParsed = parsed.replaceAll("\\p{Punct}|\\d", "").trim();

        for (final String word : SINGLE_FILTERED_WORDS) {
            if (noPuncParsed.equalsIgnoreCase(word) || noPuncParsed.startsWith(word + " ")
                    || noPuncParsed.endsWith(" " + word) || noPuncParsed.contains(" " + word + " ")) {
                return "hate speech";
            }
        }

        for (final NegativeWordPair pair : NEGATIVE_WORD_PAIRS) {
            for (final String match : pair.getMatches()) {
                if (noPuncParsed.contains(pair.getWord()) && noPuncParsed.contains(match)) {
                    return "hate speech";
                }
            }
        }

        for (final String phrase : FILTERED_PHRASES) {
            if (parsed.contains(phrase)) {
                return "hate speech";
            }
        }

        final Optional<String> filterablePhrase = FILTERED_PHRASES.stream().map(phrase -> phrase.replaceAll(" ", "")).filter(parsed::contains).findFirst();

        if (filterablePhrase.isPresent()) {
            return "hate speech";
        }

        final String[] split = parsed.trim().split(" ");

        return Arrays.stream(split).anyMatch(FILTERED_PHRASES::contains) ? "hate speech" : null;
    }
}

