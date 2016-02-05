package com.habosa.yoursquare;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "PlacesAdapter";

    private Context mContext;
    private PlacesSource mSource;
    private GoogleApiClient mGoogleApiClient;

    private CursorLoader mCursorLoader;
    private Cursor mCursor;
    private String mQuery = null;

    public PlacesAdapter(Context context, PlacesSource source, GoogleApiClient googleApiClient) {
        mContext = context;
        mSource = source;
        mGoogleApiClient = googleApiClient;
        mCursor = source.getAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.item_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // Get Place from Cursor
        mCursor.moveToPosition(position);
        final Place p = mSource.fromCursor(mCursor);
        final Place.Decorator pd = new Place.Decorator(p);

        // Display place info
        holder.titleView.setText(p.getName());
        holder.addressView.setText(p.getAddress());
        holder.tagsView.setText(pd.getTagsString());

        // Load image
        int gray = ContextCompat.getColor(holder.imageView.getContext(), android.R.color.darker_gray);
        holder.imageView.setImageDrawable(null);
        holder.imageView.setBackgroundColor(gray);
        new LoadPlaceImageTask(p.getGooglePlaceId(), holder.imageView, mGoogleApiClient).execute();

        // Delete click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {

            final int mPosition = position;

            @Override
            public void onClick(View v) {
                // Delete record and cached place picture.
                mSource.delete(p);
                PlaceImageUtil.deleteImageFile(v.getContext(), p.getGooglePlaceId());

                // Reload cursor
                // TODO(samstern): This should be done by a listener, we should only be concerned
                //                 with the UI logic (notifyItemRemoved) at this point
                setCursor(mSource.getAll());
                notifyItemRemoved(mPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        return mSource.getLoader(mContext, mQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO(samstern): What to do here?
        Log.d(TAG, "onLoaderReset");
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
    }

}
