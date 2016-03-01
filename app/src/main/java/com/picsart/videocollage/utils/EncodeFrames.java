package com.picsart.videocollage.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.decoder.PhotoUtils;
import com.picsart.videocollage.R;
import com.picsart.videocollage.activity.EditorActivity;
import com.picsart.videocollage.activity.ShareActivity;
import com.socialin.android.encoder.Encoder;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Tigran on 2/19/16.
 */
public class EncodeFrames extends AsyncTask<Void, Integer, Void> {


    private static final int WIDTH = 480;
    private static final int HEIGHT = 640;

    private ProgressDialog progressDialog;
    private Encoder encoder;
    private int frameDuration;
    private Activity activity;
    private String outputFilePath;
    boolean doReverse = false;
    public GPUImageFilter gpuImageFilter;

    public EncodeFrames(Activity activity, boolean doReverse, String outputFilePath, GPUImageFilter gpuImageFilter, int frameDuration) {
        this.frameDuration = frameDuration;
        this.doReverse = doReverse;
        this.gpuImageFilter = gpuImageFilter;
        this.activity = activity;
        this.outputFilePath = outputFilePath;
    }

    protected Void doInBackground(Void... path) {
        Bitmap bitmap = null;
        GPUImage gpuImage = new GPUImage(activity);
        gpuImage.setFilter(gpuImageFilter);
        gpuImage.requestRender();
        if (doReverse) {
            for (int i = EditorActivity.filePaths.size() - 1; i >= 0; i--) {
                try {
                    //if (i != 0) {
                    bitmap = PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i));
                    /*} else {
                        bitmap = Bitmap.createBitmap(480, 640, Bitmap.Config.ARGB_8888);
                    }*/
                    gpuImage.setImage(bitmap);
                    Bitmap bitmap1 = gpuImage.getBitmapWithFilterApplied();
                    encoder.addFrame(bitmap1, frameDuration);
                    onProgressUpdate(EditorActivity.filePaths.size() - 1 - i);
                    bitmap1.recycle();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, "Error while SaveToMemory", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            for (int i = 0; i < EditorActivity.filePaths.size() + 1; i++) {
                try {
                    if (i < EditorActivity.filePaths.size()) {
                        bitmap = PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i));
                    } else {
                        bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
                    }
                    gpuImage.setImage(bitmap);
                    Bitmap bitmap1 = gpuImage.getBitmapWithFilterApplied();
                    encoder.addFrame(bitmap1, frameDuration);
                    onProgressUpdate(i);
                    bitmap1.recycle();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(activity, R.string.saving_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
        if (progressDialog != null) {
            progressDialog.setProgress(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        encoder.endVideoGeneration();
        Intent intent = new Intent(activity, ShareActivity.class);
        intent.putExtra(Constants.INTENT_FILE_PATHS, outputFilePath);
        activity.startActivity(intent);
        progressDialog.dismiss();
        EditorActivity.filePaths.clear();
        FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/" + Constants.VIDEO_FRAMES));
        activity.finish();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(EditorActivity.filePaths.size());
        progressDialog.show();
        encoder = new Encoder();

        encoder.init(WIDTH, HEIGHT, 15, null);

        encoder.startVideoGeneration(new File(outputFilePath));
    }
}

