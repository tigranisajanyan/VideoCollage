package com.picsart.videocollage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.socialin.android.encoder.Encoder;

import java.io.File;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

public class CollageMakerActivity extends AppCompatActivity {


    private ImageView gpuImageView;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private GPUImageFilter gpuImageFilter = new GPUImageFilter();
    private GPUImageFilterTools.FilterAdjuster mFilterAdjuster;
    private GPUEffects.FilterList filters = new GPUEffects.FilterList();

    private ArrayList<String> filesPath = new ArrayList<>();
    GifImitation gifImitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage_maker);

        init();
    }

    private void init() {


        mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(gpuImageFilter);
        filters.addFilter("None", GPUEffects.FilterType.NONE);
        filters.addFilter("Contrast", GPUEffects.FilterType.CONTRAST);
        filters.addFilter("Invert", GPUEffects.FilterType.INVERT);
        filters.addFilter("Hue", GPUEffects.FilterType.HUE);
        filters.addFilter("Gamma", GPUEffects.FilterType.GAMMA);
        filters.addFilter("Brightness", GPUEffects.FilterType.BRIGHTNESS);
        filters.addFilter("Sepia", GPUEffects.FilterType.SEPIA);
        filters.addFilter("Grayscale", GPUEffects.FilterType.GRAYSCALE);
        filters.addFilter("Sharpness", GPUEffects.FilterType.SHARPEN);
        filters.addFilter("Emboss", GPUEffects.FilterType.EMBOSS);
        filters.addFilter("Posterize", GPUEffects.FilterType.POSTERIZE);


        gpuImageView = (ImageView) findViewById(R.id.gpu_image_view);
        recyclerView = (RecyclerView) findViewById(R.id.effects_rec_view);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new SpacesItemDecoration((int) Utils.dpToPixel(2, this)));

        EffectsAdapter effectsAdapter = new EffectsAdapter(filters, this);
        ApplyGifEffect applyGifEffect = new ApplyGifEffect(BitmapFactory.decodeResource(getResources(),
                R.drawable.effect1), filters, effectsAdapter, this);
        applyGifEffect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        recyclerView.setAdapter(effectsAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //((SeekBar) container.findViewById(R.id.opacity_seek_bar)).setProgress(100);
                //switchFilterTo(GPUEffects.createFilterForType(filters.filters.get(position)));
            }
        }));

        gifImitation = new GifImitation(this, gpuImageView, new File(Environment.getExternalStorageDirectory() + "/VideoCollage/output"));
        gifImitation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*container.findViewById(R.id.opacity_seek_bar).setVisibility(
                mFilterAdjuster.canAdjust() ? View.VISIBLE : View.INVISIBLE);

        ((SeekBar) container.findViewById(R.id.opacity_seek_bar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mFilterAdjuster != null) {
                    mFilterAdjuster.adjust(progress);
                }
                mainFrameImageView.requestRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/
    }

    /*private void switchFilterTo(final GPUImageFilter filter) {
        if (gpuImageFilter == null
                || (filter != null && !gpuImageFilter.getClass().equals(filter.getClass()))) {
            gpuImageFilter = filter;
            gpuImageView.setFilter(gpuImageFilter);
            mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(gpuImageFilter);
        }
    }*/

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
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            encoder = new Encoder();
            encoder.init(960, 640, 15, null);
            encoder.startVideoGeneration(new File(Environment.getExternalStorageDirectory() + "/vid.mp4"));
        }
    }
}
