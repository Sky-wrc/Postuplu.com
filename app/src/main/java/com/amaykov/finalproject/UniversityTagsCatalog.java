package com.amaykov.finalproject;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class UniversityTagsCatalog {

    private static final String ASSET_FILE = "Universities+tags";
    private static final char EM_DASH = '\u2014';

    private UniversityTagsCatalog() {
    }

    @NonNull
    public static List<UniversityWithTags> load(@NonNull Context context) throws IOException {
        AssetManager assets = context.getAssets();
        try (InputStream in = assets.open(ASSET_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            List<UniversityWithTags> result = new ArrayList<>();
            int sourceIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                int firstHash = trimmed.indexOf('#');
                if (firstHash < 0) {
                    continue;
                }

                int dashIndex = trimmed.indexOf(EM_DASH);
                if (dashIndex < 0) {
                    continue;
                }

                String preTags = trimmed.substring(0, firstHash).trim();
                List<String> tags = extractTags(trimmed.substring(firstHash));
                if (preTags.isEmpty() || tags.isEmpty()) {
                    continue;
                }

                String abbreviation = preTags.substring(0, dashIndex).trim();
                String rest = preTags.substring(dashIndex + 1).trim();
                if (abbreviation.isEmpty() || rest.isEmpty()) {
                    continue;
                }

                NameAndDescription parsed = splitNameAndDescription(rest);
                result.add(new UniversityWithTags(
                        new University(abbreviation, parsed.fullName, parsed.description, preTags),
                        tags,
                        sourceIndex
                ));
                sourceIndex++;
            }
            return Collections.unmodifiableList(result);
        }
    }

    @NonNull
    private static List<String> extractTags(@NonNull String tagsChunk) {
        List<String> tags = new ArrayList<>();
        int i = 0;
        while (i < tagsChunk.length()) {
            int hashIndex = tagsChunk.indexOf('#', i);
            if (hashIndex < 0) {
                break;
            }
            int nextHashIndex = tagsChunk.indexOf('#', hashIndex + 1);
            String tag;
            if (nextHashIndex < 0) {
                tag = tagsChunk.substring(hashIndex + 1).trim();
                i = tagsChunk.length();
            } else {
                tag = tagsChunk.substring(hashIndex + 1, nextHashIndex).trim();
                i = nextHashIndex;
            }
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    @NonNull
    private static NameAndDescription splitNameAndDescription(@NonNull String rest) {
        int boundary = findNameDescriptionBoundary(rest);
        if (boundary >= 0) {
            String fullName = rest.substring(0, boundary).trim();
            String description = rest.substring(boundary + 2).trim();
            return new NameAndDescription(fullName, description);
        }
        return new NameAndDescription(rest, "");
    }

    private static int findNameDescriptionBoundary(@NonNull String text) {
        int parenDot = text.indexOf("). ");
        if (parenDot >= 0) {
            return parenDot + 1;
        }
        for (int i = 0; i < text.length() - 2; i++) {
            if (text.charAt(i) != '.' || text.charAt(i + 1) != ' ') {
                continue;
            }
            if (!isDescriptionStartChar(text.charAt(i + 2))) {
                continue;
            }
            if (i >= 2 && isNameEndingChar(text.charAt(i - 1)) && isNameEndingChar(text.charAt(i - 2))) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isDescriptionStartChar(char c) {
        return c == '«' || c == 'Ё' || (c >= 'А' && c <= 'Я');
    }

    private static boolean isNameEndingChar(char c) {
        return c == 'ё' || c == '»' || c == ')' || (c >= 'а' && c <= 'я')
                || (c >= '0' && c <= '9');
    }

    private static final class NameAndDescription {
        final String fullName;
        final String description;

        NameAndDescription(String fullName, String description) {
            this.fullName = fullName;
            this.description = description;
        }
    }
}

final class UniversityWithTags {
    final University university;
    final List<String> tags;
    final int sourceIndex;

    UniversityWithTags(@NonNull University university, @NonNull List<String> tags, int sourceIndex) {
        this.university = university;
        this.tags = tags;
        this.sourceIndex = sourceIndex;
    }
}

