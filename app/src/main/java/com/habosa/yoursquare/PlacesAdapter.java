package com.habosa.yoursquare;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.task.LoadPlaceImageTask;

import java.io.File;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    private static final String TAG = "PlacesAdapter";

    private Context mContext;
    private OnItemRemovedListener mListener;

    private boolean mDataValid = false;
    private Cursor mCursor;
    private ContentObserver mObserver;

    public interface OnItemRemovedListener {
        void onPlaceRemoved(Place p);
    }

    public PlacesAdapter(Context context, OnItemRemovedListener listener) {
        mContext = context;
        mListener = listener;

        // Stable IDs
        setHasStableIds(true);

        // Initialize ContentObserver
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                Log.d(TAG, "onChange:" + selfChange);

                // Invalidate data, wait for new Cursor
                mDataValid = false;
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
        if (!hasData()) {
            throw new IllegalStateException("Can't bind ViewHolder: invalid data.");
        }

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

        // TODO(samstern): holder.imageView can move before this tasks finishes, make sure
        //                 we load into the right view by finding by ID.
        LoadPlaceImageTask.load(mContext, p.getGooglePlaceId())
                .addOnSuccessListener(new OnSuccessListener<File>() {
                    @Override
                    public void onSuccess(File file) {
                        // Load the file into the imageview
                        Glide.with(holder.imageView.getContext())
                                .fromFile()
                                .fitCenter()
                                .crossFade()
                                .load(file)
                                .into(holder.imageView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "image:onFailure", e);
                    }
                });

        // Delete click listener
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlaceRemoved(p);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        if (!hasData()) {
            return 0;
        }

        mCursor.moveToPosition(position);
        return Place.fromCursor(mCursor).getId();
    }

    @Override
    public int getItemCount() {
        if (!hasData()) {
            return 0;
        }

        return mCursor.getCount();
    }

    private boolean hasData() {
        return (mCursor != null && mDataValid);
    }

    public void setCursor(Cursor cursor) {
        Log.d(TAG, "setCursor");
        if (mCursor != null) {
            mCursor.close();
            mCursor.unregisterContentObserver(mObserver);
        }

        mCursor = cursor;
        mCursor.registerContentObserver(mObserver);
        mDataValid = true;
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

            titleView = itemView.findViewById(R.id.place_text_title);
            addressView = itemView.findViewById(R.id.place_text_address);
            imageView = itemView.findViewById(R.id.place_image);
            tagsView = itemView.findViewById(R.id.place_tags);
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
