package com.informatika.aplikasikeuanganmahasiswabytoriq;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrefManager {
    private static final String PREF_NAME = "MyExpensePrefs";
    private static final String KEY_DATA = "DATA_EXPENSE";

    // DUA KUNCI PENYIMPANAN KATEGORI
    private static final String KEY_CAT_EXPENSE = "CAT_EXPENSE";
    private static final String KEY_CAT_INCOME = "CAT_INCOME";

    private SharedPreferences prefs;
    private Gson gson;

    public PrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // --- TRANSAKSI ---
    public void saveExpenses(List<Expense> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_DATA, json).apply();
    }

    public ArrayList<Expense> getExpenses() {
        String json = prefs.getString(KEY_DATA, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    // --- KATEGORI PENGELUARAN ---
    public void saveExpenseCategories(List<String> categories) {
        String json = gson.toJson(categories);
        prefs.edit().putString(KEY_CAT_EXPENSE, json).apply();
    }

    public ArrayList<String> getExpenseCategories() {
        String json = prefs.getString(KEY_CAT_EXPENSE, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            // Default Pengeluaran
            return new ArrayList<>(Arrays.asList("Makan", "Transport", "Belanja", "Tagihan", "Hiburan", "Kesehatan"));
        }
    }

    // --- KATEGORI PEMASUKAN (BARU) ---
    public void saveIncomeCategories(List<String> categories) {
        String json = gson.toJson(categories);
        prefs.edit().putString(KEY_CAT_INCOME, json).apply();
    }

    public ArrayList<String> getIncomeCategories() {
        String json = prefs.getString(KEY_CAT_INCOME, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            // Default Pemasukan
            return new ArrayList<>(Arrays.asList("Gaji", "Tunjangan", "Bonus", "Investasi", "Hadiah"));
        }
    }
}