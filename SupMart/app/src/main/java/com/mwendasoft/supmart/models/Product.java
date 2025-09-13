package com.mwendasoft.supmart.models;

public class Product {
    public String productCode;
    public String productName;
    public String productPackage;
    public String productSupplier;   // supplier code
    public String supplierName;      // ✅ new field to show the name

    public Product() {
        // Default constructor
    }

    // You can keep or skip this constructor for now if unused
    public Product(String code, String name, String pack, String supplier) {
        this.productCode = code;
        this.productName = name;
        this.productPackage = pack;
        this.productSupplier = supplier;
    }
}
