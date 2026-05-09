package com.amaykov.finalproject;

import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Общее меню «помощь / назад / выход» для шагов визарда.
 */
public final class WizardHelpMenu {

    private WizardHelpMenu() {
    }

    public static void bind(AppCompatActivity activity, View anchor) {
        anchor.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(activity, anchor);
            menu.getMenuInflater().inflate(R.menu.pomogalka_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.inf_itm) {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.help_dialog_title)
                            .setMessage(R.string.help_dialog_message)
                            .setPositiveButton(R.string.help_dialog_back, (dialog, which) -> dialog.dismiss())
                            .show();
                } else if (id == R.id.back_itm) {
                    activity.finish();
                } else if (id == R.id.exit_itm) {
                    activity.finishAffinity();
                }
                return true;
            });
            menu.show();
        });
    }
}
