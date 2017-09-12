package com.habosa.yoursquare.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlacesOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
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

    public static Task<File> load(final Context context, final String googlePlaceId) {
        final TaskCompletionSource<File> source = new TaskCompletionSource<>();

        TaskThreads.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                File imageFile = loadImageFile(context, googlePlaceId);
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
    private static File loadImageFile(Context context, String googlePlaceId) {
        GeoDataClient client = Places.getGeoDataClient(context,
                new PlacesOptions.Builder().build());

        File imgCacheFile = PlaceImageUtil.getImageFile(context, googlePlaceId);
        if (imgCacheFile.exists()) {
            // Log.d(TAG, "Returning image from cache:" + imgCache.getAbsolutePath());
            return imgCacheFile;
        }

        PlacePhotoMetadataBuffer buffer;
        try {
            buffer = Tasks.await(client.getPlacePhotos(googlePlaceId)).getPhotoMetadata();
        } catch (Exception e) {
            Log.w(TAG, "PhotoMetadata load failed.", e);
            return null;
        }

        if (buffer.getCount() < 1) {
            Log.d(TAG, "PhotoMetadataBuffer empty");
            return null;
        }


        PlacePhotoResponse photoRes;
        try {
            photoRes = Tasks.await(client.getPhoto(buffer.get(0)));
        } catch (Exception e) {
            Log.w(TAG, "PlacePhoto load failed", e);
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
            buffer.release();
        } catch (IOException e) {
            Log.e(TAG, "Error saving file to " + imgCacheFile.getAbsolutePath(), e);
            return null;
        }

        return imgCacheFile;
    }
}
