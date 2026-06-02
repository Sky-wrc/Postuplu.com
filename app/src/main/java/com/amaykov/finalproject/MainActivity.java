package com.amaykov.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {



    private static final String AI_CACHE_PREFS = "ai_response_cache";
    private static final String KEY_SELECTION_FINGERPRINT = "selection_fingerprint";
    private static final String KEY_CACHED_RESPONSE = "cached_response_text";

    TextView mainText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        boolean hasAnySavedSelection = !UserSelectionStore.of(this).snapshot().isEmpty();
        if (!hasAnySavedSelection) {
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

        String selectionFingerprint = UserSelectionStore.of(this).getPoolsJsonSnapshot();
        SharedPreferences aiCache = getSharedPreferences(AI_CACHE_PREFS, MODE_PRIVATE);
        String cachedFingerprint = aiCache.getString(KEY_SELECTION_FINGERPRINT, null);
        String cachedResponse = aiCache.getString(KEY_CACHED_RESPONSE, null);

        if (selectionFingerprint.equals(cachedFingerprint)
                && cachedResponse != null
                && !cachedResponse.isEmpty()) {
            mainText.setText(cachedResponse);
            return;
        }

        mainText.setText("Загрузка ответа...");

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-3.5-flash");
        GenerativeModelFutures model = GenerativeModelFutures.from(ai);

        Content prompt = new Content.Builder()
                .addText(buildNeuralNetworkPrompt())
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Executor executor = ContextCompat.getMainExecutor(this);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                if (resultText == null || resultText.isEmpty()) {
                    mainText.setText("Мозговой центр не дал ответа");
                    return;
                }
                mainText.setText(resultText);
                aiCache.edit()
                        .putString(KEY_SELECTION_FINGERPRINT, selectionFingerprint)
                        .putString(KEY_CACHED_RESPONSE, resultText)
                        .apply();
            }

            @Override
            public void onFailure(Throwable t) {
                mainText.setText("Соединение с мозговым центром не установленно...");
            }
        }, executor);

    }




    @NonNull
    public String buildNeuralNetworkPrompt() {
        UserSelectionStore store = UserSelectionStore.of(this);

        List<String> directions = store.get(SelectionPool.STUDY_DIRECTIONS);
        List<String> universities = store.get(SelectionPool.UNIVERSITIES);
        List<String> specialties = store.get(SelectionPool.SPECIALTIES);

        String degreeKey = store.getFirstValue(SelectionPool.DEGREE);
        DegreeLevel degreeLevel = DegreeLevel.fromStorageKey(degreeKey);
        String degreeLabel = degreeLevel != null ? degreeLevel.getDisplayLabel() : "не указано";

        String admissionKey = store.getFirstValue(SelectionPool.BACHELOR_ADMISSION_WAY);
        AdmissionWay admissionWay = AdmissionWay.fromStorageKey(admissionKey);
        String admissionLabel = admissionWay != null
                ? admissionWay.getDisplayLabel()
                : (degreeLevel == DegreeLevel.MASTER ? "не применимо (магистратура)" : "не указано");

        StringBuilder prompt = new StringBuilder();
        prompt.append("Ты — консультант по поступлению в вузы России. В ответах не используй особо разметку и ")
                .append("какие либо выделения в тексте, по типу ** или ####.")
                .append("Отвечай на русском языке, опираясь на актуальные правила приёма 2026 года. ")
                .append("Учитывай данные абитуриента ниже.\n\n");

        prompt.append("=== Данные абитуриента ===\n\n");
        appendListSection(prompt, "1. Направления обучения", directions);
        appendListSection(prompt, "2. ВУЗы (сокращения)", universities);
        prompt.append("3. Ступень обучения: ").append(degreeLabel).append("\n\n");
        appendListSection(prompt, "4. Специальности", specialties);
        prompt.append("5. Способ поступления: ").append(admissionLabel).append("\n\n");

        prompt.append("=== Задание ===\n\n");
        prompt.append("Дай развёрнутый, структурированный ответ по каждому пункту ниже ")
                .append("с учётом выбранных ВУЗов, специальностей и направлений. ")
                .append("Если для конкретного вуза или специальности данных нет — укажи это явно.\n\n");

        if (degreeLevel == DegreeLevel.MASTER) {
            appendMasterAdmissionQuestions(prompt, universities, specialties);
        } else if (admissionWay == null) {
            prompt.append("Способ поступления на бакалавриат не выбран. ")
                    .append("Попроси уточнить способ поступления и перечисли доступные варианты.\n");
        } else {
            appendBachelorAdmissionQuestions(prompt, admissionWay, universities, specialties);
        }

        return prompt.toString();
    }

    private static void appendListSection(
            @NonNull StringBuilder prompt,
            @NonNull String title,
            @NonNull List<String> items) {
        prompt.append(title).append(":\n");
        if (items.isEmpty()) {
            prompt.append("— не указано\n\n");
            return;
        }
        for (String item : items) {
            prompt.append("• ").append(item).append('\n');
        }
        prompt.append('\n');
    }

    private static void appendMasterAdmissionQuestions(
            @NonNull StringBuilder prompt,
            @NonNull List<String> universities,
            @NonNull List<String> specialties) {
        prompt.append("Абитуриент поступает на магистратуру. Ответь по каждому выбранному ВУЗу и специальности:\n");
        prompt.append("1. Какие вступительные испытания или требования (портфолио, собеседование) действуют в 2026 году.\n");
        prompt.append("2. Минимальные проходные баллы или критерии отбора.\n");
        prompt.append("3. Сроки подачи документов в 2026 году.\n");
        prompt.append("4. Особенности приёма на магистратуру в выбранных направлениях.\n");
        if (universities.isEmpty() || specialties.isEmpty()) {
            prompt.append("\nУчти, что часть ВУЗов или специальностей не указана в профиле.\n");
        }
    }

    private static void appendBachelorAdmissionQuestions(
            @NonNull StringBuilder prompt,
            @NonNull AdmissionWay admissionWay,
            @NonNull List<String> universities,
            @NonNull List<String> specialties) {
        String context = buildUniversitySpecialtyContext(universities, specialties);
        switch (admissionWay) {
            case EGE_BUDGET:
                appendEgeStyleQuestions(prompt, "ЕГЭ (бюджет)", context, false);
                break;
            case EGE_PAID:
                appendEgeStyleQuestions(prompt, "ЕГЭ (платное обучение)", context, false);
                break;
            case TARGET:
                appendEgeStyleQuestions(prompt, "целевой набор", context, false);
                break;
            case BVI:
                appendEgeStyleQuestions(prompt, "БВИ (без вступительных испытаний)", context, true);
                break;
            case VI:
                appendDocumentDeadlineQuestions(prompt, "вступительные испытания (ВИ)");
                break;
            case QUOTA:
                appendDocumentDeadlineQuestions(prompt, "льгота / квота");
                break;
            default:
                break;
        }
    }

    @NonNull
    private static String buildUniversitySpecialtyContext(
            @NonNull List<String> universities,
            @NonNull List<String> specialties) {
        StringBuilder context = new StringBuilder();
        context.append("Выбранные ВУЗы: ");
        context.append(universities.isEmpty() ? "не указаны" : String.join(", ", universities));
        context.append(". Специальности: ");
        context.append(specialties.isEmpty() ? "не указаны" : String.join(", ", specialties));
        context.append('.');
        return context.toString();
    }

    private static void appendEgeStyleQuestions(
            @NonNull StringBuilder prompt,
            @NonNull String admissionLabel,
            @NonNull String context,
            boolean forBvi) {
        prompt.append("Способ поступления: ").append(admissionLabel).append(". ").append(context).append("\n\n");
        prompt.append("Ответь на вопросы:\n");
        prompt.append("1. Какие предметы ЕГЭ рекомендуется сдавать для выбранных специальностей в каждом ВУЗе.\n");
        prompt.append("2. Когда проходит сдача ЕГЭ в 2026 году (основной и резервный периоды).\n");
        prompt.append("3. Сколько баллов ЕГЭ в сумме нужно набрать в каждый выбранный ВУЗ ")
                .append("и на каждую выбранную специальность (бюджет и платное, если отличается).\n");
        prompt.append("4. Сроки подачи документов при поступлении в 2026 году.\n");
        if (forBvi) {
            prompt.append("5. Дополнительные условия и сроки для абитуриентов с правом на БВИ.\n");
        }
    }

    private static void appendDocumentDeadlineQuestions(
            @NonNull StringBuilder prompt,
            @NonNull String admissionLabel) {
        prompt.append("Способ поступления: ").append(admissionLabel).append(".\n\n");
        prompt.append("Ответь на вопрос:\n");
        prompt.append("1. Какие сроки подачи документов в 2026 году для выбранных ВУЗов и специальностей.\n");
    }

    private void clearAllUserData() {
        UserSelectionStore.of(this).clear();
        getSharedPreferences(AI_CACHE_PREFS, MODE_PRIVATE).edit().clear().apply();
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
                if (id == R.id.menu_clear_data) {
                    clearAllUserData();
                    Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
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