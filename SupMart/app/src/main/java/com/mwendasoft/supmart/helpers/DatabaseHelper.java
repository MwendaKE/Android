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

    // CODE OMITTED

}
