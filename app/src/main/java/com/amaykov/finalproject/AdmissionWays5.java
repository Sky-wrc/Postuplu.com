package com.amaykov.finalproject;

import androidx.annotation.NonNull;

public class AdmissionWays5 extends StepActivity {

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_admission_ways5;
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "Способы поступления";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return WelcomeActivity.class;
    }
}
