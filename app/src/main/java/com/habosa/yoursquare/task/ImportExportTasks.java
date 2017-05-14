package com.habosa.yoursquare.task;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;
import com.habosa.yoursquare.provider.PlacesProvider;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tasks to import and export places to JSON.
 */
@SuppressLint("MissingPermission")
public class ImportExportTasks {

    private static final String TAG = "ImportExport";

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault());

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static final Task<File> exportToFile(final Context context) {
        final TaskCompletionSource<File> source = new TaskCompletionSource<>();

        TaskThreads.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = exportJsonToFile(context);
                    source.setResult(file);
                } catch (IOException e) {
                    source.setException(e);
                }
            }
        });

        return source.getTask();
    }

    public static final Task<Void> importFromFile(final Context context, final Uri fileUri) {
        final TaskCompletionSource<Void> source = new TaskCompletionSource<>();

        TaskThreads.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get list of places from the file
                    List<Place> places = placeListFromFile(context, fileUri);
                    Log.d(TAG, "Found " + places.size() + " places");

                    // Delete all places
                    context.getContentResolver().delete(PlacesProvider.CONTENT_URI, null, null);

                    // Add all places back
                    PlacesSource placesSource = new PlacesSource(context);
                    for (Place place : places) {
                        placesSource.create(place);
                    }

                    // Finish the task
                    source.setResult(null);
                } catch (IOException e) {
                    source.setException(e);
                }
            }
        });

        return source.getTask();
    }

    @WorkerThread
    private static List<Place> placeListFromFile(Context context, Uri fileUri) throws IOException {
        // Get the input stream
        InputStream stream = context.getContentResolver().openInputStream(fileUri);
        String fileContent = IOUtils.toString(stream, Charset.defaultCharset());

        // Read from JSON
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<List<Place>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, Place.class));
        return jsonAdapter.fromJson(fileContent);
    }

    @WorkerThread
    private static File exportJsonToFile(Context context) throws IOException {
        // Create file name
        String dateString = FORMAT.format(new Date());
        String fileName = "yoursquare-" + dateString + ".json";

        // Make new file
        File exportFile = new File(getDirectory(), fileName);
        if (!exportFile.exists()) {
            exportFile.createNewFile();
        }

        // Load all places
        PlacesSource placesSource = new PlacesSource(context);
        Cursor cursor = placesSource.query(null);

        // Get all places into array
        List<Place> places = new ArrayList<>(cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            places.add(Place.fromCursor(cursor));
        }

        // Convert to JSON
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<List<Place>> jsonAdapter = moshi.adapter(Types.newParameterizedType(List.class, Place.class));
        String json = jsonAdapter.toJson(places);

        // Write to file
        FileWriter writer = new FileWriter(exportFile);
        writer.write(json);
        writer.close();

        return exportFile;
    }

    public static File getDirectory() {
        File file = new File(Environment.getExternalStorageDirectory(), "YourSquare");
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

}
