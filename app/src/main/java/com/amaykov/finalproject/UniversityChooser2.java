package com.amaykov.finalproject;

import androidx.annotation.NonNull;

public class UniversityChooser2 extends StepActivity {

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_university_chooser2;
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "Выбери вузы, которые тебя интересуют";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return AdmissionWays3.class;
    }
}
