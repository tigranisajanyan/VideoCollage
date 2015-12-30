package com.picsart.videocollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.decoder.PhotoUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by Tigran on 8/6/15.
 */
public class GifImitation extends AsyncTask<Void, Bitmap, Void> {

    private Context context;
    private GPUImageView imageView;
    private File file;
    private int duration = 90;
    private boolean play = false;
    private int k = 0;
    int index;

    int count = 0;

    File[] files;

    public GifImitation(Context context, GPUImageView imageView, File file) {

        this.imageView = imageView;
        this.file = file;
        this.context = context;
        files = file.listFiles();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        play = true;
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (play) {
            //Glide.get(context).clearDiskCache();
            index = k % files.length;

            Bitmap bitmap = null;
            //Glide.with(context).load(files[index].getAbsolutePath()).asBitmap().into(imageView);

            publishProgress(bitmap);

            try {
                TimeUnit.MILLISECONDS.sleep(duration);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            k++;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        //Bitmap bitmap = ImageLoader.getInstance().loadImageSync(FileUtils.FILE_PREFIX + files[index].getAbsolutePath());
        Bitmap bitmap = PhotoUtils.loadRawBitmap(files[index].getAbsolutePath());
        //imageView.getGPUImage().deleteImage();
        imageView.getGPUImage().setImage(bitmap);
        //Glide.with(context).load(files[index].getAbsolutePath()).into(imageView);
        //Bitmap bitmap = values[0];
        //imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        play = false;
    }

}
