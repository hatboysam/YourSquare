package com.habosa.yoursquare.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.habosa.yoursquare.util.PlaceImageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Load an image for a Place into an ImageView. Checks cache first, downloads image from
 * Google Places API if no cache entry exists.
 */
public class LoadPlaceImageTask {

    private static final String TAG = "LoadPlaceImageTask";

    public static Task<File> load(final Context context, final GoogleApiClient client,
                                  final String googlePlaceId) {
        final TaskCompletionSource<File> source = new TaskCompletionSource<>();

        TaskThreads.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                File imageFile = loadImageFile(context, client, googlePlaceId);
                if (imageFile != null) {
                    source.setResult(imageFile);
                } else {
                    source.setException(new Exception("Failed to load file: " + googlePlaceId));
                }
            }
        });


        return source.getTask();
    }

    @WorkerThread
    private static File loadImageFile(Context context, GoogleApiClient client, String googlePlaceId) {
        File imgCacheFile = PlaceImageUtil.getImageFile(context, googlePlaceId);
        if (imgCacheFile.exists()) {
            // Log.d(TAG, "Returning image from cache:" + imgCache.getAbsolutePath());
            return imgCacheFile;
        }

        PlacePhotoMetadataResult res = Places.GeoDataApi.getPlacePhotos(client, googlePlaceId).await();
        if (!res.getStatus().isSuccess() || res.getPhotoMetadata().getCount() < 1) {
            Log.d(TAG, "PhotoMetaDataResult failed or empty.");
            return null;
        }

        PlacePhotoResult photoRes = res.getPhotoMetadata().get(0).getPhoto(client).await();
        if (!photoRes.getStatus().isSuccess()) {
            Log.d(TAG, "PlacePhotoResult failed");
            return null;
        }

        // Write bitmap to file
        Bitmap bitmap = photoRes.getBitmap();
        try {
            // Create file
            imgCacheFile = PlaceImageUtil.createImageFile(context, googlePlaceId);

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
}
