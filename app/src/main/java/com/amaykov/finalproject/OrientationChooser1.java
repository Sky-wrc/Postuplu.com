package com.amaykov.finalproject;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.CompoundButtonCompat;

public class OrientationChooser1 extends WizardStepActivity {

    @Override
    protected int getWizardLayoutResId() {
        return R.layout.activity_orientation_chooser1;
    }

    @Override
    protected void onWizardStepReady(Bundle savedInstanceState) {
        // Тема задаёт buttonTint — для кастомного vector с обводкой тинт скрывает галочку / сам значок
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
        return getString(R.string.orientation_step_title);
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        return UniversityChooser2.class;
    }

    @Override
    protected void onBeforeNavigateNext() {
        showSelectedDirections();
    }

    private void showSelectedDirections() {
        StringBuilder selected = new StringBuilder(getString(R.string.selected_directions_prefix));
        boolean hasSelected = false;
        for (StudyDirection direction : StudyDirection.values()) {
            CheckBox checkBox = findViewById(direction.getCheckboxId());
            if (checkBox != null && checkBox.isChecked()) {
                if (hasSelected) {
                    selected.append(' ');
                }
                selected.append(direction.getDisplayLabel());
                hasSelected = true;
            }
        }
        if (!hasSelected) {
            Toast.makeText(this, R.string.selected_directions_none, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, selected, Toast.LENGTH_SHORT).show();
    }
}
