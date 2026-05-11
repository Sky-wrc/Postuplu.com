package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Общий каркас шага: заголовок, «Далее», кнопка помощи.
 */
public abstract class StepActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getStepLayoutResId());

        TextView title = findViewById(R.id.textView2);
        title.setText(getStepTitle());

        Button next = findViewById(R.id.button);
        next.setText("Далее");
        next.setOnClickListener(v -> {
            onBeforeNavigateNext();
            if (!canNavigateNext()) {
                return;
            }
            Intent intent = new Intent(StepActivity.this, getNextStepClass());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
        });

        Button help = findViewById(R.id.button3);
        HelpMenu.bind(this, help);

        onStepReady(savedInstanceState);
    }

    @LayoutRes
    protected abstract int getStepLayoutResId();

    @NonNull
    protected abstract String getStepTitle();

    @NonNull
    protected abstract Class<?> getNextStepClass();

    /**
     * Флаг-валидация для кнопки «Далее». Если вернёт false — переход не произойдёт.
     */
    protected boolean canNavigateNext() {
        return true;
    }

    /**
     * Вызывается перед переходом на следующий экран (например, показ сводки выбора).
     */
    protected void onBeforeNavigateNext() {
    }

    /**
     * Хук после привязки общих элементов — для чекбоксов и т.п.
     */
    protected void onStepReady(Bundle savedInstanceState) {
    }
}
