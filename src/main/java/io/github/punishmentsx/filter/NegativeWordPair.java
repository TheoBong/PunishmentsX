package io.github.punishmentsx.filter;

class NegativeWordPair {
    private final String word;
    private final String[] matches;

    NegativeWordPair(final String word, final String... matches) {
        this.word = word;
        this.matches = matches;
    }

    public String getWord() {
        return this.word;
    }

    public String[] getMatches() {
        return this.matches;
    }
}
