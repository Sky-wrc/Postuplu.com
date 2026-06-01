package com.amaykov.finalproject;

import androidx.annotation.NonNull;

/**
 * Специальность из {@code Master_Specialties.txt} или {@code Bachelors_Specialties.txt}.
 */
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
