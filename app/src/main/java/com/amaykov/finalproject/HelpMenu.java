package com.amaykov.finalproject;

import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Общее меню «помощь / назад / выход» для шаговых экранов.
 */
public final class HelpMenu {

    private HelpMenu() {
    }

    public static void bind(AppCompatActivity activity, View anchor) {
        anchor.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(activity, anchor);
            menu.getMenuInflater().inflate(R.menu.pomogalka_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.inf_itm) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Это очень полезная подсказка")
                            .setMessage("Здесь может быть очень длинный текст…\n\nВы можете добавить сюда инструкции, правила или описание функций вашего приложения.")
                            .setPositiveButton("назад", (dialog, which) -> dialog.dismiss())
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
