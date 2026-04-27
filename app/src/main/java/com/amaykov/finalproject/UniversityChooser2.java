package com.amaykov.finalproject;

import static java.lang.System.exit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.widget.CheckBox;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;




public class UniversityChooser2 extends AppCompatActivity {

    TextView mainText;
    Button ok;
    Button info; //кнопка справа сверху

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_university_chooser2);

        mainText = findViewById(R.id.textView2);
        mainText.setText("Выбери какие напрвления тебя интересуют");

        ok = findViewById(R.id.button);
        ok.setText("Далее");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UniversityChooser2.this, AdmissionWays3.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
                //finish();
            }
        });

        info = findViewById(R.id.button3);
        info.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu pomogalka = new PopupMenu(UniversityChooser2.this, info);
            pomogalka.getMenuInflater().inflate(R.menu.pomogalka_menu, pomogalka.getMenu());

            pomogalka.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.inf_itm) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UniversityChooser2.this);
                    builder.setTitle("Большая подсказка")
                            .setMessage("Здесь может быть очень длинный текст...\n\n" +
                                    "Вы можете добавить сюда инструкции, правила " +
                                    "или описание функций вашего приложения.")
                            .setPositiveButton("назад", (dialog, id) -> {

                                dialog.dismiss();
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (item.getItemId() == R.id.back_itm) {
                    finish();
                } else if (item.getItemId() == R.id.exit_itm) {
                    finishAffinity();
                }
                return false;
            });
            pomogalka.show();
            }
        });
    }
}

