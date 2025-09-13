package com.mwendasoft.supmart.helpers;

import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

public class ExcelImporter {

    private static final String TAG = "ExcelImporter";

    private static final String SUPMART_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/SupMart";

    private DatabaseHelper dbHelper;

    public ExcelImporter(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // 📥 Import all Excel files
    public void importFromExcelFiles() {
        File dir = new File(SUPMART_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.e(TAG, "❌ SupMart directory not found.");
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            Log.d(TAG, "📭 No Excel files found.");
            return;
        }

        for (File file : files) {
            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xls")) {
                processExcelFile(file);
            }
        }
    }

    private void processExcelFile(File file) {
        try {
            String name = file.getName().replace(".xls", "");
            String[] parts = name.split(" - ");
            if (parts.length != 2) {
                Log.w(TAG, "⛔ Skipping invalid filename: " + file.getName());
                return;
            }

            String supplierCode = parts[0].trim();
            String supplierName = parts[1].trim();

            Log.d(TAG, "📥 Reading: " + supplierCode + " - " + supplierName);

            // 🔁 If supplier exists, update its products
            if (dbHelper.isSupplierExists(supplierCode)) {
                Log.d(TAG, "🔁 Updating supplier: " + supplierCode);
                dbHelper.deleteProductsBySupplier(supplierCode);
            } else {
                dbHelper.insertSupplier(supplierCode, supplierName, "");
                Log.d(TAG, "➕ New supplier: " + supplierCode);
            }

            FileInputStream fis = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                Log.w(TAG, "⚠️ Empty sheet: " + file.getName());
                fis.close();
                return;
            }

            for (Row row : sheet) {
                Cell cell0 = row.getCell(0);
                Cell cell1 = row.getCell(1);
                if (cell0 == null || cell1 == null) continue;

                String productCode = getCellValue(cell0).trim();
                String productName = getCellValue(cell1).trim();

                Log.d(TAG, "🔎 Code: " + productCode + ", Name: " + productName);

                if (productCode.matches("\\d{6}")) {
                    dbHelper.insertProduct(productCode, productName, "", supplierCode);
                    Log.d(TAG, "✅ Inserted: " + productCode);
                } else {
                    Log.w(TAG, "⚠️ Skipped invalid code: " + productCode);
                }
            }

            fis.close();
            Log.d(TAG, "🎉 Done: " + supplierCode + " - " + supplierName);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error reading file: " + file.getName() + " → " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }
}
