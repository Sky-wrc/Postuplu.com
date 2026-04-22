package com.amaykov.finalproject;

import static java.lang.System.exit;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OrientationChooser1 extends AppCompatActivity {

    TextView mainText;
    Button ok;
    Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_chooser1);

        mainText = findViewById(R.id.textView2);
        mainText.setText("Выбери какие напрвления тебя интересуют");

        ok = findViewById(R.id.button);
        ok.setText("Далее");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrientationChooser1.this, UniversityChooser2.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
                finish();
            }
        });

        info = findViewById(R.id.button3);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(0);
//                Intent intent = new Intent(rientationChooser1.this, OrientationChooser1.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
//                finish();
            }
        });
    }
}