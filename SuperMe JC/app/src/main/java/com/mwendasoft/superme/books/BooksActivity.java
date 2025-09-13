package com.mwendasoft.superme.books;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;

import java.util.ArrayList;
import android.widget.*;
import android.database.*;
import android.view.*;
import com.mwendasoft.superme.categories.*;
import com.mwendasoft.superme.authors.*;
import android.app.*;
import com.mwendasoft.superme.sumrys.*;
import android.graphics.*;
import com.mwendasoft.superme.*;
import android.content.*;
import com.mwendasoft.superme.helpers.*;

public class BooksActivity extends BaseActivity {
    // Constants for context menu items
    private static final int MENU_OPEN = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_MARK_READ = 3;
    private static final int MENU_DELETE = 4;

    private BooksDBHelper dbHelper;
    private SumrysDBHelper smHelper;
    private CategoriesDBHelper categsDbHelper;
    private AuthorsDBHelper authorsDbHelper;
    private ListView listView;
    private ImageButton fabButton;
    private TextView booksListTitle, booksCountBadge;
    private final ArrayList<Book> bookTitles = new ArrayList<>();
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.books_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapters();
        setupListeners();

        loadBooks();
        setupHeaderNavigation("BOOKS", this);
    }

    private void initializeViews() {
        fabButton = findViewById(R.id.addBookFab);
        listView = findViewById(R.id.booksListView);
        booksListTitle = findViewById(R.id.booksListTitle);
        booksCountBadge = findViewById(R.id.booksCountBadge);

        registerForContextMenu(listView);
    }

    private void setupDatabaseHelpers() {
        dbHelper = new BooksDBHelper(this);
        smHelper = new SumrysDBHelper(this);
        categsDbHelper = new CategoriesDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
    }

    private void setupAdapters() {
        bookAdapter = new BookAdapter(this, bookTitles);
        listView.setAdapter(bookAdapter);
    }

    private void setupListeners() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Book clickedBook = bookTitles.get(position);
					if (clickedBook != null) {
						openBookReview(clickedBook);
					}
				}
			});

        fabButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(BooksActivity.this, AddBookActivity.class));
				}
			});
    }

    // ==== CONTEXT MENU METHODS === //

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.booksListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Book selectedBook = bookTitles.get(info.position);
            menu.setHeaderTitle(selectedBook.getTitle());

            menu.add(Menu.NONE, MENU_OPEN, 1, "Open");
            menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
            menu.add(Menu.NONE, MENU_MARK_READ, 3, "Mark Read");
            menu.add(Menu.NONE, MENU_DELETE, 4, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Book selectedBook = bookTitles.get(info.position);

        switch (item.getItemId()) {
            case MENU_OPEN:
                openBookReview(selectedBook);
                return true;
            case MENU_DELETE:
                confirmAndDeleteBook(selectedBook);
                return true;
            case MENU_MARK_READ:
                markBookAsRead(selectedBook);
                return true;
            case MENU_EDIT:
                editBook(selectedBook);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmAndDeleteBook(final Book book) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage("Are you sure you want to delete \"" + book.getTitle() + "\"?")
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.deleteBook(book.getTitle(), book.getAuthor());
                    bookTitles.remove(book);
                    if (bookAdapter != null) {
                        bookAdapter.notifyDataSetChanged();
                    }
                    updateBadges();
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void markBookAsRead(Book book) {
        dbHelper.markBookAsRead(book.getTitle(), book.getAuthor());
        ToastMessagesManager.show(this, "\"" + book.getTitle() + "\" marked as read.");
    }

    private void editBook(Book book) {
        Intent intent = new Intent(this, EditBookActivity.class);
        intent.putExtra("selectedBook", book);
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

    private void loadBooks() {
        bookTitles.clear();

        Cursor cursor = null;
        try {
            cursor = dbHelper.getAllBooks();
            if (cursor != null && cursor.moveToFirst()) {
                int bookIdIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int authorIdIndex = cursor.getColumnIndexOrThrow("author");
                int categIdIndex = cursor.getColumnIndexOrThrow("category");
                int statusIndex = cursor.getColumnIndexOrThrow("read");

                do {
                    int bookId = cursor.getInt(bookIdIndex);
                    String title = cursor.getString(titleIndex);
                    int authorId = cursor.getInt(authorIdIndex);
                    int categId = cursor.getInt(categIdIndex);
                    int status = cursor.getInt(statusIndex);

                    String authorName = authorsDbHelper.getAuthorNameById(authorId);
                    String categName = categsDbHelper.getCategoryNameById(categId);

                    bookTitles.add(new Book(bookId, title, authorName, status, categName));
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
					booksListTitle.setText(R.string.books);
					booksCountBadge.setText(String.valueOf(bookTitles.size()));

					if (bookTitles.isEmpty()) {
						showNoBooksDialog();
					} else if (bookAdapter != null) {
						bookAdapter.notifyDataSetChanged();
					}
				}
			});
    }

    private void showNoBooksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.books);
        builder.setMessage("No books found. Would you like to add a new one?");
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(BooksActivity.this, AddBookActivity.class));
				}
			});
        builder.setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void updateBadges() {
        if (booksCountBadge == null) return;

        new Thread(new Runnable() {
				@Override
				public void run() {
					final int booksCount = dbHelper.getBooksCount();
					final int sumrysCount = smHelper.getSummariesCount();

					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								TextView sumrysBadge = findViewById(R.id.sumrysCountBadge);
								if (sumrysBadge != null) {
									sumrysBadge.setText(String.valueOf(sumrysCount));
								}
								booksCountBadge.setText(String.valueOf(booksCount));
							}
						});
				}
			}).start();
    }

    private void openBookReview(Book book) {
        Intent intent = new Intent(this, BookReviewActivity.class);
        intent.putExtra("selectedBook", book);
        startActivity(intent);
    }

    @Override
	protected void onDestroy() {
		// Close database helpers after backup if needed
		// Consider if these need to stay open for backup
		if (dbHelper != null) {
			dbHelper.close();
		}
		if (smHelper != null) {
			smHelper.close();
		}
		if (categsDbHelper != null) {
			categsDbHelper.close();
		}
		if (authorsDbHelper != null) {
			authorsDbHelper.close();
		}
		
		super.onDestroy();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        loadBooks();
        updateBadges();
        setupHeaderNavigation("BOOKS", this);
    }
}
