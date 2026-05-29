package com.amaykov.finalproject;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Словарь «пул → только выбранные значения», сохраняется в {@link SharedPreferences}.
 */
public final class UserSelectionStore {

    private static final String PREFS_NAME = "user_selections";
    private static final String KEY_POOLS_JSON = "pools_json";

    private final SharedPreferences prefs;

    private UserSelectionStore(@NonNull Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    public static UserSelectionStore of(@NonNull Context context) {
        return new UserSelectionStore(context);
    }

    public void put(@NonNull SelectionPool pool, @NonNull List<String> selectedOnly) {
        JSONObject root = loadRootJson();
        try {
            JSONArray array = new JSONArray();
            for (String value : selectedOnly) {
                array.put(value);
            }
            root.put(pool.key, array);
            prefs.edit().putString(KEY_POOLS_JSON, root.toString()).apply();
        } catch (JSONException ignored) {
            // оставляем предыдущее содержимое при ошибке сериализации
        }
    }

    @NonNull
    public List<String> get(@NonNull SelectionPool pool) {
        JSONObject root = loadRootJson();
        if (!root.has(pool.key)) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = root.getJSONArray(pool.key);
            List<String> result = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                result.add(array.getString(i));
            }
            return result;
        } catch (JSONException e) {
            return Collections.emptyList();
        }
    }

    @NonNull
    public Map<String, List<String>> snapshot() {
        JSONObject root = loadRootJson();
        Map<String, List<String>> map = new LinkedHashMap<>();
        Iterator<String> keys = root.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONArray array = root.getJSONArray(key);
                List<String> values = new ArrayList<>(array.length());
                for (int i = 0; i < array.length(); i++) {
                    values.add(array.getString(i));
                }
                map.put(key, values);
            } catch (JSONException ignored) {
                // пропускаем повреждённый пул
            }
        }
        return map;
    }

    public void clear() {
        prefs.edit().remove(KEY_POOLS_JSON).apply();
    }

    @NonNull
    private JSONObject loadRootJson() {
        String json = prefs.getString(KEY_POOLS_JSON, null);
        if (json == null || json.isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
