package com.habosa.yoursquare;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    private static final String TAG = "PlacesAdapter";

    private PlacesSource mSource;
    private Cursor mCursor;

    public PlacesAdapter(PlacesSource source) {
        mSource = source;
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
        Log.d(TAG, "onBindViewHolder:" + position);

        // Get Place from Cursor
        mCursor.moveToPosition(position);
        final Place p = mSource.fromCursor(mCursor);
        final Place.Decorator pd = new Place.Decorator(p);

        // Display place info
        holder.titleView.setText(p.getName());
        holder.addressView.setText(p.getAddress());
        holder.tagsView.setText(pd.getTagsString());

        // Click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO(samstern): How to make this "smooth"?
                mSource.delete(p);
                reloadItems();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void reloadItems() {
        // TODO(samstern): is this method necessary?
        mCursor = mSource.getAll();
        notifyDataSetChanged();
    }

    public void setCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = cursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView addressView;
        public TextView tagsView;
        public View deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.place_text_title);
            addressView = (TextView) itemView.findViewById(R.id.place_text_address);
            tagsView = (TextView) itemView.findViewById(R.id.place_tags);
            deleteButton = itemView.findViewById(R.id.place_button_delete);
        }
    }

}
