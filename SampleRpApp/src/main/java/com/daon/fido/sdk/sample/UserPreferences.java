package com.daon.fido.sdk.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.util.Map;

public class UserPreferences {

    private static UserPreferences instance;

    private final SharedPreferences preferences;

    public static UserPreferences instance() {
        if (instance == null)
            throw new IllegalStateException("initialize() not called!");
        return instance;
    }

    private UserPreferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void initialize(Context context) {
        initialize(context, 0);
    }

    public static void initialize(Context context, int xmlid) {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (xmlid > 0)
                PreferenceManager.setDefaultValues(context, xmlid, false);

            if (instance == null)
                instance = new UserPreferences(context);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public int getInteger(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void putInteger(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }
}
