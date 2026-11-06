package com.example.mypersonalfinances;

public class ProductoAPI {
    private int id;
    private String title;
    private double price;
    private String description;
    private String category;
    private String image;

    // Constructor vacío
    public ProductoAPI() {}

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getImage() { return image; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setImage(String image) { this.image = image; }
}