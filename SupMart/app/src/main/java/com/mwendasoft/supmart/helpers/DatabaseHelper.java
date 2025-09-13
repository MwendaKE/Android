package com.mwendasoft.supmart.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.database.SQLException;

import com.mwendasoft.supmart.models.Product;
import com.mwendasoft.supmart.models.Supplier;

import java.io.*;
import java.util.ArrayList;
import android.content.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "SupMart.db";
    private static final int DB_VERSION = 1;
    private static final String DB_PATH_SUFFIX = "/databases/";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        copyDatabaseFromAssets();  // 💾 Copy DB on first install
    }

    private String getDatabasePath() {
        return context.getApplicationInfo().dataDir + DB_PATH_SUFFIX + DB_NAME;
    }

    private void copyDatabaseFromAssets() {
        File dbFile = new File(getDatabasePath());
        if (!dbFile.exists()) {
            File dbDir = new File(context.getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!dbDir.exists()) {
                dbDir.mkdir();
            }

            try {
                InputStream input = context.getAssets().open(DB_NAME);
                OutputStream output = new FileOutputStream(dbFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }

                output.flush();
                output.close();
                input.close();

                System.out.println("✅ SupMart.db copied successfully.");

            } catch (Exception e) {
                throw new Error("❌ Error copying database: " + e.getMessage());
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // No need to create tables — already exists in SupMart.db
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not needed for now
    }

    public ArrayList<Product> getProducts(String keyword, boolean isNumber) {
		ArrayList<Product> list = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursor;
		if (keyword.isEmpty()) {
			cursor = db.rawQuery(
				"SELECT p.*, s.supplier_name FROM products p " +
				"LEFT JOIN suppliers s ON p.product_supplier = s.supplier_code",
				null
			);
		} else if (isNumber) {
			cursor = db.rawQuery(
				"SELECT p.*, s.supplier_name FROM products p " +
				"LEFT JOIN suppliers s ON p.product_supplier = s.supplier_code " +
				"WHERE p.product_code LIKE ?",
				new String[]{"%" + keyword + "%"}
			);
		} else {
			cursor = db.rawQuery(
				"SELECT p.*, s.supplier_name FROM products p " +
				"LEFT JOIN suppliers s ON p.product_supplier = s.supplier_code " +
				"WHERE p.product_name LIKE ?",
				new String[]{"%" + keyword + "%"}
			);
		}

		if (cursor.moveToFirst()) {
			do {
				Product p = new Product();
				p.productCode = cursor.getString(cursor.getColumnIndex("product_code"));
				p.productName = cursor.getString(cursor.getColumnIndex("product_name"));
				p.productPackage = cursor.getString(cursor.getColumnIndex("product_package"));
				p.productSupplier = cursor.getString(cursor.getColumnIndex("product_supplier"));
				p.supplierName = cursor.getString(cursor.getColumnIndex("supplier_name")); // ✅ New field
				list.add(p);
			} while (cursor.moveToNext());
		}

		cursor.close();
		return list;
	}

    // ✅ Get Suppliers with Search
    public ArrayList<Supplier> getSuppliers(String keyword) {
        ArrayList<Supplier> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM suppliers WHERE supplier_name LIKE ? OR supplier_code LIKE ?",
									new String[]{"%" + keyword + "%", "%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                Supplier s = new Supplier();
                s.supplierCode = cursor.getString(cursor.getColumnIndex("supplier_code"));
                s.supplierName = cursor.getString(cursor.getColumnIndex("supplier_name"));
                s.supplierAddress = cursor.getString(cursor.getColumnIndex("supplier_address"));
                list.add(s);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
	
	public int countProducts() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM products", null);
		int count = 0;
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}

	public int countSuppliers() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM suppliers", null);
		int count = 0;
		if (cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		return count;
	}
	
	public ArrayList<String> getProductNamesBySupplier(String supplierCode) {
		ArrayList<String> products = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT product_name FROM products WHERE product_supplier = ?",
									new String[]{supplierCode});

		if (cursor.moveToFirst()) {
			do {
				products.add(cursor.getString(cursor.getColumnIndex("product_name")));
			} while (cursor.moveToNext());
		}

		cursor.close();
		return products;
	}
	
	public ArrayList<Product> getProductsBySupplierCode(String supplierCode) {
		ArrayList<Product> list = new ArrayList<>();
		SQLiteDatabase db = getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT * FROM products WHERE product_supplier = ?", new String[]{supplierCode});

		if (cursor.moveToFirst()) {
			do {
				Product p = new Product();
				p.productCode = cursor.getString(cursor.getColumnIndex("product_code"));
				p.productName = cursor.getString(cursor.getColumnIndex("product_name"));
				p.productPackage = cursor.getString(cursor.getColumnIndex("product_package"));
				p.productSupplier = cursor.getString(cursor.getColumnIndex("product_supplier"));
				list.add(p);
			} while (cursor.moveToNext());
		}

		cursor.close();
		return list;
	}
	
	// ✅ Check if supplier exists
	public boolean isSupplierExists(String code) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT id FROM suppliers WHERE supplier_code = ?", new String[]{code});
		boolean exists = cursor.moveToFirst();
		cursor.close();
		return exists;
	}

// ✅ Insert supplier
	public void insertSupplier(String code, String name, String address) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("supplier_code", code);
		values.put("supplier_name", name);
		values.put("supplier_address", address);
		db.insert("suppliers", null, values);
	}

// ✅ Insert product
	public void insertProduct(String code, String name, String pack, String supplierCode) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("product_code", code);
		values.put("product_name", name);
		values.put("product_package", pack);
		values.put("product_supplier", supplierCode);
		db.insert("products", null, values);
	}
	
	// ✅ Delete all products linked to a supplier
	public void deleteProductsBySupplier(String supplierCode) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete("products", "product_supplier = ?", new String[]{supplierCode});
	}
}
