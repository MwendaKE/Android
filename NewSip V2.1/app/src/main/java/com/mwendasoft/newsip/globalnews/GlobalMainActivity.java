package com.mwendasoft.newsip.globalnews;

import android.app.*;
import android.os.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import android.content.*;
import com.mwendasoft.newsip.*;
import com.mwendasoft.newsip.newshelpers.*;

public class GlobalMainActivity extends BaseActivity {
    EditText searchInput;
    ImageButton searchButton, btnPrev, btnNext;
    TextView pageText;
    RecyclerView resultsRecyclerView;
	
	LinearLayout paginationBar;

    List<GlobalNewsItem> newsList;
    GlobalNewsAdapter newsAdapter;
    LoadingDialog loadingDialog;

	private int totalPages = Integer.MAX_VALUE; // default large, update later
    private int currentPage = 1;
    private final int pageSize = 10;
    private String currentQuery = null;

    private final String API_KEY = "e40e33cd336244b2b45a5d87dca23b58";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_main);
        showHomeIcon(MainActivity.class);
		updateTabHighlighting(R.id.navGlobal);
		
        loadingDialog = new LoadingDialog(this);

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchImageButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        addRecyclerViewLayout(resultsRecyclerView);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        pageText = findViewById(R.id.pageText);
		paginationBar = findViewById(R.id.paginationBar);

        newsList = new ArrayList<>();
        newsAdapter = new GlobalNewsAdapter(newsList, this);
        resultsRecyclerView.setAdapter(newsAdapter);

        // ✅ Fetch first page of top headlines
        if (isNetworkAvailable()) {
            fetchNews(currentPage, null); // null means top headlines
        } else {
            showNoInternetDialog(new Runnable() {
					@Override
					public void run() {
						fetchNews(currentPage, null);
					}
				});
        }

        // 🔍 Search button click
        searchButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final String query = searchInput.getText().toString().trim();
					if (!query.isEmpty()) {
						currentQuery = query; // update current query
						currentPage = 1; // reset to page 1
						if (isNetworkAvailable()) {
							fetchNews(currentPage, currentQuery);
						} else {
							showNoInternetDialog(new Runnable() {
									@Override
									public void run() {
										fetchNews(currentPage, currentQuery);
									}
								});
						}
					}
				}
			});

        // ⬅️ Previous page
        btnPrev.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (currentPage > 1) {
						currentPage--;
						fetchNews(currentPage, currentQuery);
					}
				}
			});

        // ➡️ Next page
        btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					currentPage++;
					fetchNews(currentPage, currentQuery);
				}
			});
    }

    // 🧱 Add layout manager and dividers to RecyclerView
    private void addRecyclerViewLayout(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.horizontal_divider));
        recyclerView.addItemDecoration(divider);
    }

    // 🌍 Fetch news from API
    private void fetchNews(final int page, final String query) {
        loadingDialog.show();

        new Thread(new Runnable() {
				@Override
				public void run() {
					final List<GlobalNewsItem> fetchedNews = new ArrayList<>();
					String urlStr;

					try {
						if (query == null) {
							// Top headlines
							urlStr = "https://newsapi.org/v2/top-headlines?language=en&pageSize=" + pageSize + "&page=" + page + "&apiKey=" + API_KEY;
						} else {
							// Search query
							urlStr = "https://newsapi.org/v2/everything?q=" + URLEncoder.encode(query, "UTF-8") +
                                "&language=en&pageSize=" + pageSize + "&page=" + page + "&apiKey=" + API_KEY;
						}

						URL url = new URL(urlStr);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("GET");
						connection.setRequestProperty("User-Agent", "Mozilla/5.0");

						int responseCode = connection.getResponseCode();
						if (responseCode != 200) {
							throw new IOException("Server returned code: " + responseCode);
						}

						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						StringBuilder responseBuilder = new StringBuilder();
						String line;
						while ((line = reader.readLine()) != null) {
							responseBuilder.append(line);
						}
						reader.close();

						JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
						JSONArray articles = jsonResponse.getJSONArray("articles");
						
						// Get news count
						int totalResults = jsonResponse.optInt("totalResults", 0);
						totalPages = (int) Math.ceil((double) totalResults / pageSize);
						
						for (int i = 0; i < articles.length(); i++) {
							JSONObject article = articles.getJSONObject(i);
							String title = article.optString("title", "No Title");
							String description = article.optString("description", "No Description");
							String urlLink = article.optString("url", "");
							String source = article.optJSONObject("source") != null
                                ? article.getJSONObject("source").optString("name", "Unknown Source")
                                : "Unknown Source";
							String author = article.optString("author", "Unknown Author");
							String publishedAt = article.optString("publishedAt", "");

							fetchedNews.add(new GlobalNewsItem(title, description, source, author, publishedAt, urlLink));
						}

						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									loadingDialog.dismiss();

									newsList.clear();
									newsList.addAll(fetchedNews);
									newsAdapter.notifyDataSetChanged();
									
									paginationBar.setVisibility(View.VISIBLE);

									// ✅ Enable "Previous" only if page > 1
									btnPrev.setEnabled(currentPage > 1);

									// ✅ Enable "Next" only if we have more pages
									btnNext.setEnabled(currentPage < totalPages);

									// ✅ Update page text to show current/total
									pageText.setText("Page " + page + " / " + totalPages);
								}
							});

					} catch (final Exception e) {
						e.printStackTrace();
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									loadingDialog.dismiss();
									new AlertDialog.Builder(GlobalMainActivity.this)
										.setTitle("Error")
										.setMessage("Failed to fetch news.\n" + e.getMessage())
										.setPositiveButton("OK", null)
										.show();
								}
							});
					}
				}
			}).start();
    }
}
