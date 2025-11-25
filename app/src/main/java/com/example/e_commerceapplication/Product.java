package com.example.e_commerceapplication;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String imageURL;
    private String sellerId;

    public Product(){

    }

    public Product(String id, String name, String description, double price, String imageURL){
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
        this.sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public Product(String id, String name, String description, double price ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        imageURL = null;
        this.sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public String getSellerId(){
        return sellerId;
    }
}
