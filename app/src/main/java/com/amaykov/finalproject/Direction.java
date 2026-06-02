package com.amaykov.finalproject;

import androidx.annotation.NonNull;

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
