package com.amaykov.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ChatMessage {

    public enum Role {
        USER,
        AI
    }

    @NonNull
    public final Role role;
    @NonNull
    public final String text;
    public final boolean showRetry;
    @Nullable
    public final Integer promptTokenCount;
    @Nullable
    public final Integer candidatesTokenCount;

    public ChatMessage(@NonNull Role role, @NonNull String text) {
        this(role, text, false, null, null);
    }

    public ChatMessage(
            @NonNull Role role,
            @NonNull String text,
            boolean showRetry,
            @Nullable Integer promptTokenCount,
            @Nullable Integer candidatesTokenCount) {
        this.role = role;
        this.text = text;
        this.showRetry = showRetry;
        this.promptTokenCount = promptTokenCount;
        this.candidatesTokenCount = candidatesTokenCount;
    }

    @NonNull
    public ChatMessage withText(@NonNull String newText) {
        return new ChatMessage(role, newText, showRetry, promptTokenCount, candidatesTokenCount);
    }

    @NonNull
    public ChatMessage withRetry(boolean retry) {
        return new ChatMessage(role, text, retry, promptTokenCount, candidatesTokenCount);
    }

    @NonNull
    public ChatMessage withUsage(@Nullable Integer promptTokens, @Nullable Integer candidateTokens) {
        return new ChatMessage(role, text, showRetry, promptTokens, candidateTokens);
    }
}
