package com.picsart.videocollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;

/**
 * Created by Tigran on 11/5/15.
 */
public class ApplyGifEffect extends AsyncTask<Void, Bitmap, Void> {

    private Context context;
    private EffectsAdapter effectsAdapter;
    private Bitmap bitmap;
    private GPUEffects.FilterList filters;

    public ApplyGifEffect(Bitmap firstFrame, GPUEffects.FilterList filters, EffectsAdapter effectsAdapter, Context context) {
        this.context = context;
        this.effectsAdapter = effectsAdapter;
        this.bitmap = firstFrame;
        this.filters = filters;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... params) {
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setImage(bitmap);
        for (int i = 0; i < filters.filters.size(); i++) {
            gpuImage.setFilter(GPUEffects.createFilterForType(filters.filters.get(i)));
            publishProgress(scaleCenterCrop(gpuImage.getBitmapWithFilterApplied(), 500, 500));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        super.onProgressUpdate(values);
        effectsAdapter.addItem(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the biggerF
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

}
