package com.amaykov.finalproject;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrientationChooser1 extends StepActivity {

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_orientation_chooser1;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        Set<String> saved = new HashSet<>(UserSelectionStore.of(this).get(SelectionPool.STUDY_DIRECTIONS));
        for (StudyDirection direction : StudyDirection.values()) {
            CheckBox checkBox = findViewById(direction.getCheckboxId());
            if (checkBox == null) {
                continue;
            }
            CompoundButtonCompat.setButtonTintList(checkBox, null);
            checkBox.setChecked(saved.contains(direction.name()));
        }
    }

    @Nullable
    @Override
    protected SelectionPool getSelectionPool() {
        return SelectionPool.STUDY_DIRECTIONS;
    }

    @NonNull
    @Override
    protected List<String> collectSelectedValues() {
        List<String> selected = new ArrayList<>();
        for (StudyDirection direction : StudyDirection.values()) {
            CheckBox checkBox = findViewById(direction.getCheckboxId());
            if (checkBox != null && checkBox.isChecked()) {
                selected.add(direction.name());
            }
        }
        return selected;
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        return "Выбери какие направления тебя интересуют";
    }

    @NonNull
    @Override
    protected CharSequence getHelpMessage() {
        return "Отметь одно или несколько направлений, которые тебе интересны.\n" +
                "Потом нажми «Далее», чтобы перейти к выбору вузов";
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
}
