package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SpecialityChooser4 extends StepActivity {

    private static final int MAX_SPECIALTIES = 5;
    private static final String MSG_LOAD_ERROR = "Не удалось загрузить список специальностей";
    private static final String MSG_DEGREE_MISSING = "Сначала выбери ступень обучения";
    private static final String MSG_MAX_SPECIALTIES = "Можно выбрать не больше 5 специальностей.";

    private final List<SpecialtyRow> specialtyRows = new ArrayList<>();
    private final List<CheckBox> specialtyCheckBoxes = new ArrayList<>();

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
            if (degreeLevel == DegreeLevel.BACHELOR || degreeLevel == DegreeLevel.MASTER) {
                // сортировка специальностей по тегам выбранных направлений
                List<SpecialtyWithTags> tagged = new ArrayList<>(
                        degreeLevel == DegreeLevel.BACHELOR
                                ? BachelorSpecialtyTagsCatalog.load(this)
                                : MasterSpecialtyTagsCatalog.load(this)
                );
                List<String> selectedDirections = UserSelectionStore.of(this)
                        .get(SelectionPool.STUDY_DIRECTIONS);
                Map<String, Integer> directionRank = new HashMap<>();
                for (int i = 0; i < selectedDirections.size(); i++) {
                    directionRank.put(selectedDirections.get(i), i);
                }
                tagged.sort((a, b) -> {
                    int ra = bestTagRank(a.tags, directionRank);
                    int rb = bestTagRank(b.tags, directionRank);
                    if (ra != rb) {
                        return Integer.compare(ra, rb);
                    }
                    return Integer.compare(a.sourceIndex, b.sourceIndex);
                });
                specialties = new ArrayList<>(tagged.size());
                for (SpecialtyWithTags item : tagged) {
                    specialties.add(item.specialty);
                }
            } else {
                specialties = SpecialtyCatalog.load(this, degreeLevel);
            }
        } catch (IOException e) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        specialtyRows.clear();
        specialtyCheckBoxes.clear();
        container.removeAllViews();
        Set<String> saved = new HashSet<>(UserSelectionStore.of(this).get(SelectionPool.SPECIALTIES));
        int restoredCount = 0;

        for (Specialty specialty : specialties) {
            View rowView = inflater.inflate(R.layout.item_specialty_row, container, false);
            CheckBox checkBox = rowView.findViewById(R.id.specialty_checkbox);
            if (checkBox == null) {
                continue;
            }

            checkBox.setText(specialty.getLabel());
            CompoundButtonCompat.setButtonTintList(checkBox, null);
            boolean restore = SelectionLimitHelper.shouldRestoreAsChecked(
                    saved.contains(specialty.getLabel()),
                    restoredCount,
                    MAX_SPECIALTIES
            );
            checkBox.setChecked(restore);
            if (restore) {
                restoredCount++;
            }

            container.addView(rowView);
            specialtyRows.add(new SpecialtyRow(specialty, checkBox, rowView));
            specialtyCheckBoxes.add(checkBox);
        }

        for (CheckBox checkBox : specialtyCheckBoxes) {
            SelectionLimitHelper.bindMaxSelection(
                    checkBox,
                    specialtyCheckBoxes,
                    MAX_SPECIALTIES,
                    MSG_MAX_SPECIALTIES
            );
        }

        EditText searchField = findViewById(R.id.specialty_search);
        if (searchField != null) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applySpecialtyFilter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void applySpecialtyFilter(@NonNull String query) {
        String normalized = query.trim().toLowerCase(Locale.getDefault());
        for (SpecialtyRow row : specialtyRows) {
            boolean visible = normalized.isEmpty()
                    || row.specialty.getLabel().toLowerCase(Locale.getDefault()).contains(normalized);
            row.rowView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private static int bestTagRank(@NonNull List<String> tags, @NonNull Map<String, Integer> directionRank) {
        int best = Integer.MAX_VALUE;
        for (String tag : tags) {
            Integer rank = directionRank.get(tag);
            if (rank != null && rank < best) {
                best = rank;
            }
        }
        return best;
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
        return "Отметь от 1 до 5 специальностей из списка.\n" +
                "Используй поиск - 🔍, чтобы быстро найти нужную специальность.\n" +
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
        final View rowView;

        SpecialtyRow(Specialty specialty, CheckBox checkBox, View rowView) {
            this.specialty = specialty;
            this.checkBox = checkBox;
            this.rowView = rowView;
        }
    }
}
