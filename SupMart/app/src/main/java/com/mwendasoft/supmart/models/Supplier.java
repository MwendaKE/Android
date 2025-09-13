package com.mwendasoft.supmart.models;

public class Supplier {
    public String supplierCode;
    public String supplierName;
    public String supplierAddress;

    public Supplier() {
        // Default constructor
    }

    public Supplier(String code, String name, String address) {
        this.supplierCode = code;
        this.supplierName = name;
        this.supplierAddress = address;
    }
}
