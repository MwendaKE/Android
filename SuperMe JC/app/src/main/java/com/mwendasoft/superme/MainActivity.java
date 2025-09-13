package com.mwendasoft.superme;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.graphics.*;
import android.view.*;
import android.content.*;
import android.database.sqlite.*;
import android.database.*;
import java.util.*;
import android.graphics.drawable.*;
import android.support.v4.content.*;
import android.*;
import android.content.pm.*;
import android.support.v4.app.*;
import android.util.*;
import com.mwendasoft.superme.books.*;
import com.mwendasoft.superme.quotes.*;
import com.mwendasoft.superme.clips.*;
import com.mwendasoft.superme.music.*;
import com.mwendasoft.superme.tasks.*;
import com.mwendasoft.superme.events.*;
import com.mwendasoft.superme.diaries.*;
import com.mwendasoft.superme.poems.*;
import com.mwendasoft.superme.articles.*;
import com.mwendasoft.superme.sparks.*;
import com.mwendasoft.superme.notes.*;
import com.mwendasoft.superme.people.*;
import com.mwendasoft.superme.core.*;
import com.mwendasoft.superme.*;
import java.io.*;
import com.mwendasoft.superme.authors.*;
import android.support.v4.widget.*;
import android.support.v4.view.*;
import com.mwendasoft.superme.sumrys.*;
import com.mwendasoft.superme.categories.*;
import java.security.*;
import com.mwendasoft.superme.helpers.*;
import android.text.*;

