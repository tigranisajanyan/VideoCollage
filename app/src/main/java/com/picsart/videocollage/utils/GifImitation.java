package com.picsart.videocollage.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.decoder.PhotoUtils;
import com.picsart.videocollage.activity.EditorActivity;

import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by Tigran on 8/6/15.
 */
public class GifImitation extends AsyncTask<Void, Bitmap, Void> {

    private Context context;
    private GPUImageView imageView;
    private int currentDuration = 25;
    private boolean play = false;
    private int k = 0;
    int index;
    int gifSpeed = 25;
    private boolean doReverse = false;

    private ThreadControl tControl = new ThreadControl();

    public GifImitation(Context context, int gifSpeed, GPUImageView imageView) {

        this.imageView = imageView;
        this.context = context;
        this.gifSpeed = gifSpeed;
        this.currentDuration = gifSpeed;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        play = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (play) {
            if (isCancelled()) break;
            if (doReverse) {
                index = EditorActivity.filePaths.size() - (k % EditorActivity.filePaths.size()) - 1;
            } else {
                index = k % EditorActivity.filePaths.size();
            }
            try {
                //Pause work if control is paused.
                tControl.waitIfPaused();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Stop work if control is cancelled.
            if (tControl.isCancelled()) {
                break;
            }
            long time = System.currentTimeMillis();
            Bitmap bitmap = PhotoUtils.loadRawBitmap(EditorActivity.filePaths.get(index));
            if (isCancelled()) break;
            publishProgress(bitmap);
            time = System.currentTimeMillis() - time;
            if (isCancelled()) break;
            try {
                TimeUnit.MILLISECONDS.sleep(currentDuration - time);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            if (isCancelled()) break;
            k++;
            if (isCancelled()) break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        Bitmap bitmap = values[0];
        imageView.getGPUImage().setImage(bitmap);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        play = false;
    }

    public void changeDuration(int currDuration) {
        this.currentDuration = gifSpeed * currDuration / 10;
    }

    public void onPause() {
        //No need to pause if we are getting destroyed
        //and will cancel thread control anyway.
        //Pause control.
        tControl.pause();
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public void onResume() {
        tControl.resume();
    }

    public void doReverse(boolean doReverse) {
        k = 0;
        this.doReverse = doReverse;

    }

}
