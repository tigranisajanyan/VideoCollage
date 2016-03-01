package com.picsart.videocollage.gifutils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;

import com.decoder.PhotoUtils;
import com.picsart.videocollage.activity.EditorActivity;
import com.picsart.videocollage.activity.ShareActivity;
import com.picsart.videocollage.utils.Constants;
import com.picsart.videocollage.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by Tigran on 8/24/15.
 */
public class SaveGIFAsyncTask extends AsyncTask<Void, Integer, Void> {

    private Activity activity;
    private String outputDir;
    private ProgressDialog progressDialog;

    public GPUImageFilter gpuImageFilter;

    private int duration = Constants.DEFAULT_FPS;
    private int num = 0;
    private boolean doReverse;


    public SaveGIFAsyncTask(String outputDir, int duration, boolean doReverse, GPUImageFilter gpuImageFilter, Activity activity) {
        this.outputDir = outputDir;
        this.gpuImageFilter = gpuImageFilter;
        this.activity = activity;
        this.duration = duration;
        this.doReverse = doReverse;

        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(EditorActivity.filePaths.size());
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        num = 0;
    }

    @Override
    protected Void doInBackground(Void... params) {

        File outFile = new File(outputDir);
        try {

            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outFile));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            AnimatedGifEncoder animatedGifEncoder = new AnimatedGifEncoder();
            animatedGifEncoder.setRepeat(0);
            animatedGifEncoder.setQuality(256);
            animatedGifEncoder.start(bos);

            if (doReverse) {
                for (int i = EditorActivity.filePaths.size() - 1; i >= 0; i--) {
                    addGifFrame(animatedGifEncoder, PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i)), duration);
                    publishProgress(num);
                    num++;
                }
            } else {
                for (int i = 0; i < EditorActivity.filePaths.size(); i++) {
                    addGifFrame(animatedGifEncoder, PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i)), duration);
                    publishProgress(num);
                    num++;
                }
            }

            animatedGifEncoder.finish();
            bufferedOutputStream.write(bos.toByteArray());
            bufferedOutputStream.flush();
            bufferedOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*GifEncoder gifEncoder = new GifEncoder();
        gifEncoder.init(Environment.getExternalStorageDirectory() + "/test.gif", 480, 640, 256, 10, duration);
        int[] pixels = new int[500 * 500];
        if (doReverse) {
            for (int i = EditorActivity.filePaths.size() - 1; i >= 0; i--) {
                Bitmap bitmap = PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i));
                bitmap.getPixels(pixels, 0, 480, 0, 0, 480, 640);
                gifEncoder.addFrame(pixels);
                publishProgress(num);
                num++;
            }
        } else {
            for (int i = 0; i < EditorActivity.filePaths.size(); i++) {
                Bitmap bitmap = PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(i));
                bitmap.getPixels(pixels, 0, 480, 0, 0, 480, 640);
                gifEncoder.addFrame(pixels);
                publishProgress(num);
                num++;
            }
        }
        gifEncoder.close();*/
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressDialog != null) {
            progressDialog.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        EditorActivity.filePaths.clear();
        FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/" + Constants.VIDEO_FRAMES));
        progressDialog.dismiss();
        Intent intent = new Intent(activity, ShareActivity.class);
        intent.putExtra(Constants.INTENT_FILE_PATHS, outputDir);
        activity.startActivity(intent);
        activity.finish();
    }

    private void addGifFrame(AnimatedGifEncoder animatedGifEncoder, Bitmap bitmap, int duration) {
        GPUImage gpuImage = new GPUImage(activity);
        gpuImage.setFilter(gpuImageFilter);
        gpuImage.requestRender();
        gpuImage.setImage(bitmap);
        animatedGifEncoder.setDelay(duration);
        animatedGifEncoder.addFrame(gpuImage.getBitmapWithFilterApplied());
        bitmap.recycle();
    }

    /*GifEncoder gifEncoder = new GifEncoder();
    gifEncoder.init(Environment.getExternalStorageDirectory() + "/test.gif", 500, 500, 256, 100, 100);
    int[] pixels = new int[500 * 500];
    for (int i = 0; i < gifItems.size(); i++) {
        gifItems.get(i).getBitmap().getPixels(pixels, 0, 500, 0, 0, 500, 500);
        gifEncoder.addFrame(pixels);
    }
    gifEncoder.close();*/
}
