package com.picsart.videocollage.utils;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.File;

/**
 * Created by Tigran on 12/23/15.
 */
public class FileUtils {

    public static String FILE_PREFIX = "file://";

    public static void clearDir(File dir) {
        try {
            File[] files = dir.listFiles();
            if (files != null)
                for (File f : files) {
                    if (f.isDirectory())
                        clearDir(f);
                    f.delete();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createDir(String fileName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + fileName);
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
    }

    public static void createAndClearDir(String fileName){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + fileName);
        if (!myDir.exists()) {
            myDir.mkdirs();
        } else {
            clearDir(myDir);
            File file = new File(myDir.toString());
            file.delete();
            myDir.mkdirs();
        }
    }

    public static Drawable changeDrawableColor(Context context, int color, int id) {
        Drawable tubeBg = context.getResources().getDrawable(id);
        // bug 2.1 and lower StateListDrawable.mutate() throws
        // NullPointerException
        try {
            tubeBg.mutate();
        } catch (NullPointerException e) {
        }
        tubeBg.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        return tubeBg;
    }

    public static int checkVideoFrameRate(int frameCount, int videoDuration) {
        return videoDuration == 0 ? 35 : ((1000 * videoDuration) / (frameCount));
    }

}
