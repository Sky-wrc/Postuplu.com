package com.amaykov.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum DegreeLevel {
    BACHELOR("bachelor", "Бакалавриат", "Bachelors_Specialties.txt"),
    MASTER("master", "Магистратура", "Master_Specialties.txt");

    private final String storageKey;
    private final String displayLabel;
    private final String specialtiesAssetFile;

    DegreeLevel(String storageKey, String displayLabel, String specialtiesAssetFile) {
        this.storageKey = storageKey;
        this.displayLabel = displayLabel;
        this.specialtiesAssetFile = specialtiesAssetFile;
    }

    @NonNull
    public String getStorageKey() {
        return storageKey;
    }

    @NonNull
    public String getDisplayLabel() {
        return displayLabel;
    }

    @NonNull
    public String getSpecialtiesAssetFile() {
        return specialtiesAssetFile;
    }

    @Nullable
    public static DegreeLevel fromStorageKey(@Nullable String key) {
        if (key == null) {
            return null;
        }
        for (DegreeLevel level : values()) {
            if (level.storageKey.equals(key)) {
                return level;
            }
        }
        return null;
    }
}
