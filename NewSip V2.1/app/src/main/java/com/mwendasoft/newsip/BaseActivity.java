package com.mwendasoft.newsip;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.*;
import android.content.*;
import com.mwendasoft.newsip.globalnews.*;
import com.mwendasoft.newsip.newshelpers.*;
import android.app.*;
import android.net.*;
import com.mwendasoft.newsip.bookmarks.*;
import android.print.*;
import android.webkit.*;
import android.graphics.*;

public class BaseActivity extends Activity {
    protected ImageView homeIcon;
    protected ImageView globeIcon;
    protected ImageView bookmarkIcon;
    protected ImageView printIcon;
    protected ImageView shareIcon;
    protected ImageView customLogo;

    // Tab tracking - made static to maintain state across activities
    protected static int currentTabId = R.id.navHome;
    protected LinearLayout bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            final View customView = getLayoutInflater().inflate(R.layout.custom_action_bar, null);
            int heightInPx = (int) (72 * getResources().getDisplayMetrics().density);
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                heightInPx
            );
            actionBar.setCustomView(customView, params);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                actionBar.setElevation(0);
            }

            customLogo = customView.findViewById(R.id.customLogo);
            homeIcon = customView.findViewById(R.id.homeIcon);
            globeIcon = customView.findViewById(R.id.globeIcon);
            bookmarkIcon = customView.findViewById(R.id.bookmarkIcon);
            printIcon = customView.findViewById(R.id.printIcon);
            shareIcon = customView.findViewById(R.id.shareIcon);

            globeIcon.setVisibility(View.GONE);
            bookmarkIcon.setVisibility(View.GONE);
            shareIcon.setVisibility(View.GONE);

            if (globeIcon != null) {
                globeIcon.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(BaseActivity.this, GlobalMainActivity.class);
							startActivity(intent);
						}
					});
            }

            customView.post(new Runnable() {
					@Override
					public void run() {
						resizeLogo(customLogo, customView.getHeight());
					}
				});
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        RelativeLayout rootLayout = new RelativeLayout(this);

        View contentView = getLayoutInflater().inflate(layoutResID, null);
        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        );
        contentParams.addRule(RelativeLayout.ABOVE, R.id.bottomBar);
        rootLayout.addView(contentView, contentParams);

        bottomBar = (LinearLayout) getLayoutInflater().inflate(R.layout.bottom_bar_layout, null);
        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rootLayout.addView(bottomBar, bottomParams);

        super.setContentView(rootLayout);

        setupBottomBar(bottomBar);
        updateTabHighlighting(currentTabId); // Initialize tab highlighting
    }

    private void setupBottomBar(View bottomBar) {
        bottomBar.findViewById(R.id.navHome).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentTabId != R.id.navHome) {
						navigateToTab(MainActivity.class, R.id.navHome);
					}
				}
			});

        bottomBar.findViewById(R.id.navGlobal).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentTabId != R.id.navGlobal) {
						navigateToTab(GlobalMainActivity.class, R.id.navGlobal);
					}
				}
			});

        bottomBar.findViewById(R.id.navBookmarks).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentTabId != R.id.navBookmarks) {
						navigateToTab(BookmarksActivity.class, R.id.navBookmarks);
					}
				}
			});

        bottomBar.findViewById(R.id.navAbout).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentTabId != R.id.navAbout) {
						navigateToTab(AboutActivity.class, R.id.navAbout);
					}
				}
			});
    }

    private void navigateToTab(Class<?> targetActivity, int tabId) {
        currentTabId = tabId;
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    protected void updateTabHighlighting(int selectedTabId) {
        if (bottomBar == null) return;

        int[][] tabComponents = {
            {R.id.navHome, R.drawable.ic_home},
            {R.id.navGlobal, R.drawable.ic_language},
            {R.id.navBookmarks, R.drawable.ic_bookmark_n22},
            {R.id.navAbout, R.drawable.ic_info22}
        };

        for (int[] tab : tabComponents) {
            View tabView = bottomBar.findViewById(tab[0]);
            if (tabView != null) {
                boolean isSelected = (tab[0] == selectedTabId);

                // Set background
                // tabView.setBackgroundColor(isSelected ? 
										   // Color.parseColor("#BDBDBD") : Color.TRANSPARENT);

                // Find and style text view
                TextView textView = findTextViewInTab(tabView);
                if (textView != null) {
                    textView.setTextColor(isSelected ? 
										  Color.parseColor("#1B5E20") : Color.parseColor("#B71C1C"));
                    textView.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
                }

                // Find and style image view
                ImageView imageView = findImageViewInTab(tabView);
                if (imageView != null) {
                    imageView.setColorFilter(isSelected ? 
											 Color.parseColor("#1B5E20") : Color.parseColor("#B71C1C"));
                    imageView.setImageResource(tab[1]); // Simplified - no special handling
                }
            }
        }
    }
	
    private TextView findTextViewInTab(View tab) {
        if (tab instanceof ViewGroup) {
            ViewGroup tabGroup = (ViewGroup) tab;
            for (int i = 0; i < tabGroup.getChildCount(); i++) {
                View child = tabGroup.getChildAt(i);
                if (child instanceof TextView) {
                    return (TextView) child;
                }
            }
        }
        return null;
    }

    private ImageView findImageViewInTab(View tab) {
        if (tab instanceof ViewGroup) {
            ViewGroup tabGroup = (ViewGroup) tab;
            for (int i = 0; i < tabGroup.getChildCount(); i++) {
                View child = tabGroup.getChildAt(i);
                if (child instanceof ImageView) {
                    return (ImageView) child;
                }
            }
        }
        return null;
    }

    private void resizeLogo(ImageView logo, int containerHeight) {
        float aspectRatio = 720f / 314f;
        int logoHeight = (int) (containerHeight * 0.7f);
        int logoWidth = (int) (logoHeight * aspectRatio);

        ViewGroup.LayoutParams params = logo.getLayoutParams();
        params.height = logoHeight;
        params.width = logoWidth;
        logo.setLayoutParams(params);

        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setVisibility(View.VISIBLE);
    }

    protected void showGlobeIcon(final Class<?> targetActivity) {
        if (globeIcon != null) {
            globeIcon.setVisibility(View.VISIBLE);
            globeIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(new Intent(BaseActivity.this, targetActivity));
					}
				});
        }
    }

    protected void hideGlobeIcon() {
        if (globeIcon != null) {
            globeIcon.setVisibility(View.GONE);
            globeIcon.setOnClickListener(null);
        }
    }

    protected void showHomeIcon(final Class<?> targetActivity) {
        if (homeIcon != null) {
            homeIcon.setVisibility(View.VISIBLE);
            homeIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(BaseActivity.this, targetActivity);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						finish();
					}
				});
        }
    }

    protected void hideHomeIcon() {
        if (homeIcon != null) {
            homeIcon.setVisibility(View.GONE);
            homeIcon.setOnClickListener(null);
        }
    }

    protected void showPrintIcon(final String articleTitle, final WebView webView) {
        if (printIcon != null) {
            printIcon.setVisibility(View.VISIBLE);
            printIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						handlePrint(articleTitle, webView);
					}
				});
        }
    }

    protected void hidePrintIcon() {
        if (printIcon != null) {
            printIcon.setVisibility(View.GONE);
            printIcon.setOnClickListener(null);
        }
    }

    private void handlePrint(String title, WebView webView) {
        if (webView == null) {
            Toast.makeText(this, "No content to print", Toast.LENGTH_SHORT).show();
            return;
        }

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (printManager == null) {
            Toast.makeText(this, "Printing not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        String jobName = (title != null && !title.trim().isEmpty()) ? title : "Article";
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);

        printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }

    protected void showBookmarkIcon(final String title, final String url) {
        if (bookmarkIcon == null) return;

        final DatabaseHelper db = new DatabaseHelper(this);
        boolean isSaved = db.isBookmarked(url);
        updateBookmarkIcon(isSaved);

        bookmarkIcon.setVisibility(View.VISIBLE);
        bookmarkIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean nowSaved = !db.isBookmarked(url);
					if (nowSaved) {
						db.addBookmark(title, url);
					} else {
						db.removeBookmark(url);
					}
					updateBookmarkIcon(nowSaved);
				}
			});
    }

    private void updateBookmarkIcon(boolean isBookmarked) {
        if (bookmarkIcon != null) {
            bookmarkIcon.setImageResource(
                isBookmarked ? R.drawable.ic_bookmark_n22 : R.drawable.ic_bookmark_o22
            );
        }
    }

    protected void hideBookmarkIcon() {
        if (bookmarkIcon != null) {
            bookmarkIcon.setVisibility(View.GONE);
            bookmarkIcon.setOnClickListener(null);
        }
    }

    protected void showShareIcon(final String title, final String url) {
        if (shareIcon != null) {
            shareIcon.setVisibility(View.VISIBLE);
            shareIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.setType("text/plain");
						shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);

						String message = "Please check this article:\n\n" + title + "\n" + url;
						shareIntent.putExtra(Intent.EXTRA_TEXT, message);

						startActivity(Intent.createChooser(shareIntent, "Share article via"));
					}
				});
        }
    }

    protected void hideShareIcon() {
        if (shareIcon != null) {
            shareIcon.setVisibility(View.GONE);
            shareIcon.setOnClickListener(null);
        }
    }

    public void showNoInternetDialog(final Runnable retryAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection Error");
        builder.setMessage("Please check your internet connection and try again.");
        builder.setCancelable(false);

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (retryAction != null) {
						retryAction.run();
					}
				}
			});

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface d) {
					Button retryBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
					retryBtn.setTextColor(Color.parseColor("#B71C1C"));
					retryBtn.setTextSize(16);
					retryBtn.setAllCaps(false);

					Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
					cancelBtn.setTextColor(Color.GRAY);
					cancelBtn.setTextSize(16);
					cancelBtn.setAllCaps(false);
				}
			});

        dialog.show();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }
}
