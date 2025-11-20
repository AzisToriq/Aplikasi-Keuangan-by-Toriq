package com.informatika.aplikasikeuanganmahasiswabytoriq;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private PieChart pieChart;
    private PrefManager prefManager;
    private ArrayList<Expense> allExpenses;

    private TextView tvTotalIncome, tvTotalExpense, tvDateRange;
    private Button btnDateFilter;

    // --- BAGIAN YANG HILANG TADI (SAYA TAMBAHKAN DI SINI) ---
    private TextView tvNetTotal, tvNetStatus;
    private androidx.cardview.widget.CardView cardNetCashflow;
    // --------------------------------------------------------

    private boolean showExpenseChart = true; // Default view

    // Filter Tanggal (Long millis)
    private long startDate = 0;
    private long endDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Init View
        pieChart = findViewById(R.id.pieChart);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDateRange = findViewById(R.id.tvDateRange);
        btnDateFilter = findViewById(R.id.btnDateFilter);

        // --- INI JUGA HILANG TADI (INIT VIEW) ---
        tvNetTotal = findViewById(R.id.tvNetTotal);
        tvNetStatus = findViewById(R.id.tvNetStatus);
        cardNetCashflow = findViewById(R.id.cardNetCashflow);
        // ----------------------------------------

        MaterialButtonToggleGroup toggleChart = findViewById(R.id.toggleChart);
        Button btnBack = findViewById(R.id.btnBack);

        prefManager = new PrefManager(this);
        allExpenses = prefManager.getExpenses();

        setupPieChart();

        // Default: Filter Bulan Ini
        setMonthFilter();

        // 1. LOGIKA FILTER TANGGAL (Range Picker)
        btnDateFilter.setOnClickListener(v -> showDateRangePicker());

        // 2. LOGIKA GANTI MODE GRAFIK (IN/OUT)
        toggleChart.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showExpenseChart = (checkedId == R.id.btnShowExpense);
                refreshData();
            }
        });

        // 3. INTERAKSI GRAFIK DIKLIK (Muncul Detail)
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e == null) return;
                PieEntry entry = (PieEntry) e;
                String category = entry.getLabel(); // Ambil nama kategori yg diklik
                showCategoryDetail(category);
            }

            @Override
            public void onNothingSelected() {}
        });

        btnBack.setOnClickListener(v -> finish());
    }

    // --- BAGIAN 1: SETUP CHART & DATA ---

    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleRadius(55f);
        pieChart.setCenterTextSize(14f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.setExtraOffsets(20, 0, 20, 0);
    }

    // --- 1. LOGIKA HITUNG DATA & CASHFLOW ---
    private void refreshData() {
        ArrayList<Expense> filteredList = getFilteredList(); // Ambil data sesuai tanggal

        long totalInc = 0;
        long totalExp = 0;

        // List khusus untuk data grafik
        ArrayList<Expense> chartDataList = new ArrayList<>();

        for (Expense ex : filteredList) {
            String type = ex.getType();

            // Hitung Total Pemasukan & Pengeluaran
            if ("IN".equals(type)) {
                totalInc += ex.getAmount();
            } else {
                totalExp += ex.getAmount();
            }

            // Filter Data untuk masuk ke Grafik
            if (showExpenseChart && !"IN".equals(ex.getType())) {
                chartDataList.add(ex);
            }
            else if (!showExpenseChart && "IN".equals(ex.getType())) {
                chartDataList.add(ex);
            }
        }

        // Format Rupiah
        Locale localeID = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

        // Update Teks Pemasukan & Pengeluaran
        tvTotalIncome.setText(formatRupiah.format(totalInc));
        tvTotalExpense.setText(formatRupiah.format(totalExp));

        // --- HITUNG NET CASHFLOW (SISA) ---
        long netFlow = totalInc - totalExp;
        tvNetTotal.setText(formatRupiah.format(netFlow));

        // Logika Warna & Status Cashflow
        if (netFlow > 0) {
            tvNetStatus.setText("Surplus (Hemat)");
            tvNetStatus.setTextColor(Color.parseColor("#2E7D32")); // Hijau
            tvNetTotal.setTextColor(Color.parseColor("#2E7D32"));
            cardNetCashflow.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
        } else if (netFlow < 0) {
            tvNetStatus.setText("Defisit (Boros)");
            tvNetStatus.setTextColor(Color.parseColor("#C62828")); // Merah
            tvNetTotal.setTextColor(Color.parseColor("#C62828"));
            cardNetCashflow.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
        } else {
            tvNetStatus.setText("Seimbang");
            tvNetStatus.setTextColor(Color.parseColor("#424242")); // Abu
            tvNetTotal.setTextColor(Color.parseColor("#424242"));
            cardNetCashflow.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        // Update Grafik
        updatePieChart(chartDataList);

        // Update Judul Tengah Grafik
        pieChart.setCenterText(showExpenseChart ? "Total\nPengeluaran" : "Total\nPemasukan");
    }


    // --- 2. DESAIN GRAFIK MODERN (WARNA-WARNI) ---
    private void updatePieChart(ArrayList<Expense> dataList) {
        HashMap<String, Long> categoryMap = new HashMap<>();
        for (Expense ex : dataList) {
            long current = categoryMap.getOrDefault(ex.getCategory(), 0L);
            categoryMap.put(ex.getCategory(), current + ex.getAmount());
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        if(entries.isEmpty()){
            pieChart.clear();
            pieChart.setNoDataText("Tidak ada data pada periode ini");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        ArrayList<Integer> colors = new ArrayList<>();

        if (showExpenseChart) {
            // Palet Pengeluaran
            colors.add(Color.parseColor("#FF5252"));
            colors.add(Color.parseColor("#FF9800"));
            colors.add(Color.parseColor("#3F51B5"));
            colors.add(Color.parseColor("#00BCD4"));
            colors.add(Color.parseColor("#9C27B0"));
            colors.add(Color.parseColor("#FFC107"));
            colors.add(Color.parseColor("#607D8B"));
        } else {
            // Palet Pemasukan
            colors.add(Color.parseColor("#4CAF50"));
            colors.add(Color.parseColor("#009688"));
            colors.add(Color.parseColor("#8BC34A"));
            colors.add(Color.parseColor("#03A9F4"));
            colors.add(Color.parseColor("#CDDC39"));
        }

        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NumberFormat.getNumberInstance(Locale.US).format(value);
            }
        });

        pieChart.setData(data);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(65f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);

        pieChart.invalidate();
    }

    // --- BAGIAN 2: LOGIKA FILTER TANGGAL ---

    private void setMonthFilter() {
        Calendar c = Calendar.getInstance();
        endDate = c.getTimeInMillis();

        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        startDate = c.getTimeInMillis();

        updateDateLabel();
        refreshData();
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Pilih Periode Laporan")
                .setSelection(new Pair<>(startDate, endDate))
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            updateDateLabel();
            refreshData();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updateDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
        String start = sdf.format(new Date(startDate));
        String end = sdf.format(new Date(endDate));
        tvDateRange.setText(start + " - " + end);
    }

    private ArrayList<Expense> getFilteredList() {
        ArrayList<Expense> list = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());

        for (Expense ex : allExpenses) {
            try {
                Date date = sdf.parse(ex.getDate());
                if (date != null) {
                    long time = date.getTime();
                    if (time >= startDate - 86400000 && time <= endDate + 86400000) {
                        list.add(ex);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    // --- BAGIAN 3: BOTTOM SHEET DETAIL (KLIK GRAFIK) ---

    private void showCategoryDetail(String categoryName) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_detail, null);
        bottomSheetDialog.setContentView(view);

        TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        TextView tvTotal = view.findViewById(R.id.tvSheetTotal);
        RecyclerView rvDetails = view.findViewById(R.id.rvCategoryDetails);

        tvTitle.setText(categoryName);

        ArrayList<Expense> categoryList = new ArrayList<>();
        ArrayList<Expense> dateFiltered = getFilteredList();
        long totalCat = 0;

        for (Expense ex : dateFiltered) {
            if (ex.getCategory().equals(categoryName)) {
                boolean isExpenseType = !"IN".equals(ex.getType());
                if (showExpenseChart == isExpenseType) {
                    categoryList.add(ex);
                    totalCat += ex.getAmount();
                }
            }
        }

        Locale localeID = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        tvTotal.setText(formatRupiah.format(totalCat));

        ExpenseAdapter sheetAdapter = new ExpenseAdapter(categoryList, item -> {});
        rvDetails.setLayoutManager(new LinearLayoutManager(this));
        rvDetails.setAdapter(sheetAdapter);

        bottomSheetDialog.show();
    }
}