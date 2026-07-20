package com.amaykov.finalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;

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
import com.google.firebase.ai.type.UsageMetadata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityAI";
    private static final String AI_CACHE_PREFS = "ai_response_cache";
    private static final String KEY_SELECTION_FINGERPRINT = "selection_fingerprint";
    private static final String KEY_CACHED_CHAT = "cached_chat_json";
    private static final String KEY_CACHED_RESPONSE = "cached_response_text";

    private static final int MAX_AUTO_RETRIES = 2;
    private static final long[] RETRY_BACKOFF_MS = {1000L, 3000L};

    private enum PendingRequestKind {
        INITIAL,
        FOLLOW_UP
    }

    private ChatAdapter chatAdapter;
    private RecyclerView chatList;
    private EditText chatInput;
    private Button chatSend;
    private GenerativeModelFutures model;
    private String selectionFingerprint;
    private SharedPreferences aiCache;
    private boolean waitingForResponse;

    private PendingRequestKind pendingKind = PendingRequestKind.INITIAL;
    @Nullable
    private String pendingFollowUpText;
    private int autoRetryAttempt;
    private boolean useSlimPayload;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    @Nullable
    private Runnable scheduledRetry;

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

        chatList = findViewById(R.id.chat_list);
        chatInput = findViewById(R.id.chat_input);
        chatSend = findViewById(R.id.chat_send);

        chatAdapter = new ChatAdapter();
        chatAdapter.setRetryClickListener(this::onManualRetry);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setSmoothScrollbarEnabled(false);
        chatList.setLayoutManager(layoutManager);
        chatList.setHasFixedSize(false);
        chatList.setItemViewCacheSize(24);
        chatList.setItemAnimator(null);
        chatList.setAdapter(chatAdapter);

        selectionFingerprint = UserSelectionStore.of(this).getPoolsJsonSnapshot();
        aiCache = getSharedPreferences(AI_CACHE_PREFS, MODE_PRIVATE);

        GenerativeModel ai = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-3.5-flash");
        model = GenerativeModelFutures.from(ai);

        chatSend.setOnClickListener(v -> sendUserMessage());
        chatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendUserMessage();
                return true;
            }
            return false;
        });

        List<ChatMessage> cachedChat = loadCachedChat();
        String cachedFingerprint = aiCache.getString(KEY_SELECTION_FINGERPRINT, null);
        if (selectionFingerprint.equals(cachedFingerprint) && !cachedChat.isEmpty()) {
            chatAdapter.setMessages(cachedChat);
            scrollToBottom();
            return;
        }

        startInitialAdvice();
    }

    @Override
    protected void onDestroy() {
        cancelScheduledRetry();
        super.onDestroy();
    }

    private void startInitialAdvice() {
        pendingKind = PendingRequestKind.INITIAL;
        pendingFollowUpText = null;
        autoRetryAttempt = 0;
        useSlimPayload = false;
        setInputEnabled(false);
        chatAdapter.addMessage(new ChatMessage(ChatMessage.Role.AI, "Загрузка ответа..."));
        scrollToBottom();
        executePendingRequest();
    }

    private void sendUserMessage() {
        if (waitingForResponse) {
            return;
        }
        String text = chatInput.getText() != null ? chatInput.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return;
        }

        chatInput.setText("");
        pendingKind = PendingRequestKind.FOLLOW_UP;
        pendingFollowUpText = text;
        autoRetryAttempt = 0;
        useSlimPayload = true;

        chatAdapter.addMessage(new ChatMessage(ChatMessage.Role.USER, text));
        chatAdapter.addMessage(new ChatMessage(ChatMessage.Role.AI, "Думаю..."));
        scrollToBottom();
        setInputEnabled(false);
        executePendingRequest();
    }

    private void onManualRetry() {
        if (waitingForResponse) {
            return;
        }
        cancelScheduledRetry();
        autoRetryAttempt = 0;
        useSlimPayload = true;
        setInputEnabled(false);
        chatAdapter.updateLastMessage(new ChatMessage(
                ChatMessage.Role.AI,
                pendingKind == PendingRequestKind.INITIAL ? "Загрузка ответа..." : "Думаю...",
                false,
                null,
                null
        ));
        scrollToBottom();
        executePendingRequest();
    }

    private void executePendingRequest() {
        Content prompt = buildRequestContent();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Executor executor = ContextCompat.getMainExecutor(this);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                Integer promptTokens = null;
                Integer candidateTokens = null;
                UsageMetadata usage = result.getUsageMetadata();
                if (usage != null) {
                    promptTokens = usage.getPromptTokenCount();
                    candidateTokens = usage.getCandidatesTokenCount();
                    Log.i(TAG, "promptTokenCount=" + promptTokens
                            + ", candidatesTokenCount=" + candidateTokens);
                }

                String resultText = result.getText();
                if (resultText == null || resultText.isEmpty()) {
                    handleRequestFailure(new IllegalStateException("empty_response"), promptTokens, candidateTokens);
                    return;
                }
                chatAdapter.updateLastMessage(new ChatMessage(
                        ChatMessage.Role.AI,
                        resultText,
                        false,
                        promptTokens,
                        candidateTokens
                ));
                saveChatCache();
                setInputEnabled(true);
                scrollToBottom();
            }

            @Override
            public void onFailure(Throwable t) {
                handleRequestFailure(t, null, null);
            }
        }, executor);
    }

    private void handleRequestFailure(
            @NonNull Throwable t,
            @Nullable Integer promptTokens,
            @Nullable Integer candidateTokens) {
        Log.w(TAG, "AI request failed (attempt=" + autoRetryAttempt + ")", t);

        if (autoRetryAttempt < MAX_AUTO_RETRIES) {
            long delayMs = RETRY_BACKOFF_MS[Math.min(autoRetryAttempt, RETRY_BACKOFF_MS.length - 1)];
            autoRetryAttempt++;
            useSlimPayload = true;
            chatAdapter.updateLastMessage(new ChatMessage(
                    ChatMessage.Role.AI,
                    "Сервис недоступен. Повтор через " + (delayMs / 1000) + " с (попытка "
                            + autoRetryAttempt + "/" + MAX_AUTO_RETRIES + ")...",
                    false,
                    promptTokens,
                    candidateTokens
            ));
            scrollToBottom();
            cancelScheduledRetry();
            scheduledRetry = this::executePendingRequest;
            mainHandler.postDelayed(scheduledRetry, delayMs);
            return;
        }

        String errorMessage = mapFailureMessage(t);
        chatAdapter.updateLastMessage(new ChatMessage(
                ChatMessage.Role.AI,
                errorMessage,
                true,
                promptTokens,
                candidateTokens
        ));
        setInputEnabled(true);
        scrollToBottom();
    }

    @NonNull
    private Content buildRequestContent() {
        if (useSlimPayload || pendingKind == PendingRequestKind.FOLLOW_UP) {
            StringBuilder slim = new StringBuilder(buildUserProfileSummary());
            if (pendingKind == PendingRequestKind.FOLLOW_UP && pendingFollowUpText != null) {
                slim.append("\nВопрос пользователя:\n")
                        .append(pendingFollowUpText);
            } else {
                slim.append("\nДай практичные советы по поступлению по этому резюме на 2026 год.");
            }
            slim.append("\nОтветь кратко и по делу на русском, без лишней разметки. ");
            slim.append("Опирайся только на проверенные данные с официальных сайтов, и желательно предоставляй источники в виде ссылок на эти сайты.");
            return new Content.Builder()
                    .setRole("user")
                    .addText(slim.toString())
                    .build();
        }
        return new Content.Builder()
                .setRole("user")
                .addText(buildNeuralNetworkPrompt())
                .build();
    }

    @NonNull
    private String mapFailureMessage(@NonNull Throwable t) {
        String raw = collectErrorText(t).toLowerCase(Locale.ROOT);
        if (raw.contains("high demand")
                || raw.contains("overloaded")
                || raw.contains("unavailable")
                || raw.contains("preempted")
                || raw.contains("too many retries")
                || raw.contains("decode_preempted")) {
            return "Мозговой центр сейчас перегружен. Нажмите «Повторить» чуть позже.";
        }
        if (raw.contains("empty_response")) {
            return "Мозговой центр не дал ответа. Нажмите «Повторить».";
        }
        if (raw.contains("unable to resolve host")
                || raw.contains("failed to connect")
                || raw.contains("timeout")
                || raw.contains("network")
                || raw.contains("unknownhost")
                || raw.contains("socket")) {
            return "Нет соединения с сетью. Проверьте интернет и нажмите «Повторить».";
        }
        return "Не удалось получить ответ от мозгового центра. Нажмите «Повторить».";
    }

    @NonNull
    private static String collectErrorText(@NonNull Throwable t) {
        StringBuilder sb = new StringBuilder();
        Throwable current = t;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(' ');
            }
            current = current.getCause();
        }
        return sb.toString();
    }

    private void cancelScheduledRetry() {
        if (scheduledRetry != null) {
            mainHandler.removeCallbacks(scheduledRetry);
            scheduledRetry = null;
        }
    }

    private void setInputEnabled(boolean enabled) {
        waitingForResponse = !enabled;
        chatInput.setEnabled(enabled);
        chatSend.setEnabled(enabled);
    }

    private void scrollToBottom() {
        int count = chatAdapter.getItemCount();
        if (count > 0) {
            chatList.post(() -> chatList.scrollToPosition(count - 1));
        }
    }

    private void saveChatCache() {
        JSONArray array = new JSONArray();
        for (ChatMessage message : chatAdapter.getMessages()) {
            if (message.showRetry) {
                continue;
            }
            if ("Загрузка ответа...".equals(message.text)
                    || "Думаю...".equals(message.text)
                    || message.text.startsWith("Мозговой центр не отзывается (нет доступа). Повтор")) {
                continue;
            }
            try {
                JSONObject obj = new JSONObject();
                obj.put("role", message.role == ChatMessage.Role.USER ? "user" : "ai");
                obj.put("text", message.text);
                if (message.promptTokenCount != null) {
                    obj.put("promptTokenCount", message.promptTokenCount);
                }
                if (message.candidatesTokenCount != null) {
                    obj.put("candidatesTokenCount", message.candidatesTokenCount);
                }
                array.put(obj);
            } catch (JSONException ignored) {
            }
        }
        aiCache.edit()
                .putString(KEY_SELECTION_FINGERPRINT, selectionFingerprint)
                .putString(KEY_CACHED_CHAT, array.toString())
                .remove(KEY_CACHED_RESPONSE)
                .apply();
    }

    @NonNull
    private List<ChatMessage> loadCachedChat() {
        List<ChatMessage> result = new ArrayList<>();
        String json = aiCache.getString(KEY_CACHED_CHAT, null);
        if (json == null || json.isEmpty()) {
            String legacy = aiCache.getString(KEY_CACHED_RESPONSE, null);
            if (legacy != null && !legacy.isEmpty()) {
                result.add(new ChatMessage(ChatMessage.Role.AI, legacy));
            }
            return result;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String role = obj.optString("role", "ai");
                String text = obj.optString("text", "");
                if (text.isEmpty()) {
                    continue;
                }
                Integer promptTokens = obj.has("promptTokenCount") ? obj.optInt("promptTokenCount") : null;
                Integer candidateTokens = obj.has("candidatesTokenCount")
                        ? obj.optInt("candidatesTokenCount")
                        : null;
                result.add(new ChatMessage(
                        "user".equals(role) ? ChatMessage.Role.USER : ChatMessage.Role.AI,
                        text,
                        false,
                        promptTokens,
                        candidateTokens
                ));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    @NonNull
    private String buildUserProfileSummary() {
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

        StringBuilder summary = new StringBuilder();
        summary.append("Ты — консультант по поступлению в вузы России. ")
                .append("Отвечай на русском без markdown-разметки. ")
                .append("Краткое резюме абитуриента:\n");
        summary.append("Направления: ")
                .append(directions.isEmpty() ? "не указаны" : String.join(", ", directions))
                .append('\n');
        summary.append("ВУЗы: ")
                .append(universities.isEmpty() ? "не указаны" : String.join(", ", universities))
                .append('\n');
        summary.append("Ступень: ").append(degreeLabel).append('\n');
        summary.append("Специальности: ")
                .append(specialties.isEmpty() ? "не указаны" : String.join(", ", specialties))
                .append('\n');
        summary.append("Способ поступления: ").append(admissionLabel);
        return summary.toString();
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
                .append("Опирайся только на проверенные данные с официальных сайтов, и желательно предоставляй источники в виде ссылок на эти сайты. ")
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
