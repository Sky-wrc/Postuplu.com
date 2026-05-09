package com.amaykov.finalproject;

import androidx.annotation.NonNull;

public class UniversityChooser2 extends WizardStepActivity {

    @Override
    protected int getWizardLayoutResId() {
        return R.layout.activity_university_chooser2;
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return getString(R.string.university_step_title);
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return AdmissionWays3.class;
    }
}
