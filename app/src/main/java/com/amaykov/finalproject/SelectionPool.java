package com.amaykov.finalproject;

/**
 * Ключ пула чекбоксов в {@link UserSelectionStore}.
 */
public enum SelectionPool {
    STUDY_DIRECTIONS("study_directions"),
    UNIVERSITIES("universities"),
    DEGREE("degree"),
    SPECIALTIES("specialties"),
    BACHELOR_ADMISSION_WAY("bachelor_admission_way");

    final String key;

    SelectionPool(String key) {
        this.key = key;
    }
}
