package com.amaykov.finalproject;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import java.util.Collections;
import java.util.List;

public class BachelorAdmissionWaysActivity extends StepActivity {

    private RadioGroup admissionWayRadioGroup;

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_bachelor_admission_ways;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        if (resolveDegreeLevel() != DegreeLevel.BACHELOR) {
            startActivity(createNextIntent());
            return;
        }

        admissionWayRadioGroup = findViewById(R.id.admission_way_radio_group);
        for (AdmissionWay way : AdmissionWay.values()) {
            RadioButton radioButton = findViewById(way.getRadioButtonId());
            if (radioButton != null) {
                CompoundButtonCompat.setButtonTintList(radioButton, null);
            }
        }

        String savedKey = UserSelectionStore.of(this).getFirstValue(SelectionPool.BACHELOR_ADMISSION_WAY);
        AdmissionWay savedWay = AdmissionWay.fromStorageKey(savedKey);
        if (savedWay != null && admissionWayRadioGroup != null) {
            admissionWayRadioGroup.check(savedWay.getRadioButtonId());
        }
    }

    @Nullable
    @Override
    protected SelectionPool getSelectionPool() {
        return SelectionPool.BACHELOR_ADMISSION_WAY;
    }

    @NonNull
    @Override
    protected List<String> collectSelectedValues() {
        AdmissionWay selected = getSelectedAdmissionWay();
        if (selected == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(selected.getStorageKey());
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "Выберите способ поступления";
    }

    @NonNull
    @Override
    protected CharSequence getHelpMessage() {
        return "Выбери один способ поступления на бакалавриат.\n" +
                "После подтверждения анкета завершится.";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return MainActivity.class;
    }

    @Override
    protected boolean canNavigateNext() {
        if (getSelectedAdmissionWay() != null) {
            return true;
        }
        Toast.makeText(this, "Выбери способ поступления.", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Nullable
    private AdmissionWay getSelectedAdmissionWay() {
        if (admissionWayRadioGroup == null) {
            return null;
        }
        return AdmissionWay.fromRadioButtonId(admissionWayRadioGroup.getCheckedRadioButtonId());
    }

    @Nullable
    private DegreeLevel resolveDegreeLevel() {
        String key = UserSelectionStore.of(this).getFirstValue(SelectionPool.DEGREE);
        return DegreeLevel.fromStorageKey(key);
    }
}
