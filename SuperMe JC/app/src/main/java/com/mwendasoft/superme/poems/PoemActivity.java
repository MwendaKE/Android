package com.mwendasoft.superme.poems;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import com.mwendasoft.superme.authors.*;
import android.view.*;
import java.io.*;
import android.os.*;
import com.mwendasoft.superme.helpers.*;

public class PoemActivity extends BaseActivity {
    // Constants for context menu items
    private static final int MENU_OPEN = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_SHARE = 3;
    private static final int MENU_DELETE = 4;

    private PoemsDBHelper poemDbHelper;
    private AuthorsDBHelper authorsDbHelper;
    private ListView poemsListView;
    private TextView poemsListTitle, poemsCountBadge;
    private ImageButton addPoemFab;
    private final ArrayList<Poem> poems = new ArrayList<>();
    private PoemViewAdapter poemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poem_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapter();
        setupListeners();

        loadPoems();
    }

    private void initializeViews() {
        poemsListView = findViewById(R.id.poemsListView);
        poemsListTitle = findViewById(R.id.poemsListTitle);
        poemsCountBadge = findViewById(R.id.poemsCountBadge);
        addPoemFab = findViewById(R.id.addPoemFab);

        registerForContextMenu(poemsListView);
    }

    private void setupDatabaseHelpers() {
        poemDbHelper = new PoemsDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
    }

    private void setupAdapter() {
        poemAdapter = new PoemViewAdapter(this, poems);
        poemsListView.setAdapter(poemAdapter);
    }

    private void setupListeners() {
        poemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Poem clickedPoem = poems.get(position);
					if (clickedPoem != null) {
						openPoemDetail(clickedPoem);
					}
				}
			});

        addPoemFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(PoemActivity.this, PoemAddActivity.class));
				}
			});
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.poemsListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Poem selectedPoem = poems.get(info.position);
            menu.setHeaderTitle(selectedPoem.getPoemTitle());
            menu.add(Menu.NONE, MENU_OPEN, 1, "Open");
            menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
            menu.add(Menu.NONE, MENU_SHARE, 3, "Share");
            menu.add(Menu.NONE, MENU_DELETE, 4, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Poem selectedPoem = poems.get(info.position);

        switch (item.getItemId()) {
            case MENU_OPEN:
                openPoemDetail(selectedPoem);
                return true;
            case MENU_DELETE:
                confirmAndDeletePoem(selectedPoem);
                return true;
            case MENU_SHARE:
                sharePoem(selectedPoem);
                return true;
            case MENU_EDIT:
                editPoem(selectedPoem);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmAndDeletePoem(final Poem poem) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Are you sure you want to delete \"" + poem.getPoemTitle() + "\" poem?")
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deletePoemWithMedia(poem);
					loadPoems();
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void deletePoemWithMedia(Poem poem) {
        // Delete associated media files
        deletePoemMediaFiles(poem);

        // Delete from database
        poemDbHelper.deletePoem(poem.getId());

        // Update UI
        poems.remove(poem);
        if (poemAdapter != null) {
            poemAdapter.notifyDataSetChanged();
        }
        updatePoemCount();
    }

    private void deletePoemMediaFiles(Poem poem) {
        // Delete the media folder
        File poemFolder = new File(Environment.getExternalStorageDirectory(), ".superme/poems/Poem " + poem.getId());
        deleteFolderRecursive(poemFolder);

        // Delete any individual media files as safety
        String poemImagePath = poemDbHelper.getPoemPath(poem.getId());
        if (poemImagePath != null) {
            File file = new File(poemImagePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private void deleteFolderRecursive(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteFolderRecursive(file);
                    } else {
                        file.delete();
                    }
                }
            }
            folder.delete(); // delete the folder itself
        }
    }

    private boolean sharePoem(Poem poem) {
        String poemText = null;
        Cursor cursor = null;

        try {
            int poetId = authorsDbHelper.getAuthorIdByName(poem.getPoemAuthor());
            cursor = poemDbHelper.getPoemDetailByTitleAndAuthor(poem.getPoemTitle(), poetId);

            if (cursor != null && cursor.moveToFirst()) {
                poemText = cursor.getString(cursor.getColumnIndexOrThrow("poem"));
            }

            // Check if poemText is valid before sharing
            if (poemText == null || poemText.isEmpty()) {
				ToastMessagesManager.show(this, "Poem content not available.");
                return false;
            }

            String shareText = poem.getPoemTitle() + "\n\n" + poemText + "\n\n~ " + poem.getPoemAuthor();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void editPoem(Poem poem) {
        Intent intent = new Intent(PoemActivity.this, PoemEditActivity.class);
        intent.putExtra("poemTitle", poem.getPoemTitle());
        intent.putExtra("poemAuthor", poem.getPoemAuthor());
        startActivity(intent);
    }

    private void loadPoems() {
        poems.clear();

        Cursor cursor = null;
        try {
            cursor = poemDbHelper.getAllPoems();
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int poetIndex = cursor.getColumnIndexOrThrow("poet");

                do {
                    int poemId = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    int poetId = cursor.getInt(poetIndex);
                    String poetName = authorsDbHelper.getAuthorNameById(poetId);

                    if (title != null && !title.isEmpty()) {
                        poems.add(new Poem(poemId, title, poetName));
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
					poemsListTitle.setText(R.string.poems);
					updatePoemCount();

					if (poems.isEmpty()) {
						showNoPoemsDialog();
					} else if (poemAdapter != null) {
						poemAdapter.notifyDataSetChanged();
					}
				}
			});
    }

    private void updatePoemCount() {
        poemsCountBadge.setText(String.valueOf(poems.size()));
    }

    private void showNoPoemsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.poems)
            .setMessage("No poems found. Would you like to add a new one?")
            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(PoemActivity.this, PoemAddActivity.class));
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void openPoemDetail(Poem poem) {
        Intent intent = new Intent(this, PoemDetailActivity.class);
        intent.putExtra("selectedPoem", poem);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (poemDbHelper != null) {
            poemDbHelper.close();
        }
        if (authorsDbHelper != null) {
            authorsDbHelper.close();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPoems();
    }
}
