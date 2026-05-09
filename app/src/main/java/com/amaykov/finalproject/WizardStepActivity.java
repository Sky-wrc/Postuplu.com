package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Общий каркас шага визарда: заголовок, «Далее», кнопка помощи.
 */
public abstract class WizardStepActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getWizardLayoutResId());

        TextView title = findViewById(R.id.textView2);
        title.setText(getStepTitle());

        Button next = findViewById(R.id.button);
        next.setText(R.string.wizard_next);
        next.setOnClickListener(v -> {
            onBeforeNavigateNext();
            Intent intent = new Intent(WizardStepActivity.this, getNextStepClass());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
        });

        Button help = findViewById(R.id.button3);
        WizardHelpMenu.bind(this, help);

        onWizardStepReady(savedInstanceState);
    }

    @LayoutRes
    protected abstract int getWizardLayoutResId();

    @NonNull
    protected abstract String getStepTitle();

    @NonNull
    protected abstract Class<?> getNextStepClass();

    /**
     * Вызывается перед переходом на следующий экран (например, показ сводки выбора).
     */
    protected void onBeforeNavigateNext() {
    }

    /**
     * Хук после привязки общих элементов — для чекбоксов и т.п.
     */
    protected void onWizardStepReady(Bundle savedInstanceState) {
    }
}
