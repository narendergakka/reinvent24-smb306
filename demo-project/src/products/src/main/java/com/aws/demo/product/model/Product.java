package com.aws.demo.product.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Product {

    private String productId;
    private String productName;
    private String category;
    private String productDescription;
    private String features;
    private String brand;
    private String imageName;
    private String creationTime;

    public Product() {
    }
    

    public Product(String productId, String productName, String productDescription, String category) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.category = category;
    }


    public String getProductId() {
        return productId;
    }   
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductDescription() {
        return productDescription;
    }
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }   

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getFeatures() {
        return features;
    }
    
    public void setFeatures(String features) {
        this.features = features;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName){
        this.imageName = imageName;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public static Product fromJson(String json) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, Product.class);
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }
}


