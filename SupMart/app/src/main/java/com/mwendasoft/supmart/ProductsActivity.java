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
import android.app.*;

public class ProductsActivity extends Activity {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private TextView productCount, supplierCount;
    private ImageView switchButton, updateIcon, aboutIcon;
    private ProductAdapter adapter;
    private ArrayList<Product> productList;
    private DatabaseHelper dbHelper;

    private final String SUPMART_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/SupMart";

    // CODE OMITTED

}