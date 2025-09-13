package com.mwendasoft.superme.books;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.graphics.*;
import android.content.*;
import android.view.*;
import com.mwendasoft.superme.authors.*;
import android.text.*;
import android.content.ClipboardManager;
import android.text.style.*;
import android.support.v4.content.*;

public class BookReviewActivity extends BaseActivity {
    private BooksDBHelper dbHelper;
	private AuthorsDBHelper authorsDbHelper;
    private ListView reviewListView;
	private TextView bookReviewTitle;
	private EditText searchBookReviewEdit;
	private ImageButton editReviewFab;
    private ArrayList<String> reviewSections;
	private BookReviewAdapter reviewAdapter;
	
	private SharedPreferences sharedPrefs;
	
	private Book selectedBook;
	private String formattedBookPosTitle;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_review_activity);
		
		setAppFont();
		
		bookReviewTitle = findViewById(R.id.bookReviewTitle);
		editReviewFab = findViewById(R.id.editReviewFab);
		searchBookReviewEdit = findViewById(R.id.searchBookReviewEdit);
		reviewListView = findViewById(R.id.reviewListView);
		
		registerForContextMenu(reviewListView); // == Register for context views == //
		
		reviewSections = new ArrayList<>();
		
		reviewAdapter = new BookReviewAdapter(this, reviewSections);
		reviewListView.setAdapter(reviewAdapter);
		
        dbHelper = new BooksDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
		selectedBook = (Book) getIntent().getSerializableExtra("selectedBook");
		formattedBookPosTitle = "BOOK_" + selectedBook.getId() + "_REVIEW_POS";
		
		loadBook();
		
		//---- Restore Book Review Position -----//
		sharedPrefs = getSharedPreferences("BookReviewPrefs", MODE_PRIVATE);
		int lastReviewPos = sharedPrefs.getInt(formattedBookPosTitle, 0);
		reviewListView.setSelection(lastReviewPos);
		//----- -------//
		
		editReviewFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BookReviewActivity.this, EditBookActivity.class);
				intent.putExtra("selectedBook", selectedBook);
				startActivity(intent);
			}
		});
		
		searchBookReviewEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				reviewAdapter.filter(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void afterTextChanged(Editable s) {}
		});
    }
	
	// ==== CONTEXT MENU METHODS (CMMs) === //
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.reviewListView) {
			menu.setHeaderTitle("Choose an action");
			menu.add(Menu.NONE, 1, 1, "Share");
			menu.add(Menu.NONE, 2, 2, "Copy");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		//String selectedText = bookReviewList.get(info.position);
		String selectedText = reviewSections.get(info.position);
		
		switch (item.getItemId()) {
			case 1: // Share
			    String shareText = selectedText + ", " + selectedBook.getTitle() + ", " + selectedBook.getAuthor();
				
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
				startActivity(Intent.createChooser(shareIntent, "Share via"));
				return true;

			case 2: // Copy
			    String copyText = selectedText + ", " + selectedBook.getTitle() + ", " + selectedBook.getAuthor();
				
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("Book Review", copyText);
				clipboard.setPrimaryClip(clip);
				Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
				return true;

			default:
				return super.onContextItemSelected(item);
		}
	}
	
	// ==== CMMs === //
	
    private void loadBook() {
		reviewSections.clear();
		String title = "Unknown", authorName = "Unknown";
		
        Cursor cursor = dbHelper.getBookById(selectedBook.getId());
        if (cursor != null && cursor.moveToFirst()) {
			title = cursor.getString(cursor.getColumnIndex("title"));
			
			int authorId = cursor.getInt(cursor.getColumnIndex("author"));
			authorName = authorsDbHelper.getAuthorNameById(authorId);
            
			String review = cursor.getString(cursor.getColumnIndex("review"));
            
			if (review != null && !review.isEmpty()) {
                String[] sections = review.split("\n\n");
                for (String section : sections) {
                    reviewSections.add(section.trim());
                }
            }
            cursor.close();
        }

        if (reviewSections.isEmpty()) {
            showDialog("No review found for: " + selectedBook.getTitle() + " :by: " + selectedBook.getAuthor());
			
        } else {
			// Styled title and author
			String separator = " | ";
			String fullText = title + separator + authorName;

			SpannableString spannable = new SpannableString(fullText);
			
			// Style for the title (green, bold)
			spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.customForestGreen)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			// Style for the separator (" | ") (blue)
			int sepStart = title.length();
			int sepEnd = sepStart + separator.length();
			spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.customMidnightBlack)), sepStart, sepEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			// Style for authorName (blue, italic)
			int authorStart = sepEnd;
			spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.customOchre)), authorStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			spannable.setSpan(new StyleSpan(Typeface.ITALIC), authorStart, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			// Set it to the TextView
			bookReviewTitle.setText(spannable);
			reviewAdapter.notifyDataSetChanged();
        }
		
		reviewAdapter.updateData(reviewSections);
		
    }
	
	public void setAppFont() {
		try {
			Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/angelos.ttf");
			if (bookReviewTitle != null) {
				bookReviewTitle.setTypeface(typeface);
			}
		} catch (Exception e) {
			showDialog("Font Error: " + e.getMessage());
		}
	}

    private void showDialog(String message) {
        new AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

	@Override
	protected void onPause() {
		super.onPause();
		//----- Save Current Book Review Pos ----//
		int currentReviewPos = reviewListView.getFirstVisiblePosition();
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt(formattedBookPosTitle, currentReviewPos);
		editor.apply();
		//------- ------//
	}
	
	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadBook();
	}
}
