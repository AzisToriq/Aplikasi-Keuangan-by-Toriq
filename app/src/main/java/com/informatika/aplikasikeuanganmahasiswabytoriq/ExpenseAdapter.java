package com.informatika.aplikasikeuanganmahasiswabytoriq;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Penting
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> list;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public ExpenseAdapter(List<Expense> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvCategoryAndDate.setText(item.getCategory() + " • " + item.getDate());

        Locale localeID = new Locale("id", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        String formattedAmount = formatRupiah.format(item.getAmount());

        // LOGIKA WARNA HIJAU/MERAH
        if ("IN".equals(item.getType())) {
            // PEMASUKAN
            holder.tvAmount.setText("+ " + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_green));
        } else {
            // PENGELUARAN
            holder.tvAmount.setText("- " + formattedAmount);
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_red));
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategoryAndDate, tvAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategoryAndDate = itemView.findViewById(R.id.tvCategoryAndDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}