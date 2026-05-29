package com.amaykov.finalproject;

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

public class OrientationChooser1 extends StepActivity {

    private static final String MSG_LOAD_ERROR = "Не удалось загрузить список направлений";

    private final List<DirectionRow> directionRows = new ArrayList<>();

    @Override
    protected int getStepLayoutResId() {
        return R.layout.activity_orientation_chooser1;
    }

    @Override
    protected void onStepReady(Bundle savedInstanceState) {
        LinearLayout container = findViewById(R.id.direction_list_container);
        if (container == null) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        List<Direction> directions;
        try {
            directions = DirectionCatalog.load(this);
        } catch (IOException e) {
            Toast.makeText(this, MSG_LOAD_ERROR, Toast.LENGTH_LONG).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        directionRows.clear();
        container.removeAllViews();
        Set<String> saved = new HashSet<>(UserSelectionStore.of(this).get(SelectionPool.STUDY_DIRECTIONS));

        for (Direction direction : directions) {
            View rowView = inflater.inflate(R.layout.item_direction_row, container, false);
            CheckBox checkBox = rowView.findViewById(R.id.direction_checkbox);
            if (checkBox == null) {
                continue;
            }

            checkBox.setText(direction.getLabel());
            CompoundButtonCompat.setButtonTintList(checkBox, null);
            checkBox.setChecked(saved.contains(direction.getLabel()));

            container.addView(rowView);
            directionRows.add(new DirectionRow(direction, checkBox));
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
        for (DirectionRow row : directionRows) {
            if (row.checkBox.isChecked()) {
                selected.add(row.direction.getLabel());
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
        for (DirectionRow row : directionRows) {
            if (row.checkBox.isChecked()) {
                return true;
            }
        }
        Toast.makeText(this, "Ничего не выбрано — отметь хотя бы одно направление.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private static final class DirectionRow {
        final Direction direction;
        final CheckBox checkBox;

        DirectionRow(Direction direction, CheckBox checkBox) {
            this.direction = direction;
            this.checkBox = checkBox;
        }
    }
}
