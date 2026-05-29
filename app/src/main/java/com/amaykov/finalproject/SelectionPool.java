package com.amaykov.finalproject;

/**
 * Ключ пула чекбоксов в {@link UserSelectionStore}.
 */
public enum SelectionPool {
    STUDY_DIRECTIONS("study_directions"),
    UNIVERSITIES("universities"),
    DEGREE("degree"),
    SPECIALTIES("specialties");

    final String key;

    SelectionPool(String key) {
        this.key = key;
    }
}
