package com.mwendasoft.newsip;

import android.app.NotificationChannel; 
import android.app.NotificationManager; 
import android.content.Context; 
import android.content.DialogInterface; 
import android.content.Intent; 
import android.content.SharedPreferences; 
import android.content.res.Configuration; 
import android.graphics.Color; 
import android.net.Uri; 
import android.os.AsyncTask; 
import android.os.Build; 
import android.os.Bundle; 
import android.provider.Settings; 
import android.support.v4.widget.SwipeRefreshLayout; 
import android.support.v7.widget.DividerItemDecoration; 
import android.support.v7.widget.LinearLayoutManager; 
import android.support.v7.widget.RecyclerView; 
import android.text.Editable; 
import android.text.TextWatcher; 
import android.util.Xml; 
import android.view.View; 
import android.widget.EditText; 
import android.widget.ImageButton; 
import android.widget.LinearLayout; 
import android.widget.TextView; 
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser; 
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException; 
import java.io.InputStream; 
import java.net.HttpURLConnection; 
import java.net.URL; 
import java.util.ArrayList; 
import java.util.Collections; 
import java.util.List; 
import java.util.concurrent.CountDownLatch;

import com.mwendasoft.newsip.globalnews.*; 
import com.mwendasoft.newsip.newshelpers.*; 
import com.mwendasoft.newsip.localnews.*; 
import com.mwendasoft.newsip.notifiers.*;

public class MainActivity extends BaseActivity { 
    RecyclerView recyclerView; 
    ArrayList<LocalNewsItem> allNewsList; 
    ArrayList<LocalNewsItem> pageNewsList; 
    ArrayList<LocalNewsItem> filteredNewsList; 
    LocalNewsAdapter adapter; 
    LoadingDialog loadingDialog; 
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton btnPrev, btnNext;
    TextView pageText;
    EditText searchInput;

    LinearLayout paginationBar;

    private boolean isDestroyed = false;
    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalPages = 1;

    String[] rssFeeds = {
        "https://www.standardmedia.co.ke/rss/headlines.php",
        "https://www.capitalfm.co.ke/news/feed/",
        "https://kenyanews.go.ke/feed",
        "https://www.kenyans.co.ke/feeds/news",
        "https://taifaleo.nation.co.ke/feed",
        "https://wildlifedirect.org/feed",
        "https://internewske.org/rss",
        "https://nairobiwire.com/feed",
        "https://diasporamessenger.com/feed/",
        "https://thesharpdaily.com/feed/",
        "https://allafrica.com/tools/headlines/rdf/kenya/headlines.rdf",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		updateTabHighlighting(R.id.navHome);
		
        createEnhancedNotificationChannel();
        NewsCheckerService.schedulePeriodicChecks(this);
        showGlobeIcon(GlobalMainActivity.class);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.newsRecyclerView);
        addRecyclerViewLayout(recyclerView);

        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        pageText = (TextView) findViewById(R.id.pageText);
        searchInput = (EditText) findViewById(R.id.searchInput);

        paginationBar = (LinearLayout) findViewById(R.id.paginationBar);

        allNewsList = new ArrayList<LocalNewsItem>();
        pageNewsList = new ArrayList<LocalNewsItem>();
        filteredNewsList = new ArrayList<LocalNewsItem>();
        adapter = new LocalNewsAdapter(this, pageNewsList);
        recyclerView.setAdapter(adapter);

        loadingDialog = new LoadingDialog(this);
        setupSwipeRefresh();

        searchInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					filterNews(s.toString().toLowerCase().trim());
				}
			});

        if (isNetworkAvailable()) {
            new FetchRSS(false).execute();
        } else {
            showNoInternetDialog(new Runnable() {
					@Override
					public void run() {
						new FetchRSS(false).execute();
					}
				});
        }

        btnPrev.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentPage > 1) {
						currentPage--;
						showPage();
					}
				}
			});

        btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentPage < totalPages) {
						currentPage++;
						showPage();
					}
				}
			});
    }

    private void filterNews(String searchText) {
        filteredNewsList.clear();

        if (searchText.isEmpty()) {
            filteredNewsList.addAll(allNewsList);
        } else {
            for (LocalNewsItem item : allNewsList) {
                if (item.getTitle().toLowerCase().contains(searchText) || 
                    item.getDescription().toLowerCase().contains(searchText)) {
                    filteredNewsList.add(item);
                }
            }
        }

        updatePaginationWithFilteredResults();
    }

    private void updatePaginationWithFilteredResults() {
        ArrayList<LocalNewsItem> listToUse = searchInput.getText().toString().isEmpty() ? 
            allNewsList : filteredNewsList;

        int totalItems = listToUse.size();
        totalPages = (int) Math.ceil((double) totalItems / pageSize);
        currentPage = 1;
        showPage(listToUse);
    }

    private void showPage(ArrayList<LocalNewsItem> newsList) {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, newsList.size());

        pageNewsList.clear();
        pageNewsList.addAll(newsList.subList(start, end));
        adapter.notifyDataSetChanged();

        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
        pageText.setText("Page " + currentPage + " / " + totalPages);
    }

    private void showPage() {
        ArrayList<LocalNewsItem> listToUse = searchInput.getText().toString().isEmpty() ? 
            allNewsList : filteredNewsList;
        showPage(listToUse);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					swipeRefreshLayout.setRefreshing(false);
					if (isNetworkAvailable()) {
						loadingDialog.show();
						new FetchRSS(true).execute();
					} else {
						showNoInternetDialog(new Runnable() {
								@Override
								public void run() {
									new FetchRSS(true).execute();
								}
							});
					}
				}
			});
    }

    private void createEnhancedNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "news_channel",
                "News Updates",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new articles");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100, 300});

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
        super.onDestroy();
    }

    private void addRecyclerViewLayout(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.horizontal_divider));
        recyclerView.addItemDecoration(divider);
    }

    class FetchRSS extends AsyncTask<Void, Void, ArrayList<LocalNewsItem>> {
        private boolean isSwipeRefresh;

        public FetchRSS(boolean isSwipeRefresh) {
            this.isSwipeRefresh = isSwipeRefresh;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isSwipeRefresh && !isDestroyed && loadingDialog != null) {
                loadingDialog.show();
            }
        }

        @Override
        protected ArrayList<LocalNewsItem> doInBackground(Void... voids) {
            final List<LocalNewsItem> allNews = Collections.synchronizedList(new ArrayList<LocalNewsItem>());
            final CountDownLatch latch = new CountDownLatch(rssFeeds.length);

            for (final String urlString : rssFeeds) {
                new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								URL url = new URL(urlString);
								HttpURLConnection connection = (HttpURLConnection) url.openConnection();
								connection.setRequestMethod("GET");
								connection.setRequestProperty("User-Agent", "Mozilla/5.0");
								connection.setConnectTimeout(15000);
								connection.setReadTimeout(15000);
								connection.setInstanceFollowRedirects(true);

								int responseCode = connection.getResponseCode();
								if (responseCode == HttpURLConnection.HTTP_OK) {
									InputStream inputStream = connection.getInputStream();
									parseXML(inputStream, allNews, urlString);
									inputStream.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								latch.countDown();
							}
						}
					}).start();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Collections.shuffle(allNews);
            return new ArrayList<>(allNews);
        }

        private void parseXML(InputStream inputStream, List<LocalNewsItem> allNews, String urlString) {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);

                String title = "", link = "", description = "", pubDate = "";
                String source = new URL(urlString).getHost();
                boolean insideItem = false;

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    int eventType = parser.getEventType();

                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = parser.getName();
                        if (tagName.equalsIgnoreCase("item") || tagName.equalsIgnoreCase("entry")) {
                            insideItem = true;
                            title = "";
                            link = "";
                            description = "";
                            pubDate = "";
                        } else if (insideItem) {
                            if (tagName.equalsIgnoreCase("title")) {
                                title = parser.nextText().trim();
                            } else if (tagName.equalsIgnoreCase("link")) {
                                link = parser.nextText().trim();
                            } else if (tagName.equalsIgnoreCase("description") ||
                                       tagName.equalsIgnoreCase("summary") ||
                                       tagName.equalsIgnoreCase("content:encoded")) {
                                description = getShortDescription(parser.nextText().trim());
                            } else if (tagName.equalsIgnoreCase("pubDate") ||
                                       tagName.equalsIgnoreCase("updated")) {
                                pubDate = parser.nextText().trim();
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        String tagName = parser.getName();
                        if ((tagName.equalsIgnoreCase("item") || tagName.equalsIgnoreCase("entry")) && insideItem) {
                            if (!title.isEmpty() && !link.isEmpty()) {
                                allNews.add(new LocalNewsItem(title, link, description, pubDate, source));
                            }
                            insideItem = false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(ArrayList<LocalNewsItem> result) {
            if (isDestroyed) return;

            try {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                swipeRefreshLayout.setRefreshing(false);

                if (result == null || result.isEmpty()) {
                    showNoInternetDialog(new Runnable() {
							@Override
							public void run() {
								new FetchRSS(false).execute();
							}
						});
                } else {
                    allNewsList.clear();
                    allNewsList.addAll(result);
                    filteredNewsList.clear();
                    filteredNewsList.addAll(result);

                    paginationBar.setVisibility(View.VISIBLE);

                    int totalItems = allNewsList.size();
                    totalPages = (int) Math.ceil((double) totalItems / pageSize);
                    currentPage = 1;

                    showPage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getShortDescription(String htmlDescription) {
        String plainText = android.text.Html.fromHtml(htmlDescription).toString().trim();
        plainText = plainText.replace("\uFFFD", "");
        plainText = plainText.replaceAll("[\\p{C}&&[^\r\n\t]]", "");
        plainText = plainText.replaceAll("\\s+", " ").trim();

        String[] words = plainText.split(" ");
        int maxWords = Math.min(words.length, 50);

        StringBuilder shortText = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            shortText.append(words[i]).append(" ");
        }

        if (words.length > 50) {
            shortText.append("...");
        }

        return shortText.toString().trim();
    }
}
