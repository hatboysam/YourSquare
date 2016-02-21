package com.habosa.yoursquare;

import android.Manifest;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class PlacesMapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.OnConnectionFailedListener,
        EasyPermissions.PermissionCallbacks, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "PlacesMap";
    private static final int LOADER_PLACES = 0;
    private static final int RC_LOCATION_PERM = 101;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlacesSource mPlacesSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_map);

        // Places
        mPlacesSource = new PlacesSource(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;

        // GoogleApiClient for Location
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .build();

        // Begin loading places
        getSupportLoaderManager().initLoader(LOADER_PLACES, null, this);
    }

    @SuppressWarnings("ResourceType") // Ignore permission linter
    private void enableLocation() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable location button
            mMap.setMyLocationEnabled(true);

            // Go to current location
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                Log.d(TAG, "location:" + location.toString());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 15));
            } else {
                Log.w(TAG, "location:null");
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    "YourSquare wants to show your position on the map.",
                    RC_LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader:" + id);
        return mPlacesSource.getLoader(this, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished");
        for (int i = 0; i < data.getCount(); i++) {
            if (!data.moveToPosition(i)) {
                Log.w(TAG, "moveToPostion failed:" + i);
                continue;
            }

            // Get place from Cursor
            Place p = Place.fromCursor(data);

            // Add marker
            Log.d(TAG, "addMarker:" + p);
            LatLng ll = new LatLng(p.getLat(), p.getLng());
            mMap.addMarker(new MarkerOptions().position(ll).title(p.getName()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        enableLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult.getErrorMessage());
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {}

    @Override
    public void onPermissionsDenied(List<String> perms) {}
}
