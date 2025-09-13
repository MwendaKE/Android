package com.mwendasoft.superme.clips;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.mwendasoft.superme.R;
import com.mwendasoft.superme.BaseActivity;
import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import android.content.res.*;
import android.text.*;
import android.content.ClipboardManager;
import android.text.InputType;
import android.support.v4.content.*;
import com.mwendasoft.superme.*;
import com.mwendasoft.superme.helpers.*;

public class ClipsActivity extends BaseActivity {
    private ClipsDBHelper dbHelper;
    private ListView clipsListView;
    private TextView clipsListTitle, clipsCountBadge;
	private ImageButton addClipFab;
    private ArrayList<Clip> clips;
    private ClipsViewAdapter clipsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clips_activity);

        clipsListView = findViewById(R.id.clipsListView);
		registerForContextMenu(clipsListView);

        clipsListTitle = findViewById(R.id.clipsListTitle);
		clipsCountBadge = findViewById(R.id.clipsCountBadge);
		addClipFab = findViewById(R.id.addClipFab);
		
        dbHelper = new ClipsDBHelper(this);
        dbHelper.open();  

        clips = new ArrayList<>();
        clipsAdapter = new ClipsViewAdapter(this, clips);
        clipsListView.setAdapter(clipsAdapter); // Set adapter before loading data

        loadClips();
		
		addClipFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddClipDialog();
			}
		});
    }

    private void loadClips() {
        clips.clear(); // Avoid duplicates
		
		Cursor cursor = dbHelper.getAllClips();

        if (cursor != null) {
            int clipIndex = cursor.getColumnIndexOrThrow("clip");
            int writerIndex = cursor.getColumnIndexOrThrow("writer");
            int sourceIndex = cursor.getColumnIndexOrThrow("source");

            while (cursor.moveToNext()) {
                String clip = cursor.getString(clipIndex);
                String writer = cursor.getString(writerIndex);
                String source = cursor.getString(sourceIndex);

                if (clip != null && !clip.isEmpty()) {
                    clips.add(new Clip(clip, source, writer));
                }
            }
            cursor.close();
        }

		clipsListTitle.setText(R.string.clips);
		clipsCountBadge.setText(String.valueOf(clips.size()));

        if (clips.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.clips);
			builder.setMessage("No clips found. Would you like to add a new one?");
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showAddClipDialog();
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
			clipsAdapter.notifyDataSetChanged();
		}
    }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.clipsListView) {
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
		Clip selectedClip = clips.get(info.position);

		switch (item.getItemId()) {
			case 1: // Share
				shareClip(selectedClip);
				return true;
			case 4: // Delete
				confirmAndDeleteClip(selectedClip);
				return true;
			case 3: // Edit
				showEditDialog(selectedClip);
				return true;
			case 2: // Copy
				copyClipText(selectedClip);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private boolean shareClip(Clip clip) {
		String shareText = clip.getClip() + " ~ " + clip.getWriter() + ", " + clip.getSource();

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(shareIntent, "Share via"));
		return true;
	}

	private void copyClipText(Clip clip) {
		String copyText = clip.getClip() + " ~ " + clip.getWriter() + ", " + clip.getSource();

		ClipboardManager clipboard = (ClipboardManager) ContextCompat.getSystemService(this, ClipboardManager.class);
		if (clipboard != null) {
			ClipData clipData = ClipData.newPlainText("Clip",copyText);
			clipboard.setPrimaryClip(clipData);
			ToastMessagesManager.show(this, "Clip copied to clipboard.");
		} else {
			ToastMessagesManager.show(this, "Clipboard not available!");
		}
	}
	
	private void confirmAndDeleteClip(final Clip clip) {
		new AlertDialog.Builder(this)
			.setTitle("Delete Clip")
			.setMessage("Are you sure you want to delete this clip?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String clipText = clip.getClip();
					String clipWriter = clip.getWriter();
					String clipSource = clip.getSource();
					
					dbHelper.deleteClip(clipText, clipWriter, clipSource);
					clips.remove(clip);
					loadClips();
					clipsAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}
	
	private void showAddClipDialog() {
		int heightInDp = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 400, 
			Resources.getSystem().getDisplayMetrics()
		);

		LinearLayout.LayoutParams clipTextEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			heightInDp  // height
		);

		int marginInPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics() // Convert 20dp to px
		);

		clipTextEditParams.setMargins(0, 0, 0, marginInPx); // values in pixels

		// == SET HEIGHT AND WIDTH IN WRITER EDIT == //

		LinearLayout.LayoutParams clipWriterEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			LinearLayout.LayoutParams.WRAP_CONTENT // height
		);

		clipWriterEditParams.setMargins(0, 0, 0, marginInPx);

		// == SET HEIGHT AND WIDTH IN WRITER EDIT == //

		LinearLayout.LayoutParams clipSourceEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			LinearLayout.LayoutParams.WRAP_CONTENT // height
		);

		// Layout
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(50, 40, 50, 10);

		// Quote input
		final EditText clipTextInput = new EditText(this);
		clipTextInput.setHint("Enter your clip text here...");
		//clipTextInput.setSingleLine(false);
		//clipTextInput.setMaxLines(10);
		//clipTextInput.setLines(8);
		clipTextInput.setMinLines(3);
		clipTextInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		clipTextInput.setGravity(Gravity.TOP);
		clipTextInput.setBackgroundResource(R.drawable.edit_text_border);
		clipTextInput.setPadding(20, 20, 20, 20);
		clipTextInput.setLayoutParams(clipTextEditParams);
		layout.addView(clipTextInput);

		// Writer Input
		final EditText writerEdit = new EditText(this);
		writerEdit.setHint("Enter clip author...");
		writerEdit.setBackgroundResource(R.drawable.edit_text_border);
		writerEdit.setPadding(20, 20, 20, 20);
		writerEdit.setLayoutParams(clipWriterEditParams);
		layout.addView(writerEdit);

		// Source Input
		final EditText sourceEdit = new EditText(this);
		sourceEdit.setHint("Enter clip source...");
		sourceEdit.setBackgroundResource(R.drawable.edit_text_border);
		sourceEdit.setPadding(20, 20, 20, 20);
		sourceEdit.setLayoutParams(clipSourceEditParams);
		layout.addView(sourceEdit);

		// Show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add New Clip");
		builder.setView(layout);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newClip = clipTextInput.getText().toString().trim();
				String newWriter = writerEdit.getText().toString().trim();
				String newSource = sourceEdit.getText().toString().trim();

				ContentValues values = new ContentValues();
				values.put("clip", newClip);
				values.put("writer", newWriter);
				values.put("source", newSource);

				long rowId = dbHelper.insertClip(values);

				if (rowId != -1) {
					loadClips();
					ToastMessagesManager.show(ClipsActivity.this, "Added successfully!");
					clipsAdapter.notifyDataSetChanged();
				} else {
					ToastMessagesManager.show(ClipsActivity.this, "Failed to add clip");
				}
			}
		});
		builder.setNegativeButton("Cancel", null);
			
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
	}

	private void showEditDialog(Clip clip) {
		final String currentClip = clip.getClip();
		final String currentWriter = clip.getWriter(); 
		final String currentSource = clip.getSource(); 
		
		// == SET HEIGHT AND MARGIN IN CLIP EDIT == //
		
		int heightInDp = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 400, 
			Resources.getSystem().getDisplayMetrics()
		);
		
		LinearLayout.LayoutParams clipTextEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			heightInDp  // height
		);

		int marginInPx = (int) TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics() // Convert 20dp to px
		);
	
		clipTextEditParams.setMargins(0, 0, 0, marginInPx); // values in pixels
		
		// == SET HEIGHT AND WIDTH IN WRITER EDIT == //
		
		LinearLayout.LayoutParams clipWriterEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			LinearLayout.LayoutParams.WRAP_CONTENT // height
		);
		
		clipWriterEditParams.setMargins(0, 0, 0, marginInPx);
		
		// == SET HEIGHT AND WIDTH IN WRITER EDIT == //

		LinearLayout.LayoutParams clipSourceEditParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, // width
			LinearLayout.LayoutParams.WRAP_CONTENT // height
		);
		
		// Layout
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(50, 40, 50, 10);

		// Quote input
		final EditText clipTextInput = new EditText(this);
		clipTextInput.setHint("Quote text");
		clipTextInput.setText(currentClip);
		//clipTextInput.setSingleLine(false);
		//clipTextInput.setMaxLines(10);
		//clipTextInput.setLines(8);
		clipTextInput.setMinLines(3);
		clipTextInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		clipTextInput.setGravity(Gravity.TOP);
		clipTextInput.setBackgroundResource(R.drawable.edit_text_border);
		clipTextInput.setPadding(20, 20, 20, 20);
		clipTextInput.setLayoutParams(clipTextEditParams);
		layout.addView(clipTextInput);

		// Writer Input
		final EditText writerEdit = new EditText(this);
		writerEdit.setText(currentWriter);
		writerEdit.setBackgroundResource(R.drawable.edit_text_border);
		writerEdit.setPadding(20, 20, 20, 20);
		writerEdit.setLayoutParams(clipWriterEditParams);
		layout.addView(writerEdit);

		// Source Input
		final EditText sourceEdit = new EditText(this);
		sourceEdit.setText(currentSource);
		sourceEdit.setBackgroundResource(R.drawable.edit_text_border);
		sourceEdit.setPadding(20, 20, 20, 20);
		sourceEdit.setLayoutParams(clipSourceEditParams);
		layout.addView(sourceEdit);
		
		// Show dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Edit Clip");
		builder.setView(layout);
		builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int clipId = dbHelper.getClipId(currentClip, currentWriter, currentSource);
					
				String newClip = clipTextInput.getText().toString().trim();
				String newWriter = writerEdit.getText().toString().trim();
				String newSource = sourceEdit.getText().toString().trim();
				
				ContentValues values = new ContentValues();
				values.put("clip", newClip);
				values.put("writer", newWriter);
				values.put("source", newSource);
					
				int rowsUpdated = dbHelper.updateClip(clipId, values);
					
				if (rowsUpdated > 0) {
					loadClips();
					ToastMessagesManager.show(ClipsActivity.this, "Updated successfully.");
					clipsAdapter.notifyDataSetChanged();
				} else {
					ToastMessagesManager.show(ClipsActivity.this, "Update failed!");
				}
			}
		});
		builder.setNegativeButton("Cancel", null);
			
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		dialog.show();
	}
	
    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
