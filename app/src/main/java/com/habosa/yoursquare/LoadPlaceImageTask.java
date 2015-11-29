package com.habosa.yoursquare;

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
 * Created by samstern on 11/25/15.
 */
public class LoadPlaceImageTask extends AsyncTask<Void, Void, File> {

    private static final String TAG = "LoadPlaceImageTask";

    private String mPlaceId;
    private ImageView mTarget;
    private GoogleApiClient mGoogleApiClient;

    public LoadPlaceImageTask(String placeId, ImageView target, GoogleApiClient googleApiClient) {
        this.mPlaceId = placeId;
        this.mTarget = target;
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected File doInBackground(Void... params) {
        File imgCache = getImageFile();
        if (imgCache.exists()) {
            Log.d(TAG, "Returning image from cache:" + imgCache.getAbsolutePath());
            return imgCache;
        }

        PlacePhotoMetadataResult res = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, mPlaceId).await();
        if (!res.getStatus().isSuccess() || res.getPhotoMetadata().getCount() < 1) {
            Log.d(TAG, "PhotoMetaDataResult failed or empty.");
            return null;
        }

        PlacePhotoResult photoRes = res.getPhotoMetadata().get(0)
                .getPhoto(mGoogleApiClient).await();
        if (!photoRes.getStatus().isSuccess()) {
            Log.d(TAG, "PlacePhotoResult failed");
            return null;
        }

        // Write bitmap to file
        Bitmap bitmap = photoRes.getBitmap();
        try {
            imgCache.getParentFile().mkdirs();
            imgCache.createNewFile();

            FileOutputStream fos = new FileOutputStream(imgCache);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file to " + imgCache.getAbsolutePath(), e);
            return null;
        }

        return imgCache;
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) {
            // TODO(samstern): Placeholder image. Need a few separate resources:
            //                  1) "Loading" placeholder
            //                  2) No image found placeholder
            return;
        }

        Glide.with(mTarget.getContext())
                .fromFile()
                .asBitmap()
                .load(result)
                .into(mTarget);
    }

    private File getImageFile() {
        File dir = mTarget.getContext().getFilesDir();
        File file = new File(dir, "cache/img/" + mPlaceId);

        return  file;
    }
}