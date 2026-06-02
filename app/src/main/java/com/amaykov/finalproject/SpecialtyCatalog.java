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

public final class SpecialtyCatalog {

    private SpecialtyCatalog() {
    }

    @NonNull
    public static List<Specialty> load(@NonNull Context context, @NonNull DegreeLevel degreeLevel)
            throws IOException {
        AssetManager assets = context.getAssets();
        String assetFile = degreeLevel.getSpecialtiesAssetFile();
        try (InputStream in = assets.open(assetFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<Specialty> specialties = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String label = line.trim();
                if (!label.isEmpty()) {
                    specialties.add(new Specialty(label));
                }
            }
            return Collections.unmodifiableList(specialties);
        }
    }
}
