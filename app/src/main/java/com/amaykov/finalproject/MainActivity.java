package com.amaykov.finalproject;

import static java.lang.System.exit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("PREFS_NAME", MODE_PRIVATE);
        boolean isFirstRun = settings.getBoolean("isFirstRun", true);

        if (isFirstRun == false)
        {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //setContentView(R.layout.activity_welcome);
            // Меняем флаг, чтобы при следующем запуске это не повторялось
            settings.edit().putBoolean("isFirstRun", false).apply();

            // Завершаем текущую активность, если не хотим возвращаться к ней
            finish();
        }
        exit(0);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
        //});
    }
}