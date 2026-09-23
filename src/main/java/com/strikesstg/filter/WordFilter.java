package com.strikesstg.filter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WordFilter {

    public record FilterResult(boolean found, Component censored, List<String> matchedWords) {
        public int strikeCount() {
            return matchedWords.size();
        }
    }

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final Pattern pattern;

    public WordFilter(List<String> bannedWords) {
        if (bannedWords == null || bannedWords.isEmpty()) {
            this.pattern = null;
            return;
        }

        List<String> sorted = bannedWords.stream()
                .filter(word -> word != null && !word.isBlank())
                .map(String::trim)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .map(Pattern::quote)
                .toList();

        if (sorted.isEmpty()) {
            this.pattern = null;
            return;
        }

        // Whole-word match only; "classic" will not match a banned word "ass".
        this.pattern = Pattern.compile(
                "\\b(?:" + String.join("|", sorted) + ")\\b",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        );
    }

    public FilterResult filter(String input) {
        if (pattern == null || input == null || input.isEmpty()) {
            return new FilterResult(false, Component.text(input == null ? "" : input), List.of());
        }

        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return new FilterResult(false, Component.text(input), List.of());
        }

        matcher.reset();
        List<String> matches = new ArrayList<>();
        Component result = Component.empty();
        int last = 0;

        while (matcher.find()) {
            if (matcher.start() > last) {
                result = result.append(Component.text(input.substring(last, matcher.start())));
            }
            matches.add(matcher.group().toLowerCase(Locale.ROOT));
            result = result.append(Component.text("***", NamedTextColor.RED));
            last = matcher.end();
        }

        if (last < input.length()) {
            result = result.append(Component.text(input.substring(last)));
        }

        return new FilterResult(true, result, List.copyOf(matches));
    }

    public FilterResult filter(Component component) {
        return filter(PLAIN.serialize(component == null ? Component.empty() : component));
    }
}
