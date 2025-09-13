package com.mwendasoft.superme.sparks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.mwendasoft.superme.R;
import com.mwendasoft.superme.helpers.DialogGeneralVideoViewHelper;
import java.util.ArrayList;

public class SparksViewAdapter extends RecyclerView.Adapter<SparksViewAdapter.SparksViewHolder> {
    private ArrayList<String> videoPaths;
    private Context context;
    private OnVideoDeleteListener deleteListener;

    public interface OnVideoDeleteListener {
        void onRequestDelete(int position, String path);
    }

    public SparksViewAdapter(Context context, ArrayList<String> videoPaths) {
        this.context = context;
        this.videoPaths = videoPaths;
    }

    public void setOnVideoDeleteListener(OnVideoDeleteListener listener) {
        this.deleteListener = listener;
    }

    @Override
    public SparksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.spark_video_list_item, parent, false);
        return new SparksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SparksViewHolder holder, final int position) {
        final String path = videoPaths.get(position);

        // Load thumbnail in background
        new Thread(new Runnable() {
				@Override
				public void run() {
					final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
					((android.app.Activity) context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								holder.thumbnailView.setImageBitmap(thumbnail != null ? thumbnail : getDefaultVideoThumbnail());
							}
						});
				}
			}).start();

        // Set video duration
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMs = Long.parseLong(time);
            String formatted = String.format("%02d:%02d", 
											 (durationMs / 1000) / 60, 
											 (durationMs / 1000) % 60);
            holder.videoDuration.setText(formatted);
        } catch (Exception e) {
            holder.videoDuration.setText("00:00");
        } finally {
            retriever.release();
        }

        // Set play button overlay
        holder.playButton.setVisibility(View.VISIBLE);

        // Click to play video and hide delete button if visible
        holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (holder.deleteButton.getVisibility() == View.VISIBLE) {
						holder.deleteButton.setVisibility(View.GONE);
					} else {
						new DialogGeneralVideoViewHelper().showVideoDialog(context, path);
					}
				}
			});

        // Long click to toggle delete button visibility
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					holder.deleteButton.setVisibility(
						holder.deleteButton.getVisibility() == View.VISIBLE 
						? View.GONE 
						: View.VISIBLE);
					return true;
				}
			});

        // Hide delete button when clicking elsewhere
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN && 
						holder.deleteButton.getVisibility() == View.VISIBLE) {
						android.graphics.Rect rect = new android.graphics.Rect();
						holder.deleteButton.getGlobalVisibleRect(rect);
						if (!rect.contains((int)event.getRawX(), (int)event.getRawY())) {
							holder.deleteButton.setVisibility(View.GONE);
						}
					}
					return false;
				}
			});

        // Delete button click
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (deleteListener != null) {
						deleteListener.onRequestDelete(position, path);
					}
				}
			});

        // Set separator visibility
        holder.separator.setVisibility(position < videoPaths.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private Bitmap getDefaultVideoThumbnail() {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

    @Override
    public int getItemCount() {
        return videoPaths.size();
    }

    public void removePathAt(int position) {
        if (position >= 0 && position < videoPaths.size()) {
            videoPaths.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class SparksViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;
        TextView videoDuration;
        ImageButton deleteButton;
        ImageView playButton;
        View separator;

        public SparksViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.sparkVideoThumbnail);
            videoDuration = (TextView) view.findViewById(R.id.videoDuration);
            deleteButton = (ImageButton) view.findViewById(R.id.sparkVideoDeleteButton);
            playButton = (ImageView) view.findViewById(R.id.playButton);
            separator = view.findViewById(R.id.separator);

            // Ensure separator is visible
            separator.setBackgroundColor(Color.argb(51, 0, 0, 0)); // #33000000
        }
    }
}
