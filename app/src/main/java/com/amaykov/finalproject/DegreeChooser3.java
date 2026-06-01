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

public class DegreeChooser3 extends StepActivity {

    private RadioGroup degreeRadioGroup;
    private RadioButton radioBachelor;
    private RadioButton radioMaster;

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_degree_chooser3;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        degreeRadioGroup = findViewById(R.id.degree_radio_group);
        radioBachelor = findViewById(R.id.radio_bachelor);
        radioMaster = findViewById(R.id.radio_master);
        if (radioBachelor != null) {
            radioBachelor.setText(DegreeLevel.BACHELOR.getDisplayLabel());
            CompoundButtonCompat.setButtonTintList(radioBachelor, null);
        }
        if (radioMaster != null) {
            radioMaster.setText(DegreeLevel.MASTER.getDisplayLabel());
            CompoundButtonCompat.setButtonTintList(radioMaster, null);
        }

        UserSelectionStore store = UserSelectionStore.of(this);
        String savedKey = store.getFirstValue(SelectionPool.DEGREE);
        DegreeLevel savedLevel = DegreeLevel.fromStorageKey(savedKey);
        if (savedLevel == DegreeLevel.BACHELOR) {
            degreeRadioGroup.check(R.id.radio_bachelor);
        } else if (savedLevel == DegreeLevel.MASTER) {
            degreeRadioGroup.check(R.id.radio_master);
        }
    }

    @Nullable
    @Override
    protected SelectionPool getSelectionPool() {
        return SelectionPool.DEGREE;
    }

    @NonNull
    @Override
    protected List<String> collectSelectedValues() {
        DegreeLevel selected = getSelectedDegreeLevel();
        if (selected == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(selected.getStorageKey());
    }

    @Override
    protected void onBeforeSaveSelections() {
        UserSelectionStore store = UserSelectionStore.of(this);
        String previousKey = store.getFirstValue(SelectionPool.DEGREE);
        DegreeLevel selected = getSelectedDegreeLevel();
        if (selected != null && (previousKey == null || !previousKey.equals(selected.getStorageKey()))) {
            store.remove(SelectionPool.SPECIALTIES);
            store.remove(SelectionPool.BACHELOR_ADMISSION_WAY);
        }
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "На какую ступень вы хотите поступить?";
    }

    @NonNull
    @Override
    protected CharSequence getHelpMessage() {
        return "Выбери одну ступень: бакалавриат или магистратуру.\n" +
                "На следующем шаге откроется список специальностей для выбранной ступени.";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return SpecialityChooser4.class;
    }

    @Override
    protected boolean canNavigateNext() {
        if (getSelectedDegreeLevel() != null) {
            return true;
        }
        Toast.makeText(this, "Выбери ступень обучения.", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Nullable
    private DegreeLevel getSelectedDegreeLevel() {
        if (degreeRadioGroup == null) {
            return null;
        }
        int checkedId = degreeRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_bachelor) {
            return DegreeLevel.BACHELOR;
        }
        if (checkedId == R.id.radio_master) {
            return DegreeLevel.MASTER;
        }
        return null;
    }
}
