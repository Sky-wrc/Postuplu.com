package com.amaykov.finalproject;

import androidx.annotation.NonNull;

public final class Specialty {

    private final String label;

    public Specialty(@NonNull String label) {
        this.label = label;
    }

    @NonNull
    public String getLabel() {
        return label;
    }
}
