package org.example;

public abstract class Item {
    private final int itemID;
    private final String title, author, description;
    private final double price;
    private int stock;

    protected Item(int id, String title, double price, String author, String description, int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.itemID = id;
        this.title = title;
        this.price = price;
        this.author = author;
        this.description = description;
        this.stock = stock;
    }
    public int getItemID() { return itemID; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public int getStock() { return stock; }
    public void setStock(int stock) { if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative."); this.stock = stock; }
    public boolean reduceStock(int quantity) { if (quantity < 1 || stock < quantity) return false; stock -= quantity; return true; }
    public boolean reduceStock() { return reduceStock(1); }
    public abstract String getType();
}
