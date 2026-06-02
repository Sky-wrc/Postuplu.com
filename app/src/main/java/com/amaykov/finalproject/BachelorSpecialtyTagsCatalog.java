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

public final class BachelorSpecialtyTagsCatalog {

    private static final String ASSET_FILE = "Bachelors_Specialities+tags";

    private BachelorSpecialtyTagsCatalog() {
    }

    @NonNull
    public static List<SpecialtyWithTags> load(@NonNull Context context) throws IOException {
        AssetManager assets = context.getAssets();
        try (InputStream in = assets.open(ASSET_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            List<SpecialtyWithTags> result = new ArrayList<>();
            int sourceIndex = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                int firstHash = trimmed.indexOf('#');
                if (firstHash < 0) {
                    result.add(new SpecialtyWithTags(new Specialty(trimmed), Collections.emptyList(), sourceIndex));
                    sourceIndex++;
                    continue;
                }

                String label = trimmed.substring(0, firstHash).trim();
                if (label.isEmpty()) {
                    continue;
                }
                List<String> tags = extractTags(trimmed.substring(firstHash));
                result.add(new SpecialtyWithTags(new Specialty(label), tags, sourceIndex));
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
}

final class SpecialtyWithTags {
    final Specialty specialty;
    final List<String> tags;
    final int sourceIndex;

    SpecialtyWithTags(@NonNull Specialty specialty, @NonNull List<String> tags, int sourceIndex) {
        this.specialty = specialty;
        this.tags = tags;
        this.sourceIndex = sourceIndex;
    }
}

