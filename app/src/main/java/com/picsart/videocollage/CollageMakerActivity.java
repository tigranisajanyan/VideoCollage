package com.picsart.videocollage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class CollageMakerActivity extends AppCompatActivity {


    private GPUImageView gpuImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage_maker);
    }

    private void init() {
        gpuImageView = (GPUImageView) findViewById(R.id.gpu_image_view);
    }
}
