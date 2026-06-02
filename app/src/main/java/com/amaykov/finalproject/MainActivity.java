package com.amaykov.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PREFS_NAME";
    private static final String KEY_FIRST_RUN = "isFirstRun";

    TextView mainText;

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
        mainText = findViewById(R.id.main_text);
        mainText.setText("android:text=\"Здесь будет основной текст/результаты.\\n\\nПлейсхолдер:\\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Sed non risus. Suspendisse lectus tortor, dignissim sit amet, adipiscing nec, ultricies sed, dolor. Cras elementum ultrices diam. Maecenas ligula massa, varius a, semper congue, euismod non, mi.\\n\\nProin porttitor, orci nec nonummy molestie, enim est eleifend mi, non fermentum diam nisl sit amet erat. Duis semper. Duis arcu massa, scelerisque vitae, consequat in, pretium a, enim.\\n\\nPellentesque congue. Ut in risus volutpat libero pharetra tempor. Cras vestibulum bibendum augue. Praesent egestas leo in pede.\\n\\n(Текст переносится по ширине экрана и прокручивается.)\"");

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