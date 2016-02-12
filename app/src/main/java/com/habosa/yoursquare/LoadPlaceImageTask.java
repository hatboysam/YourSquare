package com.habosa.yoursquare;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Load an image for a Place into an ImageView. Checks cache first, downloads image from
 * Google Places API if no cache entry exists.
 */
public class LoadPlaceImageTask extends AsyncTask<Void, Void, File> {

    private static final String TAG = "LoadPlaceImageTask";

    private String mGooglePlaceId;
    private ImageView mTarget;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    public LoadPlaceImageTask(String googlePlaceId, ImageView target, GoogleApiClient googleApiClient) {
        this.mGooglePlaceId = googlePlaceId;
        this.mTarget = target;
        this.mGoogleApiClient = googleApiClient;

        mContext = mTarget.getContext();
    }

    @Override
    protected File doInBackground(Void... params) {
        File imgCacheFile = PlaceImageUtil.getImageFile(mContext, mGooglePlaceId);
        if (imgCacheFile.exists()) {
            // Log.d(TAG, "Returning image from cache:" + imgCache.getAbsolutePath());
            return imgCacheFile;
        }

        PlacePhotoMetadataResult res = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, mGooglePlaceId).await();
        if (!res.getStatus().isSuccess() || res.getPhotoMetadata().getCount() < 1) {
            Log.d(TAG, "PhotoMetaDataResult failed or empty.");
            return null;
        }

        PlacePhotoResult photoRes = res.getPhotoMetadata().get(0).getPhoto(mGoogleApiClient).await();
        if (!photoRes.getStatus().isSuccess()) {
            Log.d(TAG, "PlacePhotoResult failed");
            return null;
        }

        // Write bitmap to file
        Bitmap bitmap = photoRes.getBitmap();
        try {
            // Create file
            imgCacheFile = PlaceImageUtil.createImageFile(mContext, mGooglePlaceId);

            // Write bitmap
            Log.d(TAG, "Writing new image to:" + imgCacheFile.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(imgCacheFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Close photo metadata
            res.getPhotoMetadata().release();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file to " + imgCacheFile.getAbsolutePath(), e);
            return null;
        }

        return imgCacheFile;
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) {
            Log.w(TAG, "onPostExecute: null result");
            // TODO(samstern): Placeholder image. Need a few separate resources:
            //                  1) "Loading" placeholder
            //                  2) No image found placeholder
            return;
        }

        Glide.with(mTarget.getContext())
                .fromFile()
                .crossFade()
                .load(result)
                .into(mTarget);
    }
}
