package com.habosa.yoursquare.task;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.habosa.yoursquare.model.Place;
import com.habosa.yoursquare.model.PlacesSource;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static final void importFromFile(File file) {
        // TODO(samstern): Implement
    }

    @WorkerThread
    private static File exportJsonToFile(Context context) throws IOException {
        File exportDir = context.getExternalFilesDir("export");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        // Create file name
        String dateString = FORMAT.format(new Date());
        String fileName = "yoursquare-" + dateString + ".json";

        // Make new file
        File exportFile = new File(exportDir, fileName);
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

}
