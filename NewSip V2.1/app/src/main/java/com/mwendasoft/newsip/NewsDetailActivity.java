package com.mwendasoft.newsip;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.mwendasoft.newsip.newshelpers.DatabaseHelper;
import com.mwendasoft.newsip.newshelpers.*;

public class NewsDetailActivity extends BaseActivity {

    private LoadingDialog loadingDialog;
    private WebView webView;

    private String articleTitle;
    private String articleUrl;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_detail_activity);

		// Get article title and URL from intent
		articleTitle = getIntent().getStringExtra("title");
		articleUrl = getIntent().getStringExtra("url");

		if (articleUrl == null || articleUrl.trim().isEmpty()) {
			finish(); // Exit if URL is missing
			return;
		}
		
		// Setup WebView
		webView = findViewById(R.id.articleDetailView);
		webView.getSettings().setJavaScriptEnabled(true);

		// Show action bar icons
		showBookmarkIcon(articleTitle, articleUrl);
		showShareIcon(articleTitle, articleUrl);
		showPrintIcon(articleTitle, webView);
		
		// Loading screen
		loadingDialog = new LoadingDialog(this);

		// WebViewClient to handle page load and errors
		webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					if (loadingDialog != null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
					}
				}

				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
					if (loadingDialog != null && loadingDialog.isShowing()) {
						loadingDialog.dismiss();
					}
					// Optional: Show error message in a toast or dialog
				}
			});

		// ✅ Check for internet before loading
		if (isNetworkAvailable()) {
			loadingDialog.show();
			webView.loadUrl(articleUrl);
		} else {
			showNoInternetDialog(new Runnable() {
					@Override
					public void run() {
						// Retry loading if user clicks Retry
						if (isNetworkAvailable()) {
							loadingDialog.show();
							webView.loadUrl(articleUrl);
						} else {
							showNoInternetDialog(this); // Show again if still no connection
						}
					}
				});
		}
	}

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        webView.destroy();
        super.onDestroy();
    }
}
