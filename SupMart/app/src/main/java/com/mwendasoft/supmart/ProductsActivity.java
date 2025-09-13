package com.mwendasoft.supmart;

import android.os.Bundle;
import android.os.Environment;
import android.os.Build;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.*;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.mwendasoft.supmart.helpers.DatabaseHelper;
import com.mwendasoft.supmart.helpers.ExcelImporter;
import com.mwendasoft.supmart.models.Product;
import com.mwendasoft.supmart.viewadapters.ProductAdapter;

import java.io.File;
import java.util.ArrayList;

public class ProductsActivity extends Activity {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private TextView productCount, supplierCount;
    private ImageView switchButton, updateIcon;
    private ProductAdapter adapter;
    private ArrayList<Product> productList;
    private DatabaseHelper dbHelper;

    private final String SUPMART_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/SupMart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestStoragePermission(); // 🔐 Ask for permission at runtime

        setContentView(R.layout.activity_products);

        createSupMartDirectoryIfNotExists();

        dbHelper = new DatabaseHelper(this);

        // Setup action bar
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(R.layout.custom_actionbar);
        }

        // Actionbar views
        productCount = findViewById(R.id.productCount);
        supplierCount = findViewById(R.id.supplierCount);
        switchButton = findViewById(R.id.switchButton);
        updateIcon = findViewById(R.id.updateIcon);

        productCount.setText(String.valueOf(dbHelper.countProducts()));
        supplierCount.setText(String.valueOf(dbHelper.countSuppliers()));

        switchButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ProductsActivity.this, SuppliersActivity.class);
					startActivity(intent);
					finish();
				}
			});

        updateIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ExcelImporter importer = new ExcelImporter(dbHelper);
					importer.importFromExcelFiles();
					Toast.makeText(ProductsActivity.this, "✔️ Products updated!", Toast.LENGTH_SHORT).show();
					loadProducts("", false);
				}
			});

        searchInput = findViewById(R.id.searchEditText);
        recyclerView = findViewById(R.id.recyclerView);

        addRecyclerViewItemDivider(recyclerView);

        loadProducts("", false);

        searchInput.addTextChangedListener(new TextWatcher() {
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				@Override public void afterTextChanged(Editable s) { }

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					String input = s.toString().trim();
					boolean isNumber = input.matches("\\d+");
					loadProducts(input, isNumber);
				}
			});
    }

    private void loadProducts(String keyword, boolean isNumber) {
        productList = dbHelper.getProducts(keyword, isNumber);
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);
    }

    private void addRecyclerViewItemDivider(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.divider_grey));
        recyclerView.addItemDecoration(divider);
    }

    // ✅ Create folder if it doesn't exist
    private void createSupMartDirectoryIfNotExists() {
        File dir = new File(SUPMART_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                Log.d("SupMart", "✅ SupMart folder created.");
            } else {
                Log.e("SupMart", "❌ Failed to create SupMart folder.");
            }
        } else {
            Log.d("SupMart", "📁 SupMart folder already exists.");
        }
    }

    // ✅ Runtime storage permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
					new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1
                );
            }
        }
    }
}
