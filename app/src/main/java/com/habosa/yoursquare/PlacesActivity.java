package com.habosa.yoursquare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

public class PlacesActivity extends AppCompatActivity implements
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PlacesActivity";
    private static final int RC_PLACE_PICKER = 16001;
    private static final int RC_PERMISSIONS = 16002;

    private PlacesSource mPlacesSource;
    private PlacesAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mFab;
    private RecyclerView mRecycler;
    private EditText mSearchField;
    private View mEndSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        // Initialize Views
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mRecycler = (RecyclerView) findViewById(R.id.recycler_view_places);
        mSearchField = (EditText) findViewById(R.id.edit_text_search);
        mEndSearchButton = findViewById(R.id.button_search_back);

        // Open Database
        mPlacesSource = new PlacesSource(this);
        mPlacesSource.open();

        // Set up RecyclerView
        mAdapter = new PlacesAdapter(mPlacesSource, mGoogleApiClient);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(mAdapter);

        // Click listener(s)
        mFab.setOnClickListener(this);
        mEndSearchButton.setOnClickListener(this);

        // Search text change listener
        mSearchField.addTextChangedListener(new DebouncingWatcher() {
            @Override
            public void onNewText(String text) {
                Log.d(TAG, "onNewText:" + text);
                // TODO(samstern): act on empty, end search, one line, trim, etc
                text = text.trim();
                if (!TextUtils.isEmpty(text)) {
                    mAdapter.setCursor(mPlacesSource.fuzzySearch(text));
                } else {
                    mAdapter.setCursor(mPlacesSource.getAll());
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PLACE_PICKER) {
            Log.d(TAG, "RC_PLACE_PICKER:" + resultCode + ":" + data);
            if (resultCode == RESULT_OK) {
                // Extract gms "Place"
                com.google.android.gms.location.places.Place place = PlacePicker.getPlace(data, this);

                // Convert to app notion of "Place"
                Place myPlace = new Place();
                myPlace.setGooglePlaceId(place.getId());
                myPlace.setName(place.getName().toString());
                myPlace.setAddress(place.getAddress().toString());
                mPlacesSource.create(myPlace);
                mAdapter.reloadItems();
            } else {
                // TODO(samstern): Handle
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == RC_PERMISSIONS) {
            boolean gotAllPermissions = true;
            for (int i : grantResults) {
                gotAllPermissions = gotAllPermissions && (i == PackageManager.PERMISSION_GRANTED);
            }

            if (!gotAllPermissions) {
                Toast.makeText(this, "Error: insufficient permissions.", Toast.LENGTH_SHORT).show();

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void onFabClicked() {
        if (!hasPermissions()) {
            requestPermissions();
            return;
        }

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(this);
            startActivityForResult(intent, RC_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO(samstern): Handle
            Log.e(TAG, "Place Picker: GMS Repairable", e);
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO(samstern): Handle
            Log.e(TAG, "Place Picker: GMS Not Available", e);
        }
    }

    private void beginSearch() {
        // Show the search field
        View searchLayout = findViewById(R.id.layout_search);
        searchLayout.setVisibility(View.VISIBLE);
        searchLayout.setAlpha(0.0f);
        ViewCompat.animate(searchLayout)
                .alpha(1.0f)
                .setDuration(300)
                .start();
    }

    private void endSearch() {
        // Hide the search field
        View searchLayout = findViewById(R.id.layout_search);
        searchLayout.setVisibility(View.GONE);
        ViewCompat.animate(searchLayout)
                .alpha(0.0f)
                .setDuration(300)
                .start();
    }

    private boolean hasPermissions() {
        int locRes = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int storageRes = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (locRes == PackageManager.PERMISSION_GRANTED && storageRes == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermissions() {
        final String locPerm = Manifest.permission.ACCESS_FINE_LOCATION;
        final String storagePerm = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        final String[] allPerms = new String[]{ locPerm, storagePerm };

        boolean explainLocation = ActivityCompat.shouldShowRequestPermissionRationale(this, locPerm);
        boolean explainStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, storagePerm);

        if (explainLocation || explainStorage) {
            String rationale = "YourSquare needs to access and save location data to pick places.";
            View layout = findViewById(R.id.layout_places_root);
            Snackbar.make(layout, rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(PlacesActivity.this,
                                    allPerms, RC_PERMISSIONS);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, allPerms, RC_PERMISSIONS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlacesSource.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                onFabClicked();
                break;
            case R.id.button_search_back:
                endSearch();
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
    }
}
