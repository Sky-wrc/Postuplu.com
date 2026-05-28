package com.amaykov.finalproject;

import androidx.annotation.NonNull;

/**
 * Вуз из {@code Universities.txt}: аббревиатура на чекбоксе, полное имя и описание в подсказке.
 */
public final class University {

    private final String abbreviation;
    private final String fullName;
    private final String description;
    private final String rawLine;

    public University(
            @NonNull String abbreviation,
            @NonNull String fullName,
            @NonNull String description,
            @NonNull String rawLine
    ) {
        this.abbreviation = abbreviation;
        this.fullName = fullName;
        this.description = description;
        this.rawLine = rawLine;
    }

    @NonNull
    public String getAbbreviation() {
        return abbreviation;
    }

    @NonNull
    public String getFullName() {
        return fullName;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public CharSequence getInfoMessage() {
        StringBuilder message = new StringBuilder();
        message.append(abbreviation);
        if (!fullName.isEmpty()) {
            message.append("\n\n").append(fullName);
        }
        if (!description.isEmpty()) {
            message.append("\n\n").append(description);
        }
        if (message.length() == abbreviation.length() && !rawLine.isEmpty()) {
            return rawLine;
        }
        return message;
    }
}
