package com.habosa.yoursquare;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

import java.util.ArrayList;
import java.util.Random;

public class PlacesActivity extends AppCompatActivity {

    private PlacesSource mPlacesSource;

    private FloatingActionButton mFab;
    private RecyclerView mRecycler;
    private PlacesAdapter mAdapter;
    private EditText mSearchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Views
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mRecycler = (RecyclerView) findViewById(R.id.recycler_view_places);
        mSearchField = (EditText) findViewById(R.id.edit_text_search);

        // Open Database
        mPlacesSource = new PlacesSource(this);
        mPlacesSource.open();

        // Set up RecyclerView
        mAdapter = new PlacesAdapter(mPlacesSource);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(mAdapter);

        // Click listener
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rand = (new Random()).nextInt(100);
                String title = "Random Place " + rand;
                Place place = new Place(title, "123 Jamaica Street, AmericaTown", new ArrayList<String>());
                mPlacesSource.create(place);
                mAdapter.reloadItems();
            }
        });
    }

    private void beginSearch() {
        // Show the search field
        mSearchField.setVisibility(View.VISIBLE);
        mSearchField.setAlpha(0.0f);
        ViewCompat.animate(mSearchField)
                .alpha(1.0f)
                .setDuration(300)
                .start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                // TODO
                return true;
            case R.id.action_search:
                beginSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
