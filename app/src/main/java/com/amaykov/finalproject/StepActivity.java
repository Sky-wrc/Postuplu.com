package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

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
            if (!canNavigateNext()) {
                return;
            }
            SelectionPool pool = getSelectionPool();
            if (pool != null) {
                UserSelectionStore.of(this).put(pool, collectSelectedValues());
            }
            Intent intent = new Intent(StepActivity.this, getNextStepClass());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_right);
        });

        Button help = findViewById(R.id.button3);
        HelpMenu.bind(this, help, this::getHelpMessage);

        onStepReady(savedInstanceState);
    }

    @LayoutRes
    protected abstract int getStepLayoutResId();

    @NonNull
    protected abstract String getStepTitle();

    @NonNull
    protected abstract Class<?> getNextStepClass();

    
    @NonNull
    protected CharSequence getHelpMessage() {
        return "";
    }

    
    protected boolean canNavigateNext() {
        return true;
    }

    /**
     * Пул для сохранения при «Далее»; {@code null} — шаг ничего не пишет в store.
     */
    @Nullable
    protected SelectionPool getSelectionPool() {
        return null;
    }

    /** Только отмеченные чекбоксы текущего шага (вызывается после успешной {@link #canNavigateNext()}). */
    @NonNull
    protected List<String> collectSelectedValues() {
        return Collections.emptyList();
    }

    protected void onStepReady(Bundle savedInstanceState) {
    }
}
