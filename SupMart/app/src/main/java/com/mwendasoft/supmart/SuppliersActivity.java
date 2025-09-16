package com.mwendasoft.supmart;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DividerItemDecoration;

import com.mwendasoft.supmart.helpers.DatabaseHelper;
import com.mwendasoft.supmart.models.Supplier;
import com.mwendasoft.supmart.viewadapters.SupplierAdapter;

import java.util.ArrayList;

import android.app.Activity;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.content.Intent;
import android.app.*;
import android.view.*;
import android.widget.*;
import com.mwendasoft.supmart.models.*;
import com.mwendasoft.supmart.helpers.*;

public class SuppliersActivity extends Activity {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private SupplierAdapter adapter;
    private ArrayList<Supplier> supplierList;
    private DatabaseHelper dbHelper;
    private TextView productCount, supplierCount;
	private ImageView switchButton, updateIcon, aboutIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers);

        // 🔹 Initialize database helper first
        dbHelper = new DatabaseHelper(this);

        // 🔹 Set up custom action bar
        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(R.layout.custom_actionbar);
        }

        // 🔹 Get views from custom action bar
        productCount = (TextView) findViewById(R.id.productCount);
        supplierCount = (TextView) findViewById(R.id.supplierCount);
        switchButton = (ImageView) findViewById(R.id.switchButton);
		updateIcon = (ImageView) findViewById(R.id.updateIcon);
		aboutIcon = (ImageView) findViewById(R.id.aboutIcon);
		
        // 🔹 Set counts
        productCount.setText(String.valueOf(dbHelper.countProducts()));
        supplierCount.setText(String.valueOf(dbHelper.countSuppliers()));

        // 🔹 Switch to ProductsActivity
        switchButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(SuppliersActivity.this, ProductsActivity.class);
					startActivity(intent);
					finish();
				}
			});
			
		//##

		updateIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ExcelImporter importer = new ExcelImporter(dbHelper);
					importer.importFromExcelFiles();
					Toast.makeText(SuppliersActivity.this, "✔️ Suppliers updated!", Toast.LENGTH_SHORT).show();
					loadSuppliers("");
				}
			});
			
		aboutIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAboutMessage();
				}
			});
		

        // 🔹 Setup search and RecyclerView
        searchInput = (EditText) findViewById(R.id.searchEditText);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        addRecyclerViewItemDivider(recyclerView);

        // 🔹 Initial supplier load
        loadSuppliers("");

        // 🔹 Add text change listener
        searchInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

				@Override
				public void afterTextChanged(Editable s) { }

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					loadSuppliers(s.toString());
				}
			});
    }
	
	private void showAboutMessage() {
		// Nice professional dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("About Supmart");
		builder.setIcon(R.drawable.ic_action_info); // About icon in drawable

		builder.setMessage("Creator: Mwenda E. Njagi\n"
						   + "Phone: +254 702 623 729\n"
						   + "Email: erickmwenda256@gmail.com\n"
						   + "Website: MwendaSoft.com");

		builder.setPositiveButton("OK", null);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

    private void loadSuppliers(String keyword) {
		supplierList = dbHelper.getSuppliers(keyword);
		adapter = new SupplierAdapter(supplierList);

		adapter.setOnItemClickListener(new SupplierAdapter.OnItemClickListener() {
				@Override
				public void onItemClick(Supplier supplier) {
					// ✅ Pass the two required parameters
					showProductsDialog(supplier.supplierCode, supplier.supplierName);
				}
			});

		recyclerView.setAdapter(adapter);
	}

    private void addRecyclerViewItemDivider(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.divider_grey)); // Thin grey line
        recyclerView.addItemDecoration(divider);
    }
	
	private void showProductsDialog(String supplierCode, String supplierName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_supplier_products, null);

		TextView titleView = (TextView) dialogView.findViewById(R.id.dialogTitle);
		titleView.setText("Products by: " + supplierName);

		ListView productListView = (ListView) dialogView.findViewById(R.id.productListView);

		ArrayList<Product> products = dbHelper.getProductsBySupplierCode(supplierCode);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			this,
			R.layout.item_dialog_product,
			getProductNames(products)
		);
		productListView.setAdapter(adapter);

		builder.setView(dialogView);
		builder.setPositiveButton("Close", null);

		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private ArrayList<String> getProductNames(ArrayList<Product> products) {
		ArrayList<String> names = new ArrayList<>();
		for (Product p : products) {
			names.add(p.productCode + " - " + p.productName);
		}
		return names;
	}
}
