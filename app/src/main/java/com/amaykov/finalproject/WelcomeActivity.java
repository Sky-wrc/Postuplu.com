package com.amaykov.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        SharedPreferences sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE);
//        boolean isFirstRun = sharedPref.getBoolean("is_first_run", true);
//
//        if (isFirstRun) {
//            startActivity(new Intent(MainActivity, WelcomeActivity.class));
//            sharedPref.edit().putBoolean("is_first_run", false).apply();
//            finish();
//        }

//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_welcome);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
}