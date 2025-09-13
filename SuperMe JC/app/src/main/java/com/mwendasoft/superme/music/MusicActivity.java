package com.mwendasoft.superme.music;

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
import com.mwendasoft.superme.quotes.*;
import com.mwendasoft.superme.authors.*;
import java.io.*;
import android.os.*;
import java.util.*;

public class MusicActivity extends BaseActivity {
    private SongsDBHelper dbHelper;
	private AuthorsDBHelper artistsDbHelper;
    private ListView songsListView;
	private TextView songsListTitle, songsCountBadge;
	private ImageButton addSongFab;
    private ArrayList<Song> songs;
    private SongsViewAdapter songsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_activity);

        songsListView = findViewById(R.id.songsListView);
        songsListTitle = findViewById(R.id.songsListTitle);
		songsCountBadge = findViewById(R.id.songsCountBadge);
		addSongFab = findViewById(R.id.addSongFab);
        
		dbHelper = new SongsDBHelper(this);
        dbHelper.open(); 
		artistsDbHelper = new AuthorsDBHelper(this);
		
        songs = new ArrayList<>();
        songsAdapter = new SongsViewAdapter(this, songs);
        songsListView.setAdapter(songsAdapter); // Set adapter before loading data

		// == REGISTER FOR CONTEXT MENU == //
        registerForContextMenu(songsListView);
		// == //
		
        loadSongs();
		
		songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Song clickedSong = songs.get(position);
				if (clickedSong != null) {
					openSongView(clickedSong);
				}
			}
		});
		
		addSongFab.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MusicActivity.this, SongAddActivity.class);
					startActivity(intent);
				}
			});
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.songsListView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Song selectedSong = songs.get(info.position);
			menu.setHeaderTitle(selectedSong.getSongTitle());
			menu.add(Menu.NONE, 1, 1, "Open");
			menu.add(Menu.NONE, 2, 2, "Edit");
			menu.add(Menu.NONE, 3, 3, "Delete");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		Song selectedSong = songs.get(info.position);

		switch (item.getItemId()) {
			case 1: // Open
				openSongView(selectedSong);
				return true;
			case 3: // Delete
				confirmAndDeleteSong(selectedSong);
				return true;
			case 2: // Edit
				editSong(selectedSong);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmAndDeleteSong(final Song song) {
		new AlertDialog.Builder(this)
			.setTitle(R.string.delete)
			.setMessage("Are you sure you want to delete \"" + song.getSongTitle() + "\"?")
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Step 1: Delete the media folder e.g., ".superme/songs/Song 15"
					File songFolder = new File(Environment.getExternalStorageDirectory(), ".superme/songs/Song " + song.getId());
					deleteFolderRecursive(songFolder);

					// Step 2: Delete any individual media files as safety
					String audioPath = dbHelper.getMusicPath(song.getId());
					if (audioPath != null) {
						File file = new File(audioPath);
						if (file.exists()) {
							file.delete();
						}
					}
					// Step 3: Delete the note from the database
					dbHelper.deleteSong(song.getId());
					songs.remove(song);
					loadSongs();
					songsAdapter.notifyDataSetChanged();
				}
			})
			.setNegativeButton(R.string.no, null)
			.show();
	}

	private void deleteFolderRecursive(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						deleteFolderRecursive(file);
					} else {
						file.delete();
					}
				}
			}
			folder.delete(); // delete the folder itself
		}
	}

	private void editSong(Song song) {
		Intent intent = new Intent(MusicActivity.this, SongEditActivity.class);
		intent.putExtra("selectedSong", song);
		startActivity(intent);
	}

    private void loadSongs() {
        songs.clear(); // Avoid duplicates

        Cursor cursor = dbHelper.getAllSongs();

        if (cursor != null) {
			int idIndex = cursor.getColumnIndexOrThrow("id");
            int titleIndex = cursor.getColumnIndexOrThrow("title");
            int artistIndex = cursor.getColumnIndexOrThrow("artist");
            int lyricsIndex = cursor.getColumnIndexOrThrow("lyrics");
			
            while (cursor.moveToNext()) {
				int songId = cursor.getInt(idIndex);
                String lyrics = cursor.getString(lyricsIndex);
                String title = cursor.getString(titleIndex);
                int artistId = cursor.getInt(artistIndex);
				String artistName = artistsDbHelper.getAuthorNameById(artistId);
				
                if (title != null && !title.isEmpty()) {
                    songs.add(new Song(songId, title, artistName, lyrics));
                }
            }
            cursor.close();
        }

		songsListTitle.setText(R.string.music);
		songsCountBadge.setText(String.valueOf(songs.size()));

        if (songs.isEmpty()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.music);
			builder.setMessage("No songs found. Would you like to add a new one?");
			builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(MusicActivity.this, SongAddActivity.class);
						startActivity(intent);
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
			songsAdapter.notifyDataSetChanged();
		}

		// END //
    }
	
	private void openSongView(Song song) {
		Intent intent = new Intent(this, SongViewActivity.class);
		intent.putExtra("selectedSong", song);
		startActivity(intent);
	}

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
	
	@Override
	protected void onResume() { // Updates activity when it comes to view again.
		super.onResume();
		loadSongs();
	}
}
