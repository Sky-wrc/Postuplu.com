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

public class OrientationChooser1 extends AppCompatActivity {

    TextView mainText;
    Button ok;
    Button info; //кнопка справа сверху

    private CheckBox chkIT, chkEngineering, chkNaturalScience, chkMedicine,
            chkAgriculture, chkEconomics, chkEducation, chkLaw,
            chkHumanities, chkInternational, chkCreative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation_chooser1);

        mainText = findViewById(R.id.textView2);
        mainText.setText("Выбери какие напрвления тебя интересуют");

        chkIT = findViewById(R.id.chkIT);
        chkEngineering = findViewById(R.id.chkEngineering);
        chkNaturalScience = findViewById(R.id.chkNaturalScience);
        chkMedicine = findViewById(R.id.chkMedicine);
        chkAgriculture = findViewById(R.id.chkAgriculture);
        chkEconomics = findViewById(R.id.chkEconomics);
        chkEducation = findViewById(R.id.chkEducation);
        chkLaw = findViewById(R.id.chkLaw);
        chkHumanities = findViewById(R.id.chkHumanities);
        chkInternational = findViewById(R.id.chkInternational);
        chkCreative = findViewById(R.id.chkCreative);

        ok = findViewById(R.id.button);
        ok.setText("Далее");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrientationChooser1.this, UniversityChooser2.class);
                showSelectedDirections();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
                //finish();
            }
        });

        info = findViewById(R.id.button3);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pomogalka = new PopupMenu(OrientationChooser1.this, info);
                pomogalka.getMenuInflater().inflate(R.menu.pomogalka_menu, pomogalka.getMenu());

                pomogalka.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.inf_itm) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrientationChooser1.this);
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

        private void showSelectedDirections() {
            StringBuilder selected = new StringBuilder("Вы выбрали: ");
            boolean hasSelected = false;

            if (chkIT.isChecked()) {
                selected.append("Информационные технологии (IT) и цифровая экономика ");
                hasSelected = true;
            }
            if (chkEngineering.isChecked()) {
                selected.append("Инженерия и технические науки");
                hasSelected = true;
            }
            if (chkNaturalScience.isChecked()) {
                selected.append("Естественные науки ");
                hasSelected = true;
            }
            if (chkMedicine.isChecked()) {
                selected.append("Медицина и здравоохранение ");
                hasSelected = true;
            }
            if (chkAgriculture.isChecked()) {
                selected.append("Сельское хозяйство ");
                hasSelected = true;
            }
            if (chkEconomics.isChecked()) {
                selected.append("Экономика, менеджмент и управление ");
                hasSelected = true;
            }
            if (chkEducation.isChecked()) {
                selected.append("Педагогика и образование ");
                hasSelected = true;
            }
            if (chkLaw.isChecked()) {
                selected.append("Юриспруденция ");
                hasSelected = true;
            }
            if (chkHumanities.isChecked()) {
                selected.append("Гуманитарные и социальные науки ");
                hasSelected = true;
            }
            if (chkInternational.isChecked()) {
                selected.append("Международные отношения и регионоведение ");
                hasSelected = true;
            }
            if (chkCreative.isChecked()) {
                selected.append("Творческие и культурные направления ");
                hasSelected = true;
            }
            Toast.makeText(getApplicationContext(), selected, Toast.LENGTH_SHORT).show();
        }




}