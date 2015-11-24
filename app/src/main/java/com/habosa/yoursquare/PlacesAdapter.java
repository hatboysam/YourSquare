package com.habosa.yoursquare;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habosa.yoursquare.model.Place;

import java.util.Arrays;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.ViewHolder> {

    private static final String TAG = "PlacesAdapter";

    private List<Place> mPlaces = Arrays.asList(
            new Place("Pete's BBQ", "2322 Mission Street, San Francisco", Arrays.asList("cheap", "bbq", "mission")),
            new Place("Taqueria El Buen Sabor", "978 Valencia Street, San Francisco", Arrays.asList("mexican", "taco", "burrito")),
            new Place("Arinell Pizzeria", "506 Valencia Street, San Francisco", Arrays.asList("pizza", "cheap", "slice")),
            new Place("Latin American Club ", "896 22nd Street, San Francisco", Arrays.asList("bar", "margarita", "dive"))
    );

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.item_place, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder:" + position);

        Place p = mPlaces.get(position);
        Place.Decorator pd = new Place.Decorator(p);

        holder.titleView.setText(p.getTitle());
        holder.addressView.setText(p.getAddress());
        holder.tagsView.setText(pd.getTagsString());
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView titleView;
        public TextView addressView;
        public TextView tagsView;

        public ViewHolder(View itemView) {
            super(itemView);

            titleView = (TextView) itemView.findViewById(R.id.place_text_title);
            addressView = (TextView) itemView.findViewById(R.id.place_text_address);
            tagsView = (TextView) itemView.findViewById(R.id.place_tags);
        }
    }

}
