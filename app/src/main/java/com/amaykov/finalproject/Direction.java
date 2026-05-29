package com.amaykov.finalproject;

import androidx.annotation.NonNull;

/**
 * Направление из {@code assets/Directions.txt} (одна непустая строка — одна запись).
 */
public final class Direction {

    private final String label;

    public Direction(@NonNull String label) {
        this.label = label;
    }

    @NonNull
    public String getLabel() {
        return label;
    }
}
