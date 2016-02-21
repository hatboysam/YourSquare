package com.habosa.yoursquare.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * Created by samstern on 1/1/16.
 */
public class PlaceImageUtil {

    public static File getImageFile(Context context, String placeId) {
        File dir = context.getFilesDir();
        File file = new File(dir, "cache/img/" + placeId);

        return  file;
    }

    public static File createImageFile(Context context, String placeId) throws IOException {
        File file = getImageFile(context, placeId);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return file;
    }

    public static void deleteImageFile(Context context, String placeId) {
        File file = getImageFile(context, placeId);
        if (file.exists()) {
            file.delete();
        }
    }

}
