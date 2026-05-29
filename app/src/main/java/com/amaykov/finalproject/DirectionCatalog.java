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

/**
 * Загружает направления из {@code assets/Directions.txt} (копия project_data/Directions.txt).
 */
public final class DirectionCatalog {

    private static final String ASSET_FILE = "Directions.txt";

    private DirectionCatalog() {
    }

    @NonNull
    public static List<Direction> load(@NonNull Context context) throws IOException {
        AssetManager assets = context.getAssets();
        try (InputStream in = assets.open(ASSET_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<Direction> directions = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String label = line.trim();
                if (!label.isEmpty()) {
                    directions.add(new Direction(label));
                }
            }
            return Collections.unmodifiableList(directions);
        }
    }
}
