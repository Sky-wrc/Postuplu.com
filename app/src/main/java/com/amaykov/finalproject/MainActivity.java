package com.amaykov.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PREFS_NAME";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstRun = settings.getBoolean(KEY_FIRST_RUN, true);
        boolean hasAnySavedSelection = !UserSelectionStore.of(this).snapshot().isEmpty();
        if (isFirstRun || !hasAnySavedSelection) {
            settings.edit().putBoolean(KEY_FIRST_RUN, false).apply();
            startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        View menuButton = findViewById(R.id.button3);
        if (menuButton != null) {
            bindMainMenu(menuButton);
        }
    }

    private void bindMainMenu(View anchor) {
        anchor.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, anchor);
            menu.getMenuInflater().inflate(R.menu.main_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_change_selection) {
                    startActivity(new Intent(this, OrientationChooser1.class));
                    return true;
                }
                if (id == R.id.menu_exit) {
                    finishAffinity();
                    return true;
                }
                return false;
            });
            menu.show();
        });
    }
}