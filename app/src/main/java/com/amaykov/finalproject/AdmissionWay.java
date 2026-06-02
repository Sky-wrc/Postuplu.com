package com.amaykov.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum AdmissionWay {
    EGE_BUDGET("ege_budget", "ЕГЭ(бюджет)", R.id.radio_admission_ege_budget),
    VI("vi", "ВИ", R.id.radio_admission_vi),
    BVI("bvi", "БВИ", R.id.radio_admission_bvi),
    TARGET("target", "Целевой набор", R.id.radio_admission_target),
    QUOTA("quota", "Квота/Льгота", R.id.radio_admission_quota),
    EGE_PAID("ege_paid", "ЕГЭ(платка)", R.id.radio_admission_ege_paid);

    private final String storageKey;
    private final String displayLabel;
    private final int radioButtonId;

    AdmissionWay(String storageKey, String displayLabel, int radioButtonId) {
        this.storageKey = storageKey;
        this.displayLabel = displayLabel;
        this.radioButtonId = radioButtonId;
    }

    @NonNull
    public String getStorageKey() {
        return storageKey;
    }

    @NonNull
    public String getDisplayLabel() {
        return displayLabel;
    }

    public int getRadioButtonId() {
        return radioButtonId;
    }

    @Nullable
    public static AdmissionWay fromStorageKey(@Nullable String key) {
        if (key == null) {
            return null;
        }
        for (AdmissionWay way : values()) {
            if (way.storageKey.equals(key)) {
                return way;
            }
        }
        return null;
    }

    @Nullable
    public static AdmissionWay fromRadioButtonId(int radioButtonId) {
        for (AdmissionWay way : values()) {
            if (way.radioButtonId == radioButtonId) {
                return way;
            }
        }
        return null;
    }
}
