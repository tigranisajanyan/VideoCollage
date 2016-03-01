package com.picsart.videocollage.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.picsart.videocollage.R;
import com.picsart.videocollage.adapter.EffectsAdapter;
import com.picsart.videocollage.effects.ApplyGifEffect;
import com.picsart.videocollage.effects.GPUEffects;
import com.picsart.videocollage.gifutils.SaveGIFAsyncTask;
import com.picsart.videocollage.utils.Constants;
import com.picsart.videocollage.utils.CustomDialog;
import com.picsart.videocollage.utils.EncodeFrames;
import com.picsart.videocollage.utils.FileUtils;
import com.picsart.videocollage.utils.GifImitation;
import com.picsart.videocollage.utils.RecyclerItemClickListener;

import java.io.File;
import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class EditorActivity extends AppCompatActivity {

    private ViewGroup gpuImageContainer;
    private GPUImageView gpuImageView;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ImageButton cancelButton, doneButton;
    private SeekBar speedSeekBar;
    private ToggleButton toggleButton;

    private GPUImageFilter gpuImageFilter = new GPUImageFilter();
    private GPUEffects.FilterList filters = new GPUEffects.FilterList();

    private EffectsAdapter effectsAdapter;

    public static ArrayList<String> filePaths = new ArrayList<>();
    private GifImitation gifImitation;

    private int gifSpeed = 25;
    private boolean doReverse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        filePaths = intent.getStringArrayListExtra(Constants.INTENT_FILE_PATHS);
        gifSpeed = intent.getIntExtra(Constants.INTENT_FPS, Constants.DEFAULT_FPS);

        init();
    }

    private void init() {

        filters.addFilter("None", GPUEffects.FilterType.NONE);
        filters.addFilter("Invert", GPUEffects.FilterType.INVERT);
        filters.addFilter("Sepia", GPUEffects.FilterType.SEPIA);
        filters.addFilter("Grayscale", GPUEffects.FilterType.GRAYSCALE);
        filters.addFilter("Emboss", GPUEffects.FilterType.EMBOSS);
        filters.addFilter("Posterize", GPUEffects.FilterType.POSTERIZE);


        gpuImageContainer = (ViewGroup) findViewById(R.id.gpu_image_container);
        gpuImageView = (GPUImageView) findViewById(R.id.gpu_image_view);
        cancelButton = (ImageButton) findViewById(R.id.toolbar_editor_activity_cancel_button);
        doneButton = (ImageButton) findViewById(R.id.toolbar_editor_activity_done_button);
        speedSeekBar = (SeekBar) findViewById(R.id.speed_seek_bar);
        toggleButton = (ToggleButton) findViewById(R.id.toggle_button);

        ViewGroup.LayoutParams layoutParams = gpuImageContainer.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels;
        layoutParams.height = getResources().getDisplayMetrics().widthPixels;

        gpuImageContainer.setLayoutParams(layoutParams);
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        recyclerView = (RecyclerView) findViewById(R.id.effects_rec_view);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setClipToPadding(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new SpacesItemDecoration((int) Utils.dpToPixel(2, this)));

        effectsAdapter = new EffectsAdapter(filters, this);
        ApplyGifEffect applyGifEffect = new ApplyGifEffect(BitmapFactory.decodeResource(getResources(),
                R.drawable.effect1), filters, effectsAdapter, this);
        applyGifEffect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        recyclerView.setAdapter(effectsAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //((SeekBar) container.findViewById(R.id.opacity_seek_bar)).setProgress(100);
                switchFilterTo(GPUEffects.createFilterForType(filters.filters.get(position)));
            }
        }));

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifImitation.cancel(true);
                filePaths.clear();
                FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/" + Constants.VIDEO_FRAMES));
                finish();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gifImitation.onPause();
                CustomDialog customDialog = new CustomDialog(EditorActivity.this, 50);
                customDialog.setOnMediaTypeSellectedListener(new CustomDialog.MediaTypeSelectListener() {
                    @Override
                    public void onGifSelected() {
                        SaveGIFAsyncTask saveGIFAsyncTask = new SaveGIFAsyncTask(Environment.getExternalStorageDirectory() + "/" + Constants.MY_DIR + "/" + Constants.EXPORTED_FILE_PREFIX + System.currentTimeMillis() + Constants.FILE_POSTFIX_GIF, gifImitation.getCurrentDuration(), doReverse, gpuImageFilter, EditorActivity.this);
                        saveGIFAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    @Override
                    public void onVideoSelected() {
                        EncodeFrames encodeFrames = new EncodeFrames(EditorActivity.this, doReverse, Environment.getExternalStorageDirectory() + "/" + Constants.MY_DIR + "/" + Constants.EXPORTED_FILE_PREFIX + System.currentTimeMillis() + Constants.FILE_POSTFIX_MP4, gpuImageFilter, gifImitation.getCurrentDuration());
                        encodeFrames.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }

                    @Override
                    public void onCancel() {
                        gifImitation.onResume();
                    }
                });
                customDialog.show();
            }
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gifSpeed = (20 - progress);
                gifImitation.changeDuration(gifSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    doReverse = true;
                } else {
                    doReverse = false;
                }
                gifImitation.doReverse(doReverse);
            }
        });

        gifImitation = new GifImitation(this, gifSpeed, gpuImageView);
        gifImitation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (gpuImageFilter == null
                || (filter != null && !gpuImageFilter.getClass().equals(filter.getClass()))) {
            gpuImageFilter = filter;
            gpuImageView.setFilter(gpuImageFilter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gifImitation.cancel(true);
    }
}
