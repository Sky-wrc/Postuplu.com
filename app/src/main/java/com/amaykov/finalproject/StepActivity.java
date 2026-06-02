package com.amaykov.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.List;

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
                onBeforeSaveSelections();
                UserSelectionStore.of(this).put(pool, collectSelectedValues());
            }
            Intent intent = createNextIntent();
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

    @Nullable
    protected SelectionPool getSelectionPool() {
        return null;
    }

    @NonNull
    protected List<String> collectSelectedValues() {
        return Collections.emptyList();
    }

    protected void onBeforeSaveSelections() {
    }

    protected void onStepReady(Bundle savedInstanceState) {
    }

    @NonNull
    protected Intent createNextIntent() {
        Intent intent = new Intent(this, getNextStepClass());
        if (MainActivity.class.equals(getNextStepClass())) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }

    public void resetCurrentSelections() {
        View root = getWindow() != null ? getWindow().getDecorView() : null;
        if (root != null) {
            resetInViewTree(root);
        }
    }

    private static void resetInViewTree(@NonNull View view) {
        if (view instanceof CheckBox) {
            ((CheckBox) view).setChecked(false);
            return;
        }
        if (view instanceof RadioGroup) {
            ((RadioGroup) view).clearCheck();
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child != null) {
                    resetInViewTree(child);
                }
            }
        }
    }
}
