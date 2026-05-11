package com.amaykov.finalproject;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public final class HelpMenu {

    private HelpMenu() {
    }

    public interface HelpMessageProvider {
        CharSequence getMessage();
    }

    public static void bind(AppCompatActivity activity, View anchor) {
        bind(activity, anchor, () -> "Здесь может быть подсказка для этого экрана.");
    }

    public static void bind(AppCompatActivity activity, View anchor, HelpMessageProvider messageProvider) {
        anchor.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(activity, anchor);
            menu.getMenuInflater().inflate(R.menu.pomogalka_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.inf_itm) {
                    CharSequence msg = messageProvider != null ? messageProvider.getMessage() : null;
                    if (msg == null || msg.length() == 0) {
                        msg = "Здесь может быть подсказка для этого экрана.";
                    }
                    AlertDialog helpDialog = new AlertDialog.Builder(activity)
                            .setTitle("Подсказка")
                            .setMessage(msg)
                            .setPositiveButton("закрыть", (dialog, which) -> dialog.dismiss())
                            .create();
                    helpDialog.show();
                    Button close = helpDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (close != null) {
                        close.setTextColor(Color.WHITE);
                    }

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
