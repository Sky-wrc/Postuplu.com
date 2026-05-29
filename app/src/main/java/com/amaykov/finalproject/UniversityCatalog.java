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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Загружает список вузов из {@code assets/Universities.txt} (копия project_data/Universities.txt).
 */
public final class UniversityCatalog {

    private static final String ASSET_FILE = "Universities.txt";
    private static final char EM_DASH = '\u2014';
    private static final Pattern PAREN_ABBREV = Pattern.compile(
            "^(.+?)\\s*\\(([^)]+)\\)\\.\\s*(.*)$",
            Pattern.DOTALL
    );

    private UniversityCatalog() {
    }

    @NonNull
    public static List<University> load(@NonNull Context context) throws IOException {
        AssetManager assets = context.getAssets();
        try (InputStream in = assets.open(ASSET_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> paragraphs = new ArrayList<>();
            StringBuilder block = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (block.length() > 0) {
                        paragraphs.add(block.toString().trim());
                        block.setLength(0);
                    }
                } else {
                    if (block.length() > 0) {
                        block.append(' ');
                    }
                    block.append(line.trim());
                }
            }
            if (block.length() > 0) {
                paragraphs.add(block.toString().trim());
            }

            List<University> universities = new ArrayList<>();
            for (String paragraph : paragraphs) {
                University university = parseEntry(paragraph);
                if (university != null) {
                    universities.add(university);
                }
            }
            return Collections.unmodifiableList(universities);
        }
    }

    private static University parseEntry(@NonNull String line)
    {
        int dashIndex = line.indexOf(EM_DASH);
        if (dashIndex < 0) {
            dashIndex = line.indexOf(" - ");
        }
        if (dashIndex >= 0)
        {
            String abbreviation = line.substring(0, dashIndex).trim();
            int restStart = dashIndex + 1;
            if (dashIndex + 2 < line.length()
                    && line.charAt(dashIndex) == ' '
                    && line.charAt(dashIndex + 1) == '-'
                    && line.charAt(dashIndex + 2) == ' ') {
                restStart = dashIndex + 3;
            }
            String rest = line.substring(restStart).trim();
            NameAndDescription parsed = splitNameAndDescription(rest);
            return new University(abbreviation, parsed.fullName, parsed.description, line);
        }

        Matcher matcher = PAREN_ABBREV.matcher(line);
        if (matcher.matches()) {
            String fullName = matcher.group(1).trim();
            String abbreviation = matcher.group(2).trim();
            String description = matcher.group(3).trim();
            return new University(abbreviation, fullName, description, line);
        }

        NameAndDescription parsed = splitNameAndDescription(line);
        return new University(parsed.fullName, parsed.fullName, parsed.description, line);
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
