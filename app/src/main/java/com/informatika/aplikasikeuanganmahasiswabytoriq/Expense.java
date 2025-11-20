package com.informatika.aplikasikeuanganmahasiswabytoriq;
public class Expense {
    private String id;
    private String title;
    private long amount;
    private String category;
    private String date;
    private String type; // BARU: "IN" atau "OUT"

    // Constructor Diupdate
    public Expense(String title, long amount, String category, String date, String type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.type = type;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // Getter Setter Baru
    public String getType() {
        // JIKA DATA LAMA (NULL), ANGGAP PENGELUARAN ("OUT")
        if (type == null || type.isEmpty()) {
            return "OUT";
        }
        return type;
    }
    public void setType(String type) { this.type = type; }
}