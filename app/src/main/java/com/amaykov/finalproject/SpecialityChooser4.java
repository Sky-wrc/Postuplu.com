package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpecialityChooser4 extends StepActivity {

    private static final String MSG_LOAD_ERROR = "Не удалось загрузить список специальностей";
    private static final String MSG_DEGREE_MISSING = "Сначала выбери ступень обучения";

    private final List<SpecialtyRow> specialtyRows = new ArrayList<>();

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_speciality_chooser4;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        DegreeLevel degreeLevel = resolveDegreeLevel();
        if (degreeLevel == null) {
            Toast.makeText(this, MSG_DEGREE_MISSING, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, DegreeChooser3.class));
            return;
        }

        LinearLayout container = findViewById(R.id.specialty_list_container);
        if (container == null) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        List<Specialty> specialties;
        try {
            specialties = SpecialtyCatalog.load(this, degreeLevel);
        } catch (IOException e) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        specialtyRows.clear();
        container.removeAllViews();
        Set<String> saved = new HashSet<>(UserSelectionStore.of(this).get(SelectionPool.SPECIALTIES));

        for (Specialty specialty : specialties) {
            View rowView = inflater.inflate(R.layout.item_specialty_row, container, false);
            CheckBox checkBox = rowView.findViewById(R.id.specialty_checkbox);
            if (checkBox == null) {
                continue;
            }

            checkBox.setText(specialty.getLabel());
            CompoundButtonCompat.setButtonTintList(checkBox, null);
            checkBox.setChecked(saved.contains(specialty.getLabel()));

            container.addView(rowView);
            specialtyRows.add(new SpecialtyRow(specialty, checkBox));
        }
    }

    @Nullable
    @Override
    protected SelectionPool getSelectionPool() {
        return SelectionPool.SPECIALTIES;
    }

    @NonNull
    @Override
    protected List<String> collectSelectedValues() {
        List<String> selected = new ArrayList<>();
        for (SpecialtyRow row : specialtyRows) {
            if (row.checkBox.isChecked()) {
                selected.add(row.specialty.getLabel());
            }
        }
        return selected;
    }

    @NonNull
    @Override
    protected String getStepTitle() {
        DegreeLevel degreeLevel = resolveDegreeLevel();
        if (degreeLevel == DegreeLevel.MASTER) {
            return "Выбери специальности (магистратура)";
        }
        if (degreeLevel == DegreeLevel.BACHELOR) {
            return "Выбери специальности (бакалавриат)";
        }
        return "Выбери специальности";
    }

    @NonNull
    @Override
    protected CharSequence getHelpMessage() {
        return "Отметь одну или несколько специальностей из списка.\n" +
                "Список соответствует выбранной ступени (бакалавриат или магистратура).";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass() {
        if (resolveDegreeLevel() == DegreeLevel.BACHELOR) {
            return BachelorAdmissionWaysActivity.class;
        }
        return MainActivity.class;
    }

    @Override
    protected boolean canNavigateNext() {
        for (SpecialtyRow row : specialtyRows) {
            if (row.checkBox.isChecked()) {
                return true;
            }
        }
        Toast.makeText(this, "Ничего не выбрано — отметь хотя бы одну специальность.", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Nullable
    private DegreeLevel resolveDegreeLevel() {
        String key = UserSelectionStore.of(this).getFirstValue(SelectionPool.DEGREE);
        return DegreeLevel.fromStorageKey(key);
    }

    private static final class SpecialtyRow {
        final Specialty specialty;
        final CheckBox checkBox;

        SpecialtyRow(Specialty specialty, CheckBox checkBox) {
            this.specialty = specialty;
            this.checkBox = checkBox;
        }
    }
}