public class MainActivity extends BaseActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String PREF_NAME = "superme_prefs";
    private static final String INSTALLED_KEY = "app_installed";
	private SharedPreferences userPrefs;
	
    // UI Components
    private EditText searchInput;
    private ImageButton searchImageButton;
	private LinearLayout sideMenu, sideNavAuthors, sideNavCategs, sideNavAbout;
	private ImageButton toggleSideMenuBtn;
	private DrawerLayout drawerMainLayout;
	private TextView sideNavBooksCountBadge, 
	                 sideNavNotesCountBadge, 
	                 sideNavQuotesCountBadge,
					 sideNavDiaryCountBadge, 
					 sideNavEventsCountBadge,
					 sideNavTasksCountBadge,
					 sideNavArtsCountBadge,
					 sideNavSparksCountBadge,
					 sideNavMusicCountBadge,
					 sideNavPoemsCountBadge,
					 sideNavClipsCountBadge,
					 sideNavPeopleCountBadge,
	                 sideNavAuthorsCountBadge,
					 sideNavCategsCountBadge;
					 
    // Database helpers
    private BooksDBHelper booksDbHelper;
	private NotesDBHelper notesDbHelper;
    private QuotesDBHelper quotesDbHelper;
	private DiariesDBHelper diaryDbHelper;
	private TasksDBHelper tasksDbHelper;
    private EventsDBHelper eventsDbHelper;
	private ArticlesDBHelper artsDbHelper;
	private SongsDBHelper songsDbHelper;
	private PoemsDBHelper poemsDbHelper;
    private ClipsDBHelper clipsDbHelper;
	private PeopleDBHelper peopleDbHelper;
	private SparksDBHelper sparksDbHelper;
	private AuthorsDBHelper authorsDbHelper;
	private SumrysDBHelper sumrysDbHelper;
    private CategoriesDBHelper categsDbHelper;
	
    // Layout click listeners map
    private final Map<Integer, Class<?>> layoutActivityMap = new HashMap<Integer, Class<?>>();

	// Checkbox containers
	private ArrayList<String> checkedList = new ArrayList<>();
	private ArrayList<CheckBox> otherCheckboxes = new ArrayList<>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialize the layout to activity mapping
        initializeLayoutMap();

        initializeComponents();
        setupDatabase();
        checkAndRequestPermissions();
        setupSearchFunctionality();
		setupSideMenuFunctionality();
        setupLayoutClickListeners();
        setAppFont();
		
		setUpUserPrefs();
    }
	
	private void initializeLayoutMap() {
        layoutActivityMap.put(R.id.booksLayout, BooksActivity.class);
        layoutActivityMap.put(R.id.quotesLayout, QuotesActivity.class);
        layoutActivityMap.put(R.id.clipsLayout, ClipsActivity.class);
        layoutActivityMap.put(R.id.eventsLayout, EventsActivity.class);
        layoutActivityMap.put(R.id.tasksLayout, TasksActivity.class);
        layoutActivityMap.put(R.id.diaryLayout, DiaryActivity.class);
        layoutActivityMap.put(R.id.musicLayout, MusicActivity.class);
        layoutActivityMap.put(R.id.poemLayout, PoemActivity.class);
        layoutActivityMap.put(R.id.artsLayout, ArticlesActivity.class);
        layoutActivityMap.put(R.id.sparksLayout, SparksActivity.class);
        layoutActivityMap.put(R.id.notesLayout, NotesActivity.class);
        layoutActivityMap.put(R.id.peopleLayout, PeopleActivity.class);
    }

    private void initializeComponents() {
        searchInput = findViewById(R.id.searchEditText);
        searchImageButton = findViewById(R.id.searchImageButton);
		
		// SIDE MENU
		sideMenu = findViewById(R.id.sidebar_layout);
		toggleSideMenuBtn = findViewById(R.id.toggle_sidebar);
		drawerMainLayout = findViewById(R.id.drawerMainLayout);
		
		//--
		
		sideNavAuthors = findViewById(R.id.sideNavAuthors);
		sideNavCategs = findViewById(R.id.sideNavCategs);
		sideNavAbout = findViewById(R.id.sideNavAbout);
		
		//--
		sideNavBooksCountBadge = findViewById(R.id.sideNavBooksCountBadge);
		sideNavNotesCountBadge = findViewById(R.id.sideNavNotesCountBadge);
		sideNavQuotesCountBadge = findViewById(R.id.sideNavQuotesCountBadge);
		sideNavDiaryCountBadge = findViewById(R.id.sideNavDiaryCountBadge);
		sideNavEventsCountBadge = findViewById(R.id.sideNavEventsCountBadge);
		
		sideNavTasksCountBadge = findViewById(R.id.sideNavTasksCountBadge);
		sideNavArtsCountBadge = findViewById(R.id.sideNavArtsCountBadge);
		sideNavSparksCountBadge = findViewById(R.id.sideNavSparksCountBadge);
		sideNavMusicCountBadge = findViewById(R.id.sideNavMusicCountBadge);
		sideNavPoemsCountBadge = findViewById(R.id.sideNavPoemsCountBadge);
		
		sideNavClipsCountBadge = findViewById(R.id.sideNavClipsCountBadge);
		sideNavPeopleCountBadge = findViewById(R.id.sideNavPeopleCountBadge);
		
		sideNavAuthorsCountBadge = findViewById(R.id.sideNavAuthorsCountBadge);
		sideNavCategsCountBadge = findViewById(R.id.sideNavCategsCountBadge);
	}
	
	private void setUpUserPrefs() {
		userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
		new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					boolean hasPassword = !userPrefs.getString("user_password", "").isEmpty();
					if (hasPassword) {
						showLoginDialog();
					} else {
						showSignUpDialog();
					}
				}
			}, 1000); // 1-second delay
    }
	
    private void setupDatabase() {
        // Create app database & tables
        SuperMeDBHelper dbHelper = new SuperMeDBHelper(this);
        dbHelper.getWritableDatabase(); // Force database creation

		/*
		// Restore database
		SuperMeDBTransfer dbTransferHelper = new SuperMeDBTransfer(this);
		dbTransferHelper.checkAndMoveDatabase(this);
        dbTransferHelper.transferDataIfEmpty(this);
		*/
		
        // Initialize DB helpers
        booksDbHelper = new BooksDBHelper(this);
        quotesDbHelper = new QuotesDBHelper(this);
        clipsDbHelper = new ClipsDBHelper(this);
        songsDbHelper = new SongsDBHelper(this);
        tasksDbHelper = new TasksDBHelper(this);
        eventsDbHelper = new EventsDBHelper(this);
		notesDbHelper = new NotesDBHelper(this);
		peopleDbHelper = new PeopleDBHelper(this);
		artsDbHelper = new ArticlesDBHelper(this);
		diaryDbHelper = new DiariesDBHelper(this);
		poemsDbHelper = new PoemsDBHelper(this);
		authorsDbHelper = new AuthorsDBHelper(this);
		sparksDbHelper = new SparksDBHelper(this);
		sumrysDbHelper = new SumrysDBHelper(this);
		categsDbHelper = new CategoriesDBHelper(this);
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
											  new String[]{
												  Manifest.permission.WRITE_EXTERNAL_STORAGE,
												  Manifest.permission.READ_EXTERNAL_STORAGE
											  },
											  STORAGE_PERMISSION_CODE);
        } else {
            tryRestoreIfFreshInstall();
        }
    }

    private void setupSearchFunctionality() {
        searchImageButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String keyword = searchInput.getText().toString().trim();
					if (keyword.isEmpty()) {
						Toast.makeText(MainActivity.this, "Please enter a topic to search", Toast.LENGTH_SHORT).show();
					} else {
						showSearchOptionsDialog();
					}
				}
			});
    }
	
	private void setupSideMenuFunctionality() {
		// MAIN
		int booksCount = booksDbHelper.getBooksCount() + sumrysDbHelper.getSummariesCount();
		int authorsCount = authorsDbHelper.getAuthorsCount() + clipsDbHelper.getClipsWriterCount() + sumrysDbHelper.getSummariesAuthorCount();
		
		sideNavBooksCountBadge.setText(String.valueOf(booksCount));
		sideNavNotesCountBadge.setText(String.valueOf(notesDbHelper.getNotesCount()));
		sideNavQuotesCountBadge.setText(String.valueOf(quotesDbHelper.getQuotesCount()));
		sideNavEventsCountBadge.setText(String.valueOf(eventsDbHelper.getEventsCount()));
		sideNavDiaryCountBadge.setText(String.valueOf(diaryDbHelper.getEntryCount()));
		
		sideNavTasksCountBadge.setText(String.valueOf(tasksDbHelper.getTasksCount()));
		sideNavArtsCountBadge.setText(String.valueOf(artsDbHelper.getArticlesCount()));
		sideNavSparksCountBadge.setText(String.valueOf(sparksDbHelper.getVideoCount()));
		sideNavMusicCountBadge.setText(String.valueOf(songsDbHelper.getSongsCount()));
		sideNavPoemsCountBadge.setText(String.valueOf(poemsDbHelper.getPoemsCount()));
		
		sideNavClipsCountBadge.setText(String.valueOf(clipsDbHelper.getClipsCount()));
		sideNavPeopleCountBadge.setText(String.valueOf(peopleDbHelper.getPeopleCount()));
		
		// GROUPS
		
		sideNavAuthorsCountBadge.setText(String.valueOf(authorsCount));
		sideNavCategsCountBadge.setText(String.valueOf(categsDbHelper.getCategoriesCount()));
		
		// BEHAVIOR
		
		sideNavAuthors.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, AuthorsActivity.class);
					startActivity(intent);
				}
			});
			
		sideNavCategs.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
					startActivity(intent);
				}
			});
			
		sideNavAbout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, AboutActivity.class);
					startActivity(intent);
				}
			});
		
		
		toggleSideMenuBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (drawerMainLayout.isDrawerOpen(GravityCompat.START)) {
						drawerMainLayout.closeDrawer(GravityCompat.START);
					} else {
						drawerMainLayout.openDrawer(GravityCompat.START);
					}
				}
			});
	}

    private void setupLayoutClickListeners() {
        for (final Map.Entry<Integer, Class<?>> entry : layoutActivityMap.entrySet()) {
            View layout = findViewById(entry.getKey());
            if (layout != null) {
                layout.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(MainActivity.this, entry.getValue());
							startActivity(intent);
						}
					});
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                tryRestoreIfFreshInstall();
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot restore backup.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tryRestoreIfFreshInstall() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean installedBefore = prefs.getBoolean(INSTALLED_KEY, false);

        if (!installedBefore) {
            boolean restored = SuperMeApplication.getDbBackupManager().restoreIfFreshInstall();
            if (restored) {
                Toast.makeText(this, "Database restored from backup", Toast.LENGTH_LONG).show();
                prefs.edit().putBoolean(INSTALLED_KEY, true).apply();
            } else {
                Toast.makeText(this, "No backup found to restore", Toast.LENGTH_SHORT).show();
            }
        }
    }
	
	//#####
	
	private void showLoginDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("App Login");

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_sign_in, null);

		final EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
		final TextView hintText = dialogView.findViewById(R.id.passwordHint);

		// Load saved hint from SharedPreferences
		String savedHint = userPrefs.getString("user_password_hint", "");
		if (!savedHint.isEmpty()) {
			hintText.setText("Hint: " + savedHint);
		} else {
			hintText.setText("");
		}

		builder.setView(dialogView);

		final AlertDialog dialog = builder.create();

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Login", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					dialog.dismiss();
					finish(); // Close app
				}
			});
			
		final ImageButton showPass = dialogView.findViewById(R.id.dialogshowPasswordBtn);
		final boolean[] isVisible = {false};

		showPass.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isVisible[0]) {
						inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						showPass.setImageResource(R.drawable.ic_eye); // 👁️ eye open
						isVisible[0] = false;
					} else {
						inputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
						showPass.setImageResource(R.drawable.ic_eye_off); // 👁️ eye off
						isVisible[0] = true;
					}
					inputPassword.setSelection(inputPassword.getText().length());
				}
			});

		dialog.setCancelable(false);
		dialog.show();

		Button loginButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		loginButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String entered = inputPassword.getText().toString().trim();
					String storedHash = userPrefs.getString("user_password", "");

					if (hashPassword(entered).equals(storedHash)) {
						ToastMessagesManager.show(MainActivity.this, "Login successful");
						dialog.dismiss();
					} else {
						ToastMessagesManager.show(MainActivity.this, "Wrong password");
					}
				}
			});
	}

    private void showSignUpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Register Password");

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_sign_up, null);

		final EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
		final EditText inputHint = dialogView.findViewById(R.id.inputPasswordHint);

		builder.setView(dialogView);

		final AlertDialog dialog = builder.create();

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Register", (DialogInterface.OnClickListener) null);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int which) {
					dialog.dismiss();
					finish(); // Close app
				}
			});
			
		final ImageButton showPass = dialogView.findViewById(R.id.dialogshowPasswordBtn);
		final boolean[] isVisible = {false};

		showPass.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isVisible[0]) {
						// Hide password
						inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
						showPass.setImageResource(R.drawable.ic_eye); // show eye icon
						isVisible[0] = false;
					} else {
						// Show password
						inputPassword.setInputType(InputType.TYPE_CLASS_TEXT);
						showPass.setImageResource(R.drawable.ic_eye_off); // show crossed-eye icon
						isVisible[0] = true;
					}
					inputPassword.setSelection(inputPassword.getText().length()); // keep cursor at end
				}
			});
			
		dialog.setCancelable(false);
		dialog.show();

		Button registerButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		registerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String password = inputPassword.getText().toString().trim();
					String hint = inputHint.getText().toString().trim();

					if (password.isEmpty()) {
						ToastMessagesManager.show(MainActivity.this, "Please enter a password!");
					} else if (hint.isEmpty()) {
						ToastMessagesManager.show(MainActivity.this, "Enter your passowrd hint!");
					} else {
						userPrefs.edit()
							.putString("user_password", hashPassword(password))
							.putString("user_password_hint", hint)
							.apply();

						ToastMessagesManager.show(MainActivity.this, "Password saved successfully.");
						dialog.dismiss();
					}
				}
			});
	}

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            return null;
        }
    }
	
	//#####
	
	private void showSearchOptionsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Search Options");

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.dialog_search_options, null);
		builder.setView(dialogView); // Attach custom layout to dialog

		// Clear previous data
		otherCheckboxes.clear();
		checkedList.clear();

		// Setup checkboxes
		setupAllCheckbox(dialogView);
		addOtherCheckbox(dialogView, R.id.search_books, "Books");
		addOtherCheckbox(dialogView, R.id.search_notes, "Notes");
		addOtherCheckbox(dialogView, R.id.search_quotes, "Quotes");
		addOtherCheckbox(dialogView, R.id.search_diary, "Diary");
		addOtherCheckbox(dialogView, R.id.search_events, "Events");
		addOtherCheckbox(dialogView, R.id.search_tasks, "Tasks");
		addOtherCheckbox(dialogView, R.id.search_arts, "Articles");
		addOtherCheckbox(dialogView, R.id.search_music, "Music");
		addOtherCheckbox(dialogView, R.id.search_poems, "Poems");
		addOtherCheckbox(dialogView, R.id.search_clips, "Clips");
		addOtherCheckbox(dialogView, R.id.search_people, "People");

		// Set buttons using builder
		builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Run performSearch() in a background thread
					new Thread(new Runnable() {
							@Override
							public void run() {
								final List<SearchItem> finalResults = performSearchInBackground();

								// Back to UI thread to show results
								runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (!finalResults.isEmpty()) {
												Intent intent = new Intent(MainActivity.this, SearchViewActivity.class);
												intent.putExtra("results", (Serializable) finalResults);
												startActivity(intent);
											} else {
												Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_SHORT).show();
											}
										}
									});
							}
						}).start();
				}
			});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss(); // Simply close
				}
			});

		// Create and show dialog
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void setupAllCheckbox(View dialog) {
		CheckBox checkAll = dialog.findViewById(R.id.search_all);
		checkAll.setChecked(true);
		checkedList.add("All");

		checkAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						for (CheckBox cb : otherCheckboxes) {
							cb.setChecked(false);
							cb.setEnabled(false);
						}
						checkedList.clear();
						checkedList.add("All");
					} else {
						for (CheckBox cb : otherCheckboxes) {
							cb.setEnabled(true);
						}
						checkedList.remove("All");
					}
				}
			});
	}
	
	private void addOtherCheckbox(View dialog, int checkboxId, final String checkboxText) {
		CheckBox checkBox = dialog.findViewById(checkboxId);
		otherCheckboxes.add(checkBox);

		final CheckBox checkAll = dialog.findViewById(R.id.search_all);

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						if (!checkedList.contains(checkboxText)) {
							checkedList.add(checkboxText);
						}
					} else {
						checkedList.remove(checkboxText);
					}

					// Auto check/uncheck "All"
					if (allOthersChecked() || noneChecked()) {
						if (!checkAll.isChecked()) checkAll.setChecked(true);
					} else {
						if (checkAll.isChecked()) checkAll.setChecked(false);
					}
				}
			});
	}

	private boolean allOthersChecked() {
		for (CheckBox cb : otherCheckboxes) {
			if (!cb.isChecked()) {
				return false;
			}
		}
		return true;
	}

	private boolean noneChecked() {
		for (CheckBox cb : otherCheckboxes) {
			if (cb.isChecked()) {
				return false;
			}
		}
		return true;
	}
	
    private List<SearchItem> performSearchInBackground() {
		List<SearchItem> finalResults = new ArrayList<>();
		String keyword = searchInput.getText().toString().trim();

		if (checkedList.contains("All")) {
			finalResults.addAll(searchBookDetails(keyword));
			finalResults.addAll(searchSumryDetails(keyword));
			finalResults.addAll(searchNotesDetails(keyword));
			finalResults.addAll(searchQuotes(keyword));
			finalResults.addAll(searchDiaryDetails(keyword));
			finalResults.addAll(searchEventDetails(keyword));
			finalResults.addAll(searchTaskDetails(keyword));
			finalResults.addAll(searchArticleDetails(keyword));
			finalResults.addAll(searchSongDetails(keyword));
			finalResults.addAll(searchPoemDetails(keyword));
			finalResults.addAll(searchClips(keyword));
			finalResults.addAll(searchPeopleDetails(keyword));
		} else {
			for (String sectionName : checkedList) {
				switch (sectionName) {
					case "Books":
						finalResults.addAll(searchBookDetails(keyword));
						finalResults.addAll(searchSumryDetails(keyword));
						break;
					case "Notes":
						finalResults.addAll(searchNotesDetails(keyword));
						break;
					case "Quotes":
						finalResults.addAll(searchQuotes(keyword));
						break;
					case "Diary":
						finalResults.addAll(searchDiaryDetails(keyword));
						break;
					case "Events":
						finalResults.addAll(searchEventDetails(keyword));
						break;
					case "Tasks":
						finalResults.addAll(searchTaskDetails(keyword));
						break;
					case "Articles":
						finalResults.addAll(searchArticleDetails(keyword));
						break;
					case "Music":
						finalResults.addAll(searchSongDetails(keyword));
						break;
					case "Poems":
						finalResults.addAll(searchPoemDetails(keyword));
						break;
					case "Clips":
						finalResults.addAll(searchClips(keyword));
						break;
					case "People":
						finalResults.addAll(searchPeopleDetails(keyword));
						break;
				}
			}
		}
		return finalResults;
	}
	
	//########
	
	private List<SearchItem> searchClips(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT clip, writer, source FROM clips";

        Cursor cursor = clipsDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String clip = cursor.getString(0); 
                String writer = cursor.getString(1); 
                String source = cursor.getString(2);

                if (clip.toLowerCase().contains(keyword.toLowerCase())) {
                    results.add(new SearchItem(clip.trim(), writer, source));
                }
            }
      
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchQuotes(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT quote, author FROM quotes";

        Cursor cursor = quotesDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String quote = cursor.getString(0); 
                int authorId = cursor.getInt(1);
				String authorName = quotesDbHelper.getAuthorById(authorId);
				
                
                if (quote.toLowerCase().contains(keyword.toLowerCase())) {
                    results.add(new SearchItem(quote.trim(), authorName, "Quotes"));
                }
            }

        } finally {
            cursor.close();
        }
        return results;
	}
	
    private List<SearchItem> searchBookDetails(String keyword) {
        List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, author, review FROM books";

        Cursor cursor = booksDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                int authorId = cursor.getInt(1); 
				String fullReview = cursor.getString(2);

				String authorName = authorsDbHelper.getAuthorNameById(authorId);
                
                if (fullReview != null) {
                    String[] paragraphs = fullReview.split("\n\n");
                    for (String paragraph : paragraphs) {
                        if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
                            results.add(new SearchItem(paragraph.trim(), authorName, title));
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return results;
    }
	
	private List<SearchItem> searchSumryDetails(String keyword) {
        List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, summary FROM summaries";

        Cursor cursor = booksDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
				String fullReview = cursor.getString(1);

                if (fullReview != null) {
                    String[] paragraphs = fullReview.split("\n\n");
                    for (String paragraph : paragraphs) {
                        if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
                            results.add(new SearchItem(paragraph.trim(), title, "Sumrys"));
                        }
                    }
                }
            }
        } finally {
            cursor.close();
        }
        return results;
    }

	private List<SearchItem> searchEventDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, notes FROM events";

        Cursor cursor = eventsDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                String fullNote = cursor.getString(1);

                if (fullNote != null) {
					String[] paragraphs = fullNote.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Events"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchDiaryDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, description FROM diaries";

        Cursor cursor = diaryDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                String fullDescription = cursor.getString(1);

                if (fullDescription != null) {
					String[] paragraphs = fullDescription.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Diaries"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchNotesDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, notes FROM notes";

        Cursor cursor = notesDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                String fullNotes = cursor.getString(1);

                if (fullNotes != null) {
					String[] paragraphs = fullNotes.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Notes"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchPoemDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, poem FROM poems";

        Cursor cursor = poemsDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                String fullPoem = cursor.getString(1);

                if (fullPoem != null) {
					String[] paragraphs = fullPoem.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Poems"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchSongDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, lyrics FROM songs";

        Cursor cursor = songsDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
                String fullLyrics = cursor.getString(1);

                if (fullLyrics != null) {
					String[] paragraphs = fullLyrics.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Music"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchPeopleDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT name, occupation, description FROM people";

        Cursor cursor = peopleDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
				String occupation = cursor.getString(1);
                String fullDescription = cursor.getString(2);

                if (fullDescription != null) {
					String[] paragraphs = fullDescription.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, occupation));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchArticleDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, body FROM articles";

        Cursor cursor = artsDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
				String fullArticle = cursor.getString(1);
                
                if (fullArticle != null) {
					String[] paragraphs = fullArticle.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Arts"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}
	
	private List<SearchItem> searchTaskDetails(String keyword) {
		List<SearchItem> results = new ArrayList<>();
        String query = "SELECT title, description FROM tasks";

        Cursor cursor = tasksDbHelper.getDatabase().rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0); 
				String fullDescription = cursor.getString(1);

                if (fullDescription != null) {
					String[] paragraphs = fullDescription.split("\\n{2,}");
					for (String paragraph : paragraphs) {
						if (paragraph.toLowerCase().contains(keyword.toLowerCase())) {
							results.add(new SearchItem(paragraph.trim(), title, "Tasks"));
						}
					}
				}
            }
        } finally {
            cursor.close();
        }
        return results;
	}

    private void setAppFont() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/angelos.ttf");
        int[] textViewIds = {
            R.id.booksTextView, R.id.musicTextView, R.id.quotesTextView,
            R.id.diaryTextView, R.id.clipsTextView, R.id.poemsTextView,
            R.id.tasksTextView, R.id.artsTextView, R.id.eventsTextView,
            R.id.peopleTextView, R.id.notesTextView, R.id.sparksTextView
        };

        for (int id : textViewIds) {
            TextView textView = findViewById(id);
            if (textView != null) {
                textView.setTypeface(typeface);
				textView.setTextColor(Color.BLACK);
            }
        }
    }
}
