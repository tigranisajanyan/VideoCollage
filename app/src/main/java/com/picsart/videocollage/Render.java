package com.picsart.videocollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.socialin.android.encoder.Encoder;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


/**
 * Created by Tigran on 7/23/15.
 */
public class Render {

    private Context context;
    private static final String root = Environment.getExternalStorageDirectory().toString();

    private String outputImagesDir1 = Environment.getExternalStorageDirectory().getPath() + "/VideoCollage/first_video/";
    private String outputImagesDir2 = Environment.getExternalStorageDirectory().getPath() + "/VideoCollage/second_video/";

    private int frameWidth;
    private int frameHeight;

    private static OnRenderFinishedListener onRenderFinishedListener;

    public Render(Context context) {
        this.context = context;
    }

    /**
     * Merging two images with asynctask
     */
    private class MergeFrames extends AsyncTask<Void, Void, Void> {

        File file1 = new File(outputImagesDir1);
        File[] files1 = file1.listFiles();
        File file2 = new File(outputImagesDir2);
        File[] files2 = file2.listFiles();

        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        int x = Math.min(files1.length, files2.length);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < x; i++) {
                bitmaps.add(merge(files1[i].getAbsolutePath(), files2[i].getAbsolutePath()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            frameWidth = bitmaps.get(0).getWidth();
            frameHeight = bitmaps.get(0).getHeight();
            new EncodeFrames().execute(bitmaps);
            super.onPostExecute(result);
        }
    }

    /**
     * Merging two images
     *
     * @param path1 first image path
     * @param path2 second image path
     * @return
     */
    public Bitmap merge(String path1, String path2) {

        Bitmap bitmap1 = null;
        try {
            bitmap1 = Glide.with(context).load(path1).asBitmap().into(500, 500).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Bitmap bitmap2 = null;
        try {
            bitmap2 = Glide.with(context).load(path2).asBitmap().into(500, 500).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Bitmap mergedBitmap = Bitmap.createBitmap(720, 640, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);
        canvas.drawBitmap(bitmap1, 0, 0, null);
        canvas.drawBitmap(bitmap2, 360, 0, null);

        return mergedBitmap;
    }

    private class EncodeFrames extends AsyncTask<ArrayList<Bitmap>, Integer, Void> {

        Encoder encoder;

        protected Void doInBackground(ArrayList<Bitmap>... path) {

            for (int i = 0; i < path[0].size(); i++) {
                encoder.addFrame(path[0].get(i), 100);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            encoder.endVideoGeneration();
            onRenderFinishedListener.onFinish(3);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            encoder = new Encoder();
            encoder.init(frameWidth, frameHeight, 15, null);
            encoder.startVideoGeneration(new File(root + "/vid.mp4"));
        }
    }

    public void setOnRenderFinishedListener(OnRenderFinishedListener l) {
        onRenderFinishedListener = l;
    }

    public interface OnRenderFinishedListener {

        void onFinish(int progress);

    }

}
