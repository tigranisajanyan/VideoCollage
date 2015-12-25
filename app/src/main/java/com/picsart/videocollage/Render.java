package com.picsart.videocollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Tigran on 7/23/15.
 */
public class Render extends AsyncTask<Void, Integer, Void> {

    private Context context;

    private String inputImagesDir1 = Environment.getExternalStorageDirectory().getPath() + "/VideoCollage/first_video/";
    private String inputImagesDir2 = Environment.getExternalStorageDirectory().getPath() + "/VideoCollage/second_video/";

    File[] files1 = new File(inputImagesDir1).listFiles();
    File[] files2 = new File(inputImagesDir2).listFiles();

    int x = Math.min(files1.length, files2.length);

    private static OnRenderFinishedListener onRenderFinishedListener;

    public Render(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int i = 0; i < x; i++) {
            storeImage(merge(files1[i].getAbsolutePath(), files2[i].getAbsolutePath()), "img_" + i + ".jpg");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        onRenderFinishedListener.onFinish(true);
    }


    /**
     * Merging two images
     *
     * @param path1 first image path
     * @param path2 second image path
     * @return
     */
    private Bitmap merge(String path1, String path2) {

        Bitmap bitmap1 = ImageLoader.getInstance().loadImageSync(FileUtils.FILE_PREFIX + path1);
        Bitmap bitmap2 =  ImageLoader.getInstance().loadImageSync(FileUtils.FILE_PREFIX + path2);

        Bitmap mergedBitmap = Bitmap.createBitmap(960, 640, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);
        canvas.drawBitmap(bitmap1, 0, 0, null);
        canvas.drawBitmap(bitmap2, 480, 0, null);

        return mergedBitmap;
    }

    public void setOnRenderFinishedListener(OnRenderFinishedListener l) {
        onRenderFinishedListener = l;
    }

    public interface OnRenderFinishedListener {
        void onFinish(boolean finished);
    }

    private boolean storeImage(Bitmap imageData, String filename) {
        //get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory() + "/VideoCollage/output/" + filename;
        File sdIconStorageDir = new File(iconsStoragePath);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(sdIconStorageDir);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return false;
        }
        return true;
    }

}
