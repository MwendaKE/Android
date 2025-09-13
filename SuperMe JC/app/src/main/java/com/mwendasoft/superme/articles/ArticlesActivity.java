package com.mwendasoft.superme.articles;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import com.mwendasoft.superme.authors.AuthorsDBHelper;

import java.util.ArrayList;
import com.mwendasoft.superme.helpers.*;
import com.mwendasoft.superme.categories.*;

public class ArticlesActivity extends BaseActivity {
    // Constants for context menu items
    private static final int MENU_OPEN = 1;
    private static final int MENU_EDIT = 2;
    private static final int MENU_DELETE = 3;

    private static final String TAG = "ArticlesActivity";

    private ArticlesDBHelper articlesDbHelper;
	private CategoriesDBHelper categsDbHelper;
    private AuthorsDBHelper authorsDbHelper;
    private ListView articlesListView;
    private TextView articlesTitle, articlesCountBadge;
    private ImageButton addArticleFab;
    private final ArrayList<Article> articles = new ArrayList<>();
    private ArticleViewAdapter articleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.articles_activity);

        initializeViews();
        setupDatabaseHelpers();
        setupAdapter();
        setupListeners();

        loadArticles();
    }

    private void initializeViews() {
        articlesListView = findViewById(R.id.articlesListView);
        articlesTitle = findViewById(R.id.articlesTitle);
        articlesCountBadge = findViewById(R.id.articlesCountBadge);
        addArticleFab = findViewById(R.id.addArticleFab);
    }

    private void setupDatabaseHelpers() {
        articlesDbHelper = new ArticlesDBHelper(this);
        authorsDbHelper = new AuthorsDBHelper(this);
    }

    private void setupAdapter() {
        articleAdapter = new ArticleViewAdapter(this, articles);
        articlesListView.setAdapter(articleAdapter);
        registerForContextMenu(articlesListView);
    }

    private void setupListeners() {
        articlesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Article clickedArticle = articles.get(position);
					if (clickedArticle != null) {
						openArticleDetail(clickedArticle);
					}
				}
			});

        addArticleFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(ArticlesActivity.this, ArticleAddActivity.class));
				}
			});
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.articlesListView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Article selectedArticle = articles.get(info.position);
            menu.setHeaderTitle(selectedArticle.getTitle());
            menu.add(Menu.NONE, MENU_OPEN, 1, "Open");
            menu.add(Menu.NONE, MENU_EDIT, 2, "Edit");
            menu.add(Menu.NONE, MENU_DELETE, 3, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);

        Article selectedArticle = articles.get(info.position);

        switch (item.getItemId()) {
            case MENU_OPEN:
                openArticleDetail(selectedArticle);
                return true;
            case MENU_EDIT:
                editArticle(selectedArticle);
                return true;
            case MENU_DELETE:
                confirmAndDeleteArticle(selectedArticle);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmAndDeleteArticle(final Article article) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.delete_article_confirmation, article.getTitle()))
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteArticle(article);
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }

    private void deleteArticle(Article article) {
        try {
            int writerId = authorsDbHelper.getAuthorIdByName(article.getWriter());
            if (writerId == -1) {
				ToastMessagesManager.show(this, "Author not found!");
                return;
            }

            int deletedRows = articlesDbHelper.deleteArticle(article.getId());
            if (deletedRows > 0) {
                articles.remove(article);
                if (articleAdapter != null) {
                    articleAdapter.notifyDataSetChanged();
                }
                updateArticleCount();
                ToastMessagesManager.show(this, "Deleted article.");
            } else {
                ToastMessagesManager.show(this, "Error deleting article!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting article", e);
            ToastMessagesManager.show(this, "An error has occurred!");
        }
    }

    private void editArticle(Article article) {
        Intent intent = new Intent(this, ArticleEditActivity.class);
        intent.putExtra("selectedArticle", article);
        startActivity(intent);
    }

    private void loadArticles() {
        articles.clear();
        Cursor cursor = null;

        try {
            cursor = articlesDbHelper.getAllArticles();
            if (cursor != null && cursor.moveToFirst()) {
				int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int writerIndex = cursor.getColumnIndexOrThrow("writer");
				int categIndex = cursor.getColumnIndexOrThrow("category");

                do {
					int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    
					int writerId = cursor.getInt(writerIndex);
                    String writerName = authorsDbHelper.getAuthorNameById(writerId);
					
					int categId = cursor.getInt(categIndex);
					
                    if (title != null && !title.isEmpty() && writerName != null) {
                        articles.add(new Article(id, title, writerName, categId));
                    }
                } while (cursor.moveToNext());
            }
            updateUIAfterLoading();
        } catch (Exception e) {
            Log.e(TAG, "Error loading articles", e);
            showErrorDialog(R.string.error_loading_articles);
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
					articlesTitle.setText(R.string.articles);
					updateArticleCount();

					if (articles.isEmpty()) {
						showNoArticlesDialog();
					} else if (articleAdapter != null) {
						articleAdapter.notifyDataSetChanged();
					}
				}
			});
    }

    private void updateArticleCount() {
        articlesCountBadge.setText(String.valueOf(articles.size()));
    }

    private void showNoArticlesDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.articles)
            .setMessage("Articles not found. Would you like to add a new one?")
            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(ArticlesActivity.this, ArticleAddActivity.class));
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .show();
    }

    private void openArticleDetail(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("selectedArticle", article);
        startActivity(intent);
    }

    private void showErrorDialog(int messageResId) {
        new AlertDialog.Builder(this)
            .setMessage(messageResId)
            .setPositiveButton(R.string.Ok, null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadArticles();
    }

    @Override
    protected void onDestroy() {
        try {
            if (articlesDbHelper != null) {
                articlesDbHelper.close();
            }
            if (authorsDbHelper != null) {
                authorsDbHelper.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing database helpers", e);
        }
        super.onDestroy();
    }
}
