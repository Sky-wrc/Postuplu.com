package com.amaykov.finalproject;

public enum StudyDirection {
    IT(R.id.chkIT, "Информационные технологии (IT)"),
    ENGINEERING(R.id.chkEngineering, "Инженерия и технические науки"),
    NATURAL_SCIENCE(R.id.chkNaturalScience, "Естественные науки"),
    MEDICINE(R.id.chkMedicine, "Медицина и здравоохранение"),
    AGRICULTURE(R.id.chkAgriculture, "Сельское хозяйство"),
    ECONOMICS(R.id.chkEconomics, "Экономика, менеджмент и управление"),
    EDUCATION(R.id.chkEducation, "Педагогика и образование"),
    LAW(R.id.chkLaw, "Юриспруденция"),
    HUMANITIES(R.id.chkHumanities, "Гуманитарные и социальные науки"),
    INTERNATIONAL(R.id.chkInternational, "Международные отношения и регионоведение"),
    CREATIVE(R.id.chkCreative, "Творческие и культурные направления");

    private final int checkboxId;
    private final String displayLabel;

    StudyDirection(int checkboxId, String displayLabel) {
        this.checkboxId = checkboxId;
        this.displayLabel = displayLabel;
    }

    public int getCheckboxId() {
        return checkboxId;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
}
