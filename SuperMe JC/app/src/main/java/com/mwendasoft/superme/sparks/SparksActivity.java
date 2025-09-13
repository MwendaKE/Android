package com.mwendasoft.superme.sparks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.*;
import com.mwendasoft.superme.helpers.*;

public class SparksActivity extends BaseActivity {
    private static final int PICK_VIDEOS_CODE = 101;
	private static final String TAG = "SparksActivity";
    private TextView sparksListTitle;
    private TextView sparksCountBadge;
    private RecyclerView[] recyclerViews;
    private LinearLayout[] layoutViews;
    private TextView[] countViews;
    private ImageButton addSparkFab;
    private HashMap<String, Integer> categoryMap;
    private int selectedCategory = -1;
    private SparksDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sparks_activity);

        dbHelper = new SparksDBHelper(this);
        sparksListTitle = findViewById(R.id.sparksListTitle);
        sparksCountBadge = findViewById(R.id.sparksCountBadge);
        addSparkFab = findViewById(R.id.addSparkFab);

        // Initialize RecyclerViews, Layouts, and CountViews
        recyclerViews = new RecyclerView[] {
            findViewById(R.id.sparksTechSciRecycler),
            findViewById(R.id.sparksHeavenlyRecycler),
            findViewById(R.id.sparksMicrodropRecycler),
            findViewById(R.id.sparksPastRecycler),
            findViewById(R.id.sparksPowerupRecycler),
            findViewById(R.id.sparksLaughsRecycler),
            findViewById(R.id.sparksMoneyRecycler),
            findViewById(R.id.sparksRandomRecycler)
        };

        layoutViews = new LinearLayout[] {
            findViewById(R.id.sparksTechSciViewLayout),
            findViewById(R.id.sparksHeavenlyViewLayout),
            findViewById(R.id.sparksMicdropViewLayout),
            findViewById(R.id.sparksPastViewLayout),
            findViewById(R.id.sparksPowerupViewLayout),
            findViewById(R.id.sparksLaughsViewLayout),
            findViewById(R.id.sparksMoneyViewLayout),
            findViewById(R.id.sparksRandomViewLayout)
        };

        countViews = new TextView[] {
            findViewById(R.id.sparksTechCount),
            findViewById(R.id.sparksHeavenlyCount),
            findViewById(R.id.sparksMicrodropCount),
            findViewById(R.id.sparksPastCount),
            findViewById(R.id.sparksPowerupCount),
            findViewById(R.id.sparksLaughsCount),
            findViewById(R.id.sparksMoneyCount),
            findViewById(R.id.sparksRandomCount)
        };

        loadSparkVideos();

        addSparkFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAddFabDialog();
				}
			});
    }

    private void setupRecycler(final RecyclerView recyclerView, final LinearLayout layout, 
							   final TextView countView, List<String> videoPaths) {
        if (videoPaths == null || videoPaths.isEmpty()) {
            layout.setVisibility(View.GONE);
            return;
        }

        layout.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        final SparksViewAdapter adapter = new SparksViewAdapter(this, new ArrayList<String>(videoPaths));
        adapter.setOnVideoDeleteListener(new SparksViewAdapter.OnVideoDeleteListener() {
				@Override
				public void onRequestDelete(final int position, final String path) {
					new AlertDialog.Builder(SparksActivity.this)
						.setTitle("Delete Video")
						.setMessage("Are you sure you want to delete this video?")
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (position >= 0 && position < adapter.getItemCount()) {
									if (dbHelper.deleteSpark(path) == 1) {
										File file = new File(path);
										if (file.exists()) {
											file.delete();
										}
										adapter.removePathAt(position);
										countView.setText(String.valueOf(adapter.getItemCount()));
										if (adapter.getItemCount() == 0) {
											layout.setVisibility(View.GONE);
										}
										updateTotalCount();
									}
								}
							}
						})
						.setNegativeButton("No", null)
						.show();
				}
			});
        recyclerView.setAdapter(adapter);
        countView.setText(String.valueOf(videoPaths.size()));
    }

    private void loadSparkVideos() {
		int videoCount = dbHelper.getVideoCount();
		sparksListTitle.setText(R.string.sparks);
		sparksCountBadge.setText(String.valueOf(videoCount));

		if (videoCount == 0) {
			// Hide all layout views
			for (View layout : layoutViews) {
				layout.setVisibility(View.GONE);
			}

			showEmptySparksDialog();
			return;
		}

		// Show all layout views (in case they were previously hidden)
		for (View layout : layoutViews) {
			layout.setVisibility(View.VISIBLE);
		}

		// Show other related views
		sparksListTitle.setVisibility(View.VISIBLE);
		sparksCountBadge.setVisibility(View.VISIBLE);

		// Setup recycler views
		for (int i = 0; i < recyclerViews.length; i++) {
			setupRecycler(recyclerViews[i], layoutViews[i], countViews[i], 
						  dbHelper.getVideosByCategory(1001 + i));
		}
	}

    private void showEmptySparksDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sparks)
			.setMessage("No spark videos found. Would you like to add new ones?")
			.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showAddFabDialog();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setCancelable(false)
			.show();
    }

    private void showAddFabDialog() {
        categoryMap = new HashMap<String, Integer>();
        categoryMap.put("Brainy & Buzzy (Tech & Science)", 1001);
        categoryMap.put("Heavenly Vibes (Christianity)", 1002);
        categoryMap.put("Mic Drop Moments (Songs)", 1003);
        categoryMap.put("Past & Curious (History)", 1004);
        categoryMap.put("Power Up! (Motivational)", 1005);
        categoryMap.put("Just for Laughs (Comedy)", 1006);
        categoryMap.put("Money Matters (Financial)", 1007);
        categoryMap.put("Random Goodies (Other)", 1008);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.spark_addvideo_dialog);

        final Spinner spinner = dialog.findViewById(R.id.sparkDialogAddCategorySpinner);
        Button btnAdd = dialog.findViewById(R.id.sparkDialogAddVideosBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, 
            android.R.layout.simple_spinner_dropdown_item, 
            new ArrayList<String>(categoryMap.keySet())
        );
        spinner.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectedCategory = categoryMap.get(spinner.getSelectedItem().toString());
					dialog.dismiss();
					pickVideos();
				}
			});

        dialog.show();
    }

    private void pickVideos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Videos"), PICK_VIDEOS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEOS_CODE && resultCode == RESULT_OK) {
            List<String> savedPaths = new ArrayList<String>();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    String path = saveToSupermeFolder(uri);
                    if (path != null) savedPaths.add(path);
                }
            } else if (data.getData() != null) {
                String path = saveToSupermeFolder(data.getData());
                if (path != null) savedPaths.add(path);
            }

            if (!savedPaths.isEmpty()) {
                if (dbHelper.insertMultipleVideos(savedPaths, selectedCategory) > 0) {
					ToastMessagesManager.show(this, "Videos added successfully.");
                    loadSparkVideos();
                }
            }
        }
    }

    private String saveToSupermeFolder(Uri uri) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File superMeFolder = new File(Environment.getExternalStorageDirectory(), ".superme/sparks");
            if (!superMeFolder.exists()) {
                superMeFolder.mkdirs();
            }

            File destFile = new File(superMeFolder, "spark_video_" + System.currentTimeMillis() + ".mp4");

            in = getContentResolver().openInputStream(uri);
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "Error saving video", e);
            return null;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing streams", e);
            }
        }
    }

    private void updateTotalCount() {
        int totalCount = 0;
        for (TextView countView : countViews) {
            try {
                totalCount += Integer.parseInt(countView.getText().toString());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing count", e);
            }
        }
        sparksCountBadge.setText(String.valueOf(totalCount));
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
