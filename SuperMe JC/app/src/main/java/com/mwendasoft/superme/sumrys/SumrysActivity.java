package com.mwendasoft.superme.sumrys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.app.*;
import android.database.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import com.mwendasoft.superme.events.*;
import com.mwendasoft.superme.books.*;
import android.graphics.*;

public class SumrysActivity extends BaseActivity {
    // Constants for context menu items
    private static final int MENU_OPEN = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_DELETE = 3;

    private SumrysDBHelper dbHelper;
    private BooksDBHelper bkHelper;
    private ListView sumrysListView;
    private TextView sumrysListTitle, sumrysCountBadge;
    private ImageButton addSumryFab;

    private SumrysViewAdapter sumrysAdapter;
    private final ArrayList<Sumry> summaries = new ArrayList<>(); // Initialize as final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sumrys_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapters();
        setupListeners();

        loadSummaries();
        setupHeaderNavigation("SUMRYS", this);
    }

    private void initializeViews() {
        sumrysListTitle = findViewById(R.id.sumrysListTitle);
        sumrysCountBadge = findViewById(R.id.sumrysCountBadge);
        sumrysListView = findViewById(R.id.sumrysListView);
        addSumryFab = findViewById(R.id.addSumryFab);

        registerForContextMenu(sumrysListView);
    }

    private void setupDatabaseHelpers() {
        dbHelper = new SumrysDBHelper(this);
        bkHelper = new BooksDBHelper(this);
    }

    private void setupAdapters() {
        sumrysAdapter = new SumrysViewAdapter(this, summaries);
        sumrysListView.setAdapter(sumrysAdapter);
    }

    private void setupListeners() {
        sumrysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Sumry selectedSumry = summaries.get(position);
					openSummaryDetail(selectedSumry);
				}
			});

        addSumryFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(SumrysActivity.this, SumryAddActivity.class));
				}
			});
    }

    // ==== CONTEXT MENU METHODS === //

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.sumrysListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Sumry selectedTask = summaries.get(info.position);
            menu.setHeaderTitle(selectedTask.getTitle());

            menu.add(Menu.NONE, MENU_OPEN, 1, "Open");
            menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
            menu.add(Menu.NONE, MENU_DELETE, 3, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Sumry selectedSummary = summaries.get(info.position);

        switch (item.getItemId()) {
            case MENU_OPEN:
                openSummaryDetail(selectedSummary);
                return true;
            case MENU_DELETE:
                confirmAndDeleteSummary(selectedSummary);
                return true;
            case MENU_EDIT:
                editSummary(selectedSummary);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmAndDeleteSummary(final Sumry selectedSummary) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Are you sure you want to delete \"" + selectedSummary.getTitle() + "\"?")
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.deleteSummary(selectedSummary.getId());
                    summaries.remove(selectedSummary);
                    if (sumrysAdapter != null) {
                        sumrysAdapter.notifyDataSetChanged();
                    }
                    updateBadges();
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void editSummary(Sumry sumry) {
        Intent intent = new Intent(this, SumryEditActivity.class);
        intent.putExtra("selectedSumry", sumry);
        startActivity(intent);
    }

    // == SWITCH LAYOUT == //

    private void setupHeaderNavigation(final String currentScreen, final Activity currentActivity) {
        LinearLayout booksSection = findViewById(R.id.booksSection);
        LinearLayout sumrysSection = findViewById(R.id.sumrysSection);
        TextView booksTitle = findViewById(R.id.booksListTitle);
        TextView sumrysTitle = findViewById(R.id.sumrysListTitle);

        if (booksSection == null || sumrysSection == null || booksTitle == null || sumrysTitle == null) {
            return;
        }

        // Highlight current screen
        if (currentScreen.equals("BOOKS")) {
            booksTitle.setTextColor(Color.parseColor("#7A2021"));
            booksTitle.setTypeface(null, Typeface.BOLD);    
        } else if (currentScreen.equals("SUMRYS")) {
            sumrysTitle.setTextColor(Color.parseColor("#8D4004"));
            sumrysTitle.setTypeface(null, Typeface.BOLD);
        }

        booksSection.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!currentScreen.equals("BOOKS")) {
						Intent intent = new Intent(currentActivity, BooksActivity.class);
						currentActivity.startActivity(intent);
						currentActivity.finish();
					}
				}
			});

        sumrysSection.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!currentScreen.equals("SUMRYS")) {
						Intent intent = new Intent(currentActivity, SumrysActivity.class);
						currentActivity.startActivity(intent);
						currentActivity.finish();
					}
				}
			});
    }

    private void loadSummaries() {
        summaries.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllSummaries();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int authorIndex = cursor.getColumnIndexOrThrow("author");
                int sumryIndex = cursor.getColumnIndexOrThrow("summary");
                int favoriteIndex = cursor.getColumnIndexOrThrow("favorite");

                do {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String author = cursor.getString(authorIndex);
                    String sumry = cursor.getString(sumryIndex);
                    int favorite = cursor.getInt(favoriteIndex);

                    if (title != null && !title.isEmpty()) {
                        summaries.add(new Sumry(id, title, author, sumry, favorite));
                    }
                } while (cursor.moveToNext());
            }

            updateUIAfterLoading();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateUIAfterLoading() {
        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					sumrysListTitle.setText(R.string.sumrys);
					sumrysCountBadge.setText(String.valueOf(summaries.size()));

					if (summaries.isEmpty()) {
						showNoSummariesDialog();
					} else if (sumrysAdapter != null) {
						sumrysAdapter.notifyDataSetChanged();
					}
				}
			});
    }

    private void showNoSummariesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sumrys);
        builder.setMessage("No summaries found. Would you like to add a new one?");
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(SumrysActivity.this, SumryAddActivity.class));
				}
			});
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void openSummaryDetail(Sumry summary) {
        Intent intent = new Intent(this, SumryDetailActivity.class);
        intent.putExtra("selectedSumry", summary);
        startActivity(intent);
    }

    private void updateBadges() {
        if (sumrysCountBadge == null) return;

        new Thread(new Runnable() {
				@Override
				public void run() {
					final int booksCount = bkHelper.getBooksCount();
					final int sumrysCount = dbHelper.getSummariesCount();

					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								TextView booksBadge = findViewById(R.id.booksCountBadge);
								if (booksBadge != null) {
									booksBadge.setText(String.valueOf(booksCount));
								}
								sumrysCountBadge.setText(String.valueOf(sumrysCount));
							}
						});
				}
			}).start();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (bkHelper != null) {
            bkHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummaries();
        updateBadges();
        setupHeaderNavigation("SUMRYS", this);
    }
}
