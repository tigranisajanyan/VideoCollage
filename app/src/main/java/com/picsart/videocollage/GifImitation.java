package com.picsart.videocollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tigran on 8/6/15.
 */
public class GifImitation extends AsyncTask<Void, Bitmap, Void> {

    private Context context;
    private ImageView imageView;
    private File file;
    private int duration = 53;
    private boolean play = false;
    private int k = 0;
    int index;

    int count = 0;

    File[] files;

    public GifImitation(Context context, ImageView imageView, File file) {

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
            Glide.get(context).clearDiskCache();
            index = k % files.length;

            Bitmap bitmap = null;
            //try {
                //Glide.with(context).load(files[index].getAbsolutePath()).asBitmap().into(imageView);
            /*} catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }*/
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
        ImageLoader.getInstance().displayImage(FileUtils.FILE_PREFIX+files[index].getAbsolutePath(),imageView);
        //Glide.with(context).load(files[index].getAbsolutePath()).asBitmap().into(imageView);
        //Bitmap bitmap = values[0];
        //imageView.setImageBitmap(bitmap);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        play = false;
    }

}
