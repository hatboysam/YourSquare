package com.habosa.yoursquare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PlacesActivity extends AppCompatActivity implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.OnConnectionFailedListener,
        EasyPermissions.PermissionCallbacks {

    private static final String TAG = "PlacesActivity";

    private static final String KEY_IS_SEARCHING = "key_is_searching";

    private static final int RC_PLACE_PICKER = 101;
    private static final int RC_PERMISSIONS = 102;
    private static final int RC_PLAY_SERVICES_ERROR = 103;

    private static final int LOADER_PLACES = 0;

    private static final String[] PERMS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private PlacesSource mPlacesSource;
    private PlacesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mFab;
    private RecyclerView mRecycler;
    private EditText mSearchField;
    private View mEndSearchButton;

    // TODO(samstern): rotation persist query
    private String mSearchQuery;
    private boolean mIsSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Restore saved instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

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

        // Set up RecyclerView
        mAdapter = new PlacesAdapter(mGoogleApiClient, new PlacesAdapter.OnItemRemovedListener() {
            @Override
            public void onPlaceRemoved(Place p) {
                Log.d(TAG, "onPlaceRemoved:" + p.getName());
                // Delete record and cached place picture.
                // TODO(samstern): Should I be doing this off the UI thread?
                mPlacesSource.delete(p);
                PlaceImageUtil.deleteImageFile(PlacesActivity.this, p.getGooglePlaceId());

                // Reload cursor
                changeQuery(null);
            }
        });
        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mLayoutManager);
        mRecycler.setAdapter(mAdapter);

        // Set up Loader and hook it up to PlacesAdapter
        getSupportLoaderManager().initLoader(LOADER_PLACES, null, this);

        // Click listener(s)
        mFab.setOnClickListener(this);
        mEndSearchButton.setOnClickListener(this);

        // Search text change listener
        mSearchField.addTextChangedListener(new DebouncingWatcher(200) {
            @Override
            public void onNewText(String text) {
                Log.d(TAG, "onNewText:" + text);
                text = text.trim();
                if (!TextUtils.isEmpty(text)) {
                    // Search for entered text
                    changeQuery(mPlacesSource.getFuzzySearchQuery(text));
                } else {
                    // Search for all
                    changeQuery(null);
                }
            }
        });

        // Search text key listener
        mSearchField.setOnEditorActionListener(new EnterListener() {
            @Override
            public void onEnter() {
                hideKeyboard();
            }
        });
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mIsSearching = inState.getBoolean(KEY_IS_SEARCHING, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_SEARCHING, mIsSearching);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PLACE_PICKER) {
            Log.d(TAG, "RC_PLACE_PICKER:" + resultCode + ":" + data);
            if (resultCode == RESULT_OK) {
                // Extract gms "Place"
                com.google.android.gms.location.places.Place place = PlacePicker.getPlace(this, data);

                // Convert to app notion of "Place"
                Place myPlace = new Place();
                myPlace.setGooglePlaceId(place.getId());
                myPlace.setName(place.getName().toString());
                myPlace.setAddress(place.getAddress().toString());
                myPlace.setLat(place.getLatLng().latitude);
                myPlace.setLng(place.getLatLng().longitude);
                mPlacesSource.create(myPlace);

                // Restart the loader
                changeQuery(null);

                // Scroll to top
                mRecycler.smoothScrollToPosition(0);
            } else if (resultCode != RESULT_CANCELED) {
                Toast.makeText(this, "Error opening Place Picker", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {
        if (mIsSearching) {
            endSearch();
        } else {
            super.onBackPressed();
        }
    }

    @AfterPermissionGranted(RC_PERMISSIONS)
    private void onFabClicked() {
        // Check for location and storage permissions
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.location_storage_rationale),
                    RC_PERMISSIONS,
                    PERMS);
            return;
        }

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(this);
            startActivityForResult(intent, RC_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            // Repairable Play Services error, show the dialog.
            Log.e(TAG, "Place Picker: GMS Repairable", e);
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this,
                    RC_PLAY_SERVICES_ERROR).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Unrepairable Play Services error, just display a Toast.
            Log.e(TAG, "Place Picker: GMS Not Available", e);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeQuery(String query) {
        // Restart the Loader with a new query
        mSearchQuery = query;
        getSupportLoaderManager().restartLoader(LOADER_PLACES, null, this);
    }

    private void beginSearch() {
        mIsSearching = true;

        // Show the search field
        View searchLayout = findViewById(R.id.layout_search);
        searchLayout.setVisibility(View.VISIBLE);

        // Hide the add button
        ViewCompat.animate(mFab)
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(300)
                .start();

        // Request typing focus
        mSearchField.requestFocus();
        showKeyboard(mSearchField);
    }

    private void endSearch() {
        mIsSearching = false;

        // Clear search term from field (if it exists, to avoid flicker)
        if (!TextUtils.isEmpty(mSearchField.getText().toString().trim())) {
            mSearchField.setText(null);
        }

        // Hide the search field
        View searchLayout = findViewById(R.id.layout_search);
        searchLayout.setVisibility(View.GONE);

        // Hide the keyboard
        hideKeyboard();

        // Show the add button
        ViewCompat.animate(mFab)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300)
                .start();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard(View focusView) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(focusView, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        return mPlacesSource.getLoader(this, mSearchQuery);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        mAdapter.setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO(samstern): What to do here?
        Log.d(TAG, "onLoaderReset");
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
                // TODO(samstern): settings screen
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

    @Override
    public void onPermissionsGranted(List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + perms);
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + perms);
    }
}
