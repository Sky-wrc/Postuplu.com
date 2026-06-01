package com.amaykov.finalproject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.CompoundButtonCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniversityChooser2 extends StepActivity {

    private static final int MAX_UNIVERSITIES = 5;
    private static final String MSG_LOAD_ERROR = "Не удалось загрузить список вузов";
    private static final String MSG_MAX_UNIVERSITIES = "Можно выбрать не больше 5 вузов.";
    private static final String INFO_DIALOG_TITLE = "О вузе";
    private static final String INFO_BUTTON_DESCRIPTION = "Подробнее о вузе";

    private final List<UniversityRow> universityRows = new ArrayList<>();
    private final List<CheckBox> universityCheckBoxes = new ArrayList<>();

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_university_chooser2;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        LinearLayout container = findViewById(R.id.university_list_container);
//        if (container == null) {
//            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
//            return;
//        }

        List<University> universities;
        try {
            universities = UniversityCatalog.load(this);
        } catch (IOException e) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        universityRows.clear();
        universityCheckBoxes.clear();
        container.removeAllViews();
        Set<String> saved = new HashSet<>(UserSelectionStore.of(this).get(SelectionPool.UNIVERSITIES));
        int restoredCount = 0;

        for (University university : universities) {
            View rowView = inflater.inflate(R.layout.item_university_row, container, false);
            CheckBox checkBox = rowView.findViewById(R.id.university_checkbox);
            Button infoButton = rowView.findViewById(R.id.university_info_button);
            if (checkBox == null || infoButton == null) {
                continue;
            }

            checkBox.setText(university.getAbbreviation());
            CompoundButtonCompat.setButtonTintList(checkBox, null);
            boolean restore = SelectionLimitHelper.shouldRestoreAsChecked(
                    saved.contains(university.getAbbreviation()),
                    restoredCount,
                    MAX_UNIVERSITIES
            );
            checkBox.setChecked(restore);
            if (restore) {
                restoredCount++;
            }

            infoButton.setContentDescription(INFO_BUTTON_DESCRIPTION);
            infoButton.setOnClickListener(v -> showUniversityInfo(university));

            container.addView(rowView);
            universityRows.add(new UniversityRow(university, checkBox));
            universityCheckBoxes.add(checkBox);
        }

        for (CheckBox checkBox : universityCheckBoxes) {
            SelectionLimitHelper.bindMaxSelection(
                    checkBox,
                    universityCheckBoxes,
                    MAX_UNIVERSITIES,
                    MSG_MAX_UNIVERSITIES
            );
        }
    }

    private void showUniversityInfo(@NonNull University university) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(INFO_DIALOG_TITLE)
                .setMessage(university.getInfoMessage())
                .setPositiveButton("закрыть", (d, which) -> d.dismiss())
                .create();
        dialog.show();
        Button close = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (close != null) {
            close.setTextColor(Color.WHITE);
        }
    }

    @NonNull
    @Override
    protected String getStepTitle()
    {
        return "Выбери вузы, которые тебя интересуют";
    }

    @NonNull
    @Override
    protected CharSequence getHelpMessage()
    {
        return "Отметь от 1 до 5 вузов, которые рассматриваешь.\n" +
                "Кнопка «⁝» рядом с аббревиатурой покажет полное название и описание.\n" +
                "После выбора нажми «Далее».";
    }

    @NonNull
    @Override
    protected Class<?> getNextStepClass()
    {
        return DegreeChooser3.class;
    }

    @Nullable
    @Override
    protected SelectionPool getSelectionPool() {
        return SelectionPool.UNIVERSITIES;
    }

    @NonNull
    @Override
    protected List<String> collectSelectedValues() {
        List<String> selected = new ArrayList<>();
        for (UniversityRow row : universityRows) {
            if (row.checkBox.isChecked()) {
                selected.add(row.university.getAbbreviation());
            }
        }
        return selected;
    }

    @Override
    protected boolean canNavigateNext()
    {
        for (UniversityRow row : universityRows)
        {
            if (row.checkBox.isChecked())
            {
                return true;
            }
        }

        Toast.makeText(this, "Ничего не выбрано - отметь хотя бы один вуз.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private static final class UniversityRow {
        final University university;
        final CheckBox checkBox;

        UniversityRow(University university, CheckBox checkBox) {
            this.university = university;
            this.checkBox = checkBox;
        }
    }
}
