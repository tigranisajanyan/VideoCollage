package com.picsart.videocollage;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private FrameLayout firstWindow;
    private FrameLayout secondWindow;

    private FrameLayout firstCameraPreviewContainer;
    private FrameLayout secondCameraPreviewContainer;

    private RelativeLayout firstCameraPreview;
    private RelativeLayout secondCameraPreview;

    private ImageView imageView1;
    private ImageView imageView2;


    private ImageButton switchCameraPreviewButton, switchFirstCameraButton, switchSecondCameraButton, recordFirstCameraButton, recordSecondCameraButton;


    private CameraPreview cameraPreview;
    private Camera camera;
    private boolean cameraFront = false;
    private boolean recording = false;
    private boolean cameraFirst = true;

    private int firstTimer = 0;
    private int secondTimer = 0;

    private int captureVideoCount = 0;

    GifImitation gifImitation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FileUtils.createDir(Constants.MY_DIR);
        FileUtils.createDir(Constants.FIRST_VIDEO);
        FileUtils.createDir(Constants.SECOND_VIDEO);

        init();

    }

    private void init() {
        context = this;

        firstWindow = (FrameLayout) findViewById(R.id.first_window);
        secondWindow = (FrameLayout) findViewById(R.id.second_window);

        firstCameraPreview = (RelativeLayout) findViewById(R.id.camera_preview_first);
        secondCameraPreview = (RelativeLayout) findViewById(R.id.camera_preview_second);

        firstCameraPreviewContainer = (FrameLayout) findViewById(R.id.camera_preview_first_container);
        secondCameraPreviewContainer = (FrameLayout) findViewById(R.id.camera_preview_second_container);

        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView2 = (ImageView) findViewById(R.id.image2);

        ViewGroup.LayoutParams layoutParams = firstWindow.getLayoutParams();
        layoutParams.width = getResources().getDisplayMetrics().widthPixels / 2;
        layoutParams.height = layoutParams.width * 4 / 3;
        firstWindow.setLayoutParams(layoutParams);
        secondWindow.setLayoutParams(layoutParams);

        switchFirstCameraButton = (ImageButton) findViewById(R.id.camera_preview_first_switch_button);
        switchSecondCameraButton = (ImageButton) findViewById(R.id.camera_preview_second_switch_button);
        switchCameraPreviewButton = (ImageButton) findViewById(R.id.switch_camera_preview);

        recordFirstCameraButton = (ImageButton) findViewById(R.id.first_camera_capture_button);
        recordSecondCameraButton = (ImageButton) findViewById(R.id.second_camera_capture_button);

        cameraPreview = new CameraPreview(this, camera);

        firstCameraPreview.addView(cameraPreview);

        visibilitySwitcher(cameraFirst);


        switchFirstCameraButton.setOnClickListener(switchCameraListener);
        switchSecondCameraButton.setOnClickListener(switchCameraListener);
        switchCameraPreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstCameraPreview.getChildCount() == 0) {
                    cameraFirst = true;
                    secondCameraPreview.removeAllViews();
                    firstCameraPreview.addView(cameraPreview);
                } else {
                    cameraFirst = false;
                    firstCameraPreview.removeAllViews();
                    secondCameraPreview.addView(cameraPreview);
                }
                visibilitySwitcher(cameraFirst);
                openBackCamera();
            }
        });

        recordFirstCameraButton.setOnTouchListener(captrureListener);
        recordSecondCameraButton.setOnTouchListener(captrureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CameraHelper.hasCamera(this)) {
            Toast toast = Toast.makeText(this, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (camera == null) {
            // if the front facing camera does not exist
            if (CameraHelper.findFrontFacingCamera() < 0) {

                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
            }
            camera = Camera.open(CameraHelper.findBackFacingCamera());
            cameraPreview.refreshCamera(camera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        // stop and release camera
        try {
            if (camera != null) {
                cameraPreview.stop();
                camera.stopPreview();
                camera.setPreviewCallback(null);
                //cameraPreview.getHolder().removeCallback(cameraPreview);
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseCamera() {
        // if the camera preview is the front
        if (cameraFront) {
            int cameraId = CameraHelper.findBackFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                cameraFront = false;
                camera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                cameraPreview.refreshCamera(camera);
            }
        } else {
            int cameraId = CameraHelper.findFrontFacingCamera();
            if (cameraId >= 0) {
                // open the backFacingCamera
                // set a picture callback
                // refresh the preview

                cameraFront = true;
                camera = Camera.open(cameraId);
                // mPicture = getPictureCallback();
                cameraPreview.refreshCamera(camera);
            }
        }
    }

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // get the number of cameras
            if (!recording) {
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    // release the old camera instance
                    // switch camera, from the front and the corner and vice versa
                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(context, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    View.OnTouchListener captrureListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (cameraFirst) {
                        FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/VideoCollage/first_video"));
                        cameraPreview.start("/VideoCollage/first_video");
                        recording = true;
                    } else {
                        FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/VideoCollage/second_video"));
                        cameraPreview.start("/VideoCollage/second_video");
                        recording = true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (recording) {
                        cameraPreview.stop();
                        recording = false;
                        captureVideoCount += 1;
                        if (captureVideoCount > 1) {
                            Intent intent = new Intent(MainActivity.this, CollageMakerActivity.class);
                            startActivity(intent);
                        } else {
                            if (firstCameraPreview.getChildCount() == 0) {
                                gifImitation = new GifImitation(MainActivity.this, imageView2, new File(Environment.getExternalStorageDirectory() + "/VideoCollage/second_video"));
                                gifImitation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                cameraFirst = true;
                                secondCameraPreview.removeAllViews();
                                firstCameraPreview.addView(cameraPreview);
                            } else {
                                gifImitation = new GifImitation(MainActivity.this, imageView1, new File(Environment.getExternalStorageDirectory() + "/VideoCollage/first_video"));
                                gifImitation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                cameraFirst = false;
                                firstCameraPreview.removeAllViews();
                                secondCameraPreview.addView(cameraPreview);
                            }
                            visibilitySwitcher(cameraFirst);
                            openBackCamera();
                        }
                    }
                default:
                    return false;
            }
            return false;
        }
    };

    private void visibilitySwitcher(boolean cameraFirst) {
        if (cameraFirst) {
            firstCameraPreviewContainer.setVisibility(View.VISIBLE);
            secondCameraPreviewContainer.setVisibility(View.INVISIBLE);
            imageView2.setVisibility(View.VISIBLE);
        } else {
            secondCameraPreviewContainer.setVisibility(View.VISIBLE);
            firstCameraPreviewContainer.setVisibility(View.INVISIBLE);
            imageView1.setVisibility(View.VISIBLE);
        }
    }

    private void openBackCamera() {
        int cameraId = CameraHelper.findBackFacingCamera();
        if (cameraId >= 0) {
            // open the backFacingCamera
            // set a picture callback
            // refresh the preview

            cameraFront = false;
            camera = Camera.open(cameraId);
            // mPicture = getPictureCallback();
            cameraPreview.refreshCamera(camera);
        }
    }

}
