package io.github.lijinhong11.supermines.message;

import it.unimi.dsi.fastutil.Pair;

public final class MessageReplacement implements Pair<String, String> {
    private final String placeholder;
    private final String replacement;

    private MessageReplacement(String placeholder, String replacement) {
        this.placeholder = placeholder;
        this.replacement = replacement;
    }

    public String parse(String message) {
        return message.replaceAll(left(), right());
    }

    public static MessageReplacement replace(String placeholder, String replacement) {
        return new MessageReplacement(placeholder, replacement);
    }

    @Override
    public String left() {
        return placeholder;
    }

    @Override
    public String right() {
        return replacement;
    }
}