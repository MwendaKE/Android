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


    // CODE OMITTED 

}