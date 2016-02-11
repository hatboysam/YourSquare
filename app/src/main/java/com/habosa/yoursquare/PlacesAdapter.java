package com.habosa.yoursquare;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.habosa.yoursquare.model.Place;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    private static final String TAG = "PlacesAdapter";

    private GoogleApiClient mGoogleApiClient;
    private Cursor mCursor;
    private OnItemRemovedListener mListener;
    private ContentObserver mObserver;

    public interface OnItemRemovedListener {
        void onPlaceRemoved(Place p);
    }

    public PlacesAdapter(GoogleApiClient googleApiClient, OnItemRemovedListener listener) {
        mGoogleApiClient = googleApiClient;
        mListener = listener;

        // Initialize ContentObserver
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "onChange:" + selfChange);
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.item_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // Clear ViewHolder
        holder.clear();

        // Get Place from Cursor
        mCursor.moveToPosition(position);
        final Place p = Place.fromCursor(mCursor);
        final Place.Decorator pd = new Place.Decorator(p);

        // Display place info
        holder.titleView.setText(p.getName());
        holder.addressView.setText(p.getAddress());
        holder.tagsView.setText(pd.getTagsString());

        // Load image
        int gray = ContextCompat.getColor(holder.imageView.getContext(), android.R.color.darker_gray);
        holder.imageView.setImageDrawable(null);
        holder.imageView.setBackgroundColor(gray);
        new LoadPlaceImageTask(p.getGooglePlaceId(), holder.imageView, mGoogleApiClient)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Delete click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaceRemoved(p);
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public long getItemId(int position) {
        if (mCursor == null) {
            return 0;
        }

        mCursor.moveToPosition(position);
        return Place.fromCursor(mCursor).getId();
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }

        return mCursor.getCount();
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
            mCursor.unregisterContentObserver(mObserver);
        }

        mCursor = cursor;
        mCursor.registerContentObserver(mObserver);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView addressView;
        public ImageView imageView;
        public TextView tagsView;
        public View deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.place_text_title);
            addressView = (TextView) itemView.findViewById(R.id.place_text_address);
            imageView = (ImageView) itemView.findViewById(R.id.place_image);
            tagsView = (TextView) itemView.findViewById(R.id.place_tags);
            deleteButton = itemView.findViewById(R.id.place_button_delete);
        }

        public void clear() {
            titleView.setText("...");
            addressView.setText("...");
            imageView.setImageDrawable(null);
            tagsView.setText(null);
        }
    }

}
