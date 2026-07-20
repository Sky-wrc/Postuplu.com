package com.amaykov.finalproject;

import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    public interface RetryClickListener {
        void onRetryClicked();
    }

    private final List<ChatMessage> messages = new ArrayList<>();
    @Nullable
    private RetryClickListener retryClickListener;

    public void setRetryClickListener(@Nullable RetryClickListener listener) {
        retryClickListener = listener;
    }

    public void setMessages(@NonNull List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(@NonNull ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateLastMessage(@NonNull ChatMessage message) {
        if (messages.isEmpty()) {
            return;
        }
        int index = messages.size() - 1;
        messages.set(index, message);
        notifyItemChanged(index);
    }

    public void updateLastMessage(@NonNull String text) {
        if (messages.isEmpty()) {
            return;
        }
        int index = messages.size() - 1;
        ChatMessage last = messages.get(index);
        messages.set(index, last.withText(text).withRetry(false));
        notifyItemChanged(index);
    }

    @Nullable
    public ChatMessage getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    @NonNull
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).role == ChatMessage.Role.USER ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
        return new AiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).bind(message, retryClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static void configureMessageTextOnce(@NonNull TextView messageText) {
        messageText.setLinkTextColor(
                ContextCompat.getColor(messageText.getContext(), R.color.link_blue));
        messageText.setMovementMethod(ScrollFriendlyLinkMovementMethod.getInstance());
        messageText.setTextIsSelectable(true);
        messageText.setFocusable(true);
        messageText.setLongClickable(true);
        messageText.setLinksClickable(true);
    }

    private static void bindMessageText(@NonNull TextView messageText, @NonNull String text) {
        messageText.setText(text);
        Linkify.addLinks(messageText, Linkify.WEB_URLS);
    }

    static final class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            configureMessageTextOnce(messageText);
        }

        void bind(@NonNull ChatMessage message) {
            bindMessageText(messageText, message.text);
        }
    }

    static final class AiViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView tokenUsageText;
        private final Button retryButton;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            tokenUsageText = itemView.findViewById(R.id.token_usage_text);
            retryButton = itemView.findViewById(R.id.retry_button);
            configureMessageTextOnce(messageText);
        }

        void bind(@NonNull ChatMessage message, @Nullable RetryClickListener listener) {
            bindMessageText(messageText, message.text);
            if (message.promptTokenCount != null || message.candidatesTokenCount != null) {
                int prompt = message.promptTokenCount != null ? message.promptTokenCount : 0;
                int candidates = message.candidatesTokenCount != null ? message.candidatesTokenCount : 0;
                tokenUsageText.setVisibility(View.VISIBLE);
                tokenUsageText.setText(
                        "Токены: promptTokenCount=" + prompt
                                + ", candidatesTokenCount=" + candidates);
            } else {
                tokenUsageText.setVisibility(View.GONE);
            }
            if (message.showRetry) {
                retryButton.setVisibility(View.VISIBLE);
                retryButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRetryClicked();
                    }
                });
            } else {
                retryButton.setVisibility(View.GONE);
                retryButton.setOnClickListener(null);
            }
        }
    }
}
