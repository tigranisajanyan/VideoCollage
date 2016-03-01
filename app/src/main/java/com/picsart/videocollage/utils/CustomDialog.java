package com.picsart.videocollage.utils;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.picsart.videocollage.R;

/**
 * Created by Tigran on 2/20/16.
 */
public class CustomDialog extends Dialog {
    private MediaTypeSelectListener mediaTypeSelectListener;
    int framesCount;
    Activity activity;

    public CustomDialog(Activity activity, int framesCount) {
        super(activity, R.style.Base_Theme_AppCompat_Dialog);
        this.framesCount = framesCount;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_or_video_dialog);
        if (framesCount == 1) {
            findViewById(R.id.btn_video).setEnabled(false);
        }
        initListeners();
    }

    private void initListeners() {
        findViewById(R.id.btn_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaTypeSelectListener != null) {
                    mediaTypeSelectListener.onGifSelected();
                    dismiss();
                }
            }
        });
        findViewById(R.id.btn_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaTypeSelectListener != null) {
                    mediaTypeSelectListener.onVideoSelected();
                    dismiss();
                }
            }
        });
        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaTypeSelectListener != null) {
                    mediaTypeSelectListener.onCancel();
                    dismiss();
                }
            }
        });
    }

    public void setOnMediaTypeSellectedListener(MediaTypeSelectListener listener) {
        mediaTypeSelectListener = listener;
    }

    public interface MediaTypeSelectListener {
        void onGifSelected();

        void onVideoSelected();

        void onCancel();

    }
}