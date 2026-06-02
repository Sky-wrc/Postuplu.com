package com.amaykov.finalproject;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public final class SelectionLimitHelper {

    private SelectionLimitHelper() {
    }

    public static void bindMaxSelection(
            @NonNull CheckBox checkBox,
            @NonNull List<CheckBox> allCheckBoxes,
            int maxSelection,
            @NonNull String limitMessage
    ) {
        checkBox.setOnCheckedChangeListener((CompoundButton button, boolean isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (countChecked(allCheckBoxes) > maxSelection) {
                button.setChecked(false);
                Context context = button.getContext();
                Toast.makeText(context, limitMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static int countChecked(@NonNull List<CheckBox> checkBoxes) {
        int count = 0;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                count++;
            }
        }
        return count;
    }

    public static boolean shouldRestoreAsChecked(
            boolean inSavedSelection,
            int alreadyRestoredCount,
            int maxSelection
    ) {
        return inSavedSelection && alreadyRestoredCount < maxSelection;
    }
}
