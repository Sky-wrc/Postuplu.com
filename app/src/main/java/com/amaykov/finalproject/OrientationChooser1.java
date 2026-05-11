package com.amaykov.finalproject;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;

public class OrientationChooser1 extends StepActivity {

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_orientation_chooser1;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        for (StudyDirection d : StudyDirection.values()) {
            CheckBox cb = findViewById(d.getCheckboxId());
            if (cb != null) {
                CompoundButtonCompat.setButtonTintList(cb, null);
            }
        }
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "Выбери какие направления тебя интересуют";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return UniversityChooser2.class;
    }

    @Override
    protected boolean canNavigateNext() {
        for (StudyDirection direction : StudyDirection.values()) {
            CheckBox checkBox = findViewById(direction.getCheckboxId());
            if (checkBox != null && checkBox.isChecked()) {
                return true;
            }
        }
        Toast.makeText(this, "Ничего не выбрано — отметь хотя бы одно направление.", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onBeforeNavigateNext() {
        showSelectedDirections();
    }

    private void showSelectedDirections() {
        boolean hasSelected = false;
        for (StudyDirection direction : StudyDirection.values()) {
            CheckBox checkBox = findViewById(direction.getCheckboxId());
            if (checkBox != null && checkBox.isChecked()) {
                hasSelected = true;
            }
        }
        if (!hasSelected) {
            return;
        }
        //Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
    }
}
