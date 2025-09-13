package com.mwendasoft.superme.quotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.content.res.*;
import java.util.*;
import android.content.ClipboardManager;
import android.text.InputType;
import com.mwendasoft.superme.authors.*;
import android.graphics.*;
import com.mwendasoft.superme.helpers.*;

public class QuotesActivity extends BaseActivity {
	private ImageButton addQuoteFab;
    private QuotesDBHelper dbHelper;
	private AuthorsDBHelper authorsDbHelper;
    private ListView quotesListView;
	private TextView quotesListTitle, quotesCountBadge;
    private ArrayList<Quote> quotes;
	private QuotesListAdapter quotesAdapter;
	
	private HashMap<String, Integer> dialogAuthorMap;
	private ArrayList<String> dialogAuthorList;
	private ArrayAdapter<String> dialogAuthorAdapter;
	private Spinner dialogAuthorSpinner;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quotes_activity);
		
		// == REGISTER FOR CONTEXT MENU == //
        quotesListView = findViewById(R.id.quotesListView);
		registerForContextMenu(quotesListView);
		// == //
		
		quotesListTitle = findViewById(R.id.quotesListTitle);
		quotesCountBadge = findViewById(R.id.quotesCountBadge);
		addQuoteFab = findViewById(R.id.addQuoteFab);
		
        dbHelper = new QuotesDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
		
        quotes = new ArrayList<>(); // Initialize the list
		quotesAdapter = new QuotesListAdapter(this, quotes);
		quotesListView.setAdapter(quotesAdapter);
		
	    dialogAuthorMap = new HashMap<>();
	    dialogAuthorList = new ArrayList<>();
	    dialogAuthorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dialogAuthorList);
		
		loadQuotes();
		loadDialogAuthors();
		
		addQuoteFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddQuoteDialog();
			}
				
		});
    }
	
	// ==== CONTEXT MENU METHODS (CMMs) === //

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.quotesListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Choose an action");

			menu.add(Menu.NONE, 1, 1, "Share");
			menu.add(Menu.NONE, 2, 2, "Copy");
			menu.add(Menu.NONE, 3, 3, "Edit");
			menu.add(Menu.NONE, 4, 4, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Quote selectedQuote = quotes.get(info.position);

		switch (item.getItemId()) {
			case 1: // Share
				shareQuote(selectedQuote);
				return true;
			case 4: // Delete
				confirmAndDeleteQuote((selectedQuote));
				return true;
			case 3: // Edit
				showEditDialog(selectedQuote);
				return true;
			case 2: // Copy
				copyQuoteText(selectedQuote);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmAndDeleteQuote(final Quote quote) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Quote")
			.setMessage("Are you sure you want to delete this quote?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dbHelper.deleteQuote(quote.getAuthorId());
					quotes.remove(quote);
					quotesAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}

	private boolean shareQuote(Quote quote) {
		String shareText = quote.getQuoteText() + " ~ " + quote.getAuthorName();
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(shareIntent, "Share via"));
		return true;
	}
	
	private boolean copyQuoteText(Quote quote) {
		String copyText = quote.getQuoteText() + " ~ " + quote.getAuthorName();

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Quote Text", copyText);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
		return true;
	}

	private void showAddQuoteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("New Quote");

		// Inflate the layout
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_add_quote, null);
		
		final EditText quoteInput = dialogView.findViewById(R.id.dialogQuoteAddInput);
		
		//##
		ImageButton addAuthorButton = dialogView.findViewById(R.id.dialogAddQuoteAuthorBtn);
		dialogAuthorSpinner = dialogView.findViewById(R.id.dialogQuoteAddAuthorSpinner);
		dialogAuthorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dialogAuthorSpinner.setAdapter(dialogAuthorAdapter);

		addAuthorButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog(dialogAuthorSpinner);
				}
			});

		//##
		
		builder.setView(dialogView);
		
		final AlertDialog dialog = builder.create();

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					dialog.dismiss();
				}
			});

		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		saveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String quote = quoteInput.getText().toString().trim();
					String selectedAuthor = (String) dialogAuthorSpinner.getSelectedItem();
					int authorId = dialogAuthorMap.get(selectedAuthor);

					if (quote.isEmpty() || authorId == -1) {
						ToastMessagesManager.show(QuotesActivity.this, "Please enter a quote and select a valid author!");
						return;
					}

					ContentValues values = new ContentValues();
					values.put("quote", quote);
					values.put("author", authorId);

					long rowId = dbHelper.insertQuote(values);

					if (rowId != -1) {
						loadQuotes();
						ToastMessagesManager.show(QuotesActivity.this, "Added successfully!");
						dialog.dismiss(); // Close dialog only after success
					} else {
						ToastMessagesManager.show(QuotesActivity.this, "Failed to add quote.");
					}
				}
			});
	}
	
	private void showAddAuthorDialog(final Spinner dialogAuthorSpinner) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("New Author");

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_add_author, null);

		final EditText authorNameInput = dialogView.findViewById(R.id.diaogAddAuthorName);
		final Spinner authorCategorySpinner = dialogView.findViewById(R.id.dialogAddAuthorCategorySpinner);

		String[] categories = {"Novelist", "Playwright", "Songwriter", "Poet", "Other"};
		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
		categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		authorCategorySpinner.setAdapter(categoryAdapter);

		builder.setView(dialogView);

		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String authorName = authorNameInput.getText().toString().trim();
					String authorCategory = authorCategorySpinner.getSelectedItem().toString();

					if (!authorName.isEmpty()) {
						Cursor cursor = authorsDbHelper.getAuthorByName(authorName);
						if (cursor != null) {
							if (cursor.getCount() > 0) {
								ToastMessagesManager.show(QuotesActivity.this, "Author already exists!");
							} else {
								authorsDbHelper.insertAuthor(authorName, authorCategory);
								int authorId = authorsDbHelper.getAuthorIdByName(authorName);
								dialogAuthorList.add(authorName);
								dialogAuthorMap.put(authorName, authorId);
								dialogAuthorAdapter.notifyDataSetChanged();

								int newIndex = dialogAuthorList.indexOf(authorName);
								if (newIndex != -1) {
									dialogAuthorSpinner.setSelection(newIndex);
								}
							}
							cursor.close();
						}
					}
				}
			});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});

		builder.setCancelable(false);
		builder.show();
	}
	
	private void showEditDialog(final Quote quote) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Quote");

		// Inflate the layout
		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_edit_quote, null);

		// Get references to views
		final EditText quoteInput = dialogView.findViewById(R.id.dialogQuoteEditInput);
		
		//##
		ImageButton addAuthorButton = dialogView.findViewById(R.id.dialogEditQuoteAuthorBtn);
		dialogAuthorSpinner = dialogView.findViewById(R.id.dialogQuoteEditAuthorSpinner);
		dialogAuthorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dialogAuthorSpinner.setAdapter(dialogAuthorAdapter);

		addAuthorButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddAuthorDialog(dialogAuthorSpinner);
				}
			});
	
		//##
		
		// Set initial values
		quoteInput.setText(quote.getQuoteText());
		
		// Author Spinner update
		String authorName = getKeyByValue(dialogAuthorMap, quote.getAuthorId());
		int authorPos = dialogAuthorAdapter.getPosition(authorName);
		dialogAuthorSpinner.setSelection(authorPos);
		//##
		
        builder.setView(dialogView);

		// Create the dialog first
		final AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					dialog.dismiss();
				}
			});

		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		saveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String newQuoteText = quoteInput.getText().toString().trim();
					String selectedAuthor = (String) dialogAuthorSpinner.getSelectedItem();
					int authorId = dialogAuthorMap.get(selectedAuthor);
					
					if (newQuoteText.isEmpty() || authorId == -1) {
						ToastMessagesManager.show(QuotesActivity.this, "Please enter a quote and select a valid author!");
						return;
					}

					ContentValues values = new ContentValues();
					values.put("quote", newQuoteText);
					values.put("author", authorId);
					
					int rowsUpdated = dbHelper.updateQuote(quote.getId(), values);

					if (rowsUpdated > 0) {
						loadQuotes();
						ToastMessagesManager.show(QuotesActivity.this, "Added successfully.");
						dialog.dismiss(); 
					} else {
						ToastMessagesManager.show(QuotesActivity.this, "Failed to update quote!");
					}
				}
			});
	}
	
    private void loadQuotes() {
		quotes.clear();
		
        Cursor cursor = dbHelper.getAllQuotes();

		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				String quote = cursor.getString(cursor.getColumnIndex("quote"));
				int authorId = cursor.getInt(cursor.getColumnIndex("author")); // Optional, if you still need it
				String authorName = cursor.getString(cursor.getColumnIndex("author_name")); // From the JOIN

				if (quote != null && !quote.isEmpty()) {
					quotes.add(new Quote(id, quote, authorId, authorName));
				}
			}
			cursor.close();
		}

		quotesListTitle.setText(R.string.quotes);
		quotesCountBadge.setText(String.valueOf(quotes.size()));

        if (quotes.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.quotes);
			builder.setMessage("No quotes found. Would you like to add a new one?");
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showAddQuoteDialog();
					}
				});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();

		} else {
			quotesAdapter.notifyDataSetChanged();
		}
    }
	
	private void loadDialogAuthors() {
		// Populate spinner
		dialogAuthorList.add("--Select an author--");
		dialogAuthorMap.put("--Select an author--", -1);

		Cursor cursor = authorsDbHelper.getAllAuthorsWithIds();
		if (cursor != null && cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				String name = cursor.getString(cursor.getColumnIndex("author_name"));
				dialogAuthorList.add(name);
				dialogAuthorMap.put(name, id);
			} while (cursor.moveToNext());
			cursor.close();
		} else {
			dialogAuthorList.add("No Authors Available");
			dialogAuthorMap.put("No Authors Available", -1);
		}
	}
	
	private String getKeyByValue(HashMap<String, Integer> map, int value) {
		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}
	
    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
