package com.mwendasoft.newsip.notifiers;

import android.util.Xml;
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
import com.mwendasoft.newsip.localnews.*;
import android.os.*;

public class FetchRSSBackground {
    private static final String[] rssFeeds = {
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
        "https://allafrica.com/tools/headlines/rdf/kenya/headlines.rdf"
    };

    public ArrayList<LocalNewsItem> fetchLatest() {
        try {
            return new FetchTask().execute().get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private class FetchTask extends AsyncTask<Void, Void, ArrayList<LocalNewsItem>> {
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

        private String getShortDescription(String htmlDescription) {
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
}
