package com.informatika.aplikasikeuanganmahasiswabytoriq;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private ArrayList<Expense> expenseList;

    // DUA LIST KATEGORI
    private ArrayList<String> expenseCategories;
    private ArrayList<String> incomeCategories;

    private ExpenseAdapter adapter;
    private TextView tvTotalBalance;
    private RecyclerView rvExpenses;
    private boolean isBalanceVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(this);
        expenseList = prefManager.getExpenses();

        // LOAD DUA JENIS KATEGORI
        expenseCategories = prefManager.getExpenseCategories();
        incomeCategories = prefManager.getIncomeCategories();

        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        rvExpenses = findViewById(R.id.rvExpenses);

        ImageButton btnMenu = findViewById(R.id.btnReport);
        btnMenu.setOnClickListener(this::showPopupMenu);

        ImageView btnEye = findViewById(R.id.btnToggleBalance);
        if(btnEye != null) {
            btnEye.setOnClickListener(v -> {
                isBalanceVisible = !isBalanceVisible;
                updateTotalBalance();
                btnEye.setAlpha(isBalanceVisible ? 1.0f : 0.5f);
            });
        }

        setupRecyclerView();
        updateTotalBalance();

        findViewById(R.id.fabAdd).setOnClickListener(v -> showDialog(null));
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_report) {
                Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_about) {

                // --- LOGIKA TENTANG APLIKASI (POPUP KEREN) ---
                new AlertDialog.Builder(this)
                        .setTitle("Tentang Aplikasi")
                        .setMessage("MyFinancial\n\n" +
                                "Aplikasi Pengelola Keuangan Mahasiswa.\n\n" +
                                "Dibuat oleh:\n" +
                                "Nama : Azis Toriq Maulana\n" +
                                "Informatika")
                        .setPositiveButton("Mantap", null)
                        .show();
                // ---------------------------------------------

                return true;
            }
            return false;
        });
        popup.show();
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(expenseList, this::showActionDialog);
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);
    }

    private void updateTotalBalance() {
        long total = 0;
        for (Expense expense : expenseList) {
            if ("IN".equals(expense.getType())) {
                total += expense.getAmount();
            } else {
                total -= expense.getAmount();
            }
        }

        if (isBalanceVisible) {
            Locale localeID = new Locale("id", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            tvTotalBalance.setText(formatRupiah.format(total));
        } else {
            tvTotalBalance.setText("Rp ••••••••");
        }
    }

    private void showDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        ImageButton btnAddCategory = dialogView.findViewById(R.id.btnAddCategory);

        RadioGroup rgType = dialogView.findViewById(R.id.rgType);
        RadioButton rbExpense = dialogView.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rbIncome);

        // --- LOGIKA SPINNER DINAMIS ---
        // Adapter Awal (Default Pengeluaran)
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Fungsi Update Isi Spinner berdasarkan Pilihan
        Runnable updateSpinner = () -> {
            spinnerAdapter.clear();
            if (rbIncome.isChecked()) {
                spinnerAdapter.addAll(incomeCategories); // Isi Gaji, Bonus...
            } else {
                spinnerAdapter.addAll(expenseCategories); // Isi Makan, Transport...
            }
            spinnerAdapter.notifyDataSetChanged();
        };

        // Listener saat Radio Button diklik (Ganti isi spinner)
        rgType.setOnCheckedChangeListener((group, checkedId) -> updateSpinner.run());

        // Jalankan sekali di awal
        updateSpinner.run();

        // --------------------------------

        btnAddCategory.setOnClickListener(v -> {
            AlertDialog.Builder catBuilder = new AlertDialog.Builder(this);
            catBuilder.setTitle("Kategori Baru");
            final EditText input = new EditText(this);
            input.setHint("Nama Kategori...");
            catBuilder.setView(input);
            catBuilder.setPositiveButton("Tambah", (d, w) -> {
                String newCat = input.getText().toString();
                if (!newCat.isEmpty()) {
                    // Simpan ke list yang sesuai (IN atau OUT)
                    if (rbIncome.isChecked()) {
                        incomeCategories.add(newCat);
                        prefManager.saveIncomeCategories(incomeCategories);
                    } else {
                        expenseCategories.add(newCat);
                        prefManager.saveExpenseCategories(expenseCategories);
                    }
                    updateSpinner.run(); // Refresh spinner
                    spinnerCategory.setSelection(spinnerAdapter.getCount() - 1);
                }
            });
            catBuilder.show();
        });

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        etDate.setText(selectedDate);
                    }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        if (expense != null) {
            // MODE EDIT
            etTitle.setText(expense.getTitle());
            etAmount.setText(String.valueOf(expense.getAmount()));
            etDate.setText(expense.getDate());
            builder.setTitle("Edit Transaksi");

            // Set Radio Button
            if("IN".equals(expense.getType())) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }
            // Update spinner dulu biar isinya benar
            updateSpinner.run();

            // Baru set selection
            int spinnerPosition = spinnerAdapter.getPosition(expense.getCategory());
            if(spinnerPosition >= 0) spinnerCategory.setSelection(spinnerPosition);

        } else {
            builder.setTitle("Tambah Transaksi");
            Calendar c = Calendar.getInstance();
            String today = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
            etDate.setText(today);
            // Default Pengeluaran
            rbExpense.setChecked(true);
            updateSpinner.run();
        }

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String title = etTitle.getText().toString();
            String amountStr = etAmount.getText().toString();
            String date = etDate.getText().toString();
            String category = "";
            if(spinnerCategory.getSelectedItem() != null) category = spinnerCategory.getSelectedItem().toString();

            String type = (rgType.getCheckedRadioButtonId() == R.id.rbIncome) ? "IN" : "OUT";

            if (!title.isEmpty() && !amountStr.isEmpty() && !date.isEmpty() && !category.isEmpty()) {
                long amount = Long.parseLong(amountStr);
                if (expense == null) {
                    expenseList.add(0, new Expense(title, amount, category, date, type));
                } else {
                    expense.setTitle(title);
                    expense.setAmount(amount);
                    expense.setCategory(category);
                    expense.setDate(date);
                    expense.setType(type);
                }
                prefManager.saveExpenses(expenseList);
                adapter.notifyDataSetChanged();
                updateTotalBalance();
            } else {
                Toast.makeText(MainActivity.this, "Lengkapi data!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void showActionDialog(Expense expense) {
        CharSequence[] options = {"Edit", "Hapus"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showDialog(expense);
            } else {
                expenseList.remove(expense);
                prefManager.saveExpenses(expenseList);
                adapter.notifyDataSetChanged();
                updateTotalBalance();
                Toast.makeText(this, "Data dihapus", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}