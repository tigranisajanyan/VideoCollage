package com.picsart.videocollage.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.picsart.videocollage.R;
import com.picsart.videocollage.camera.CameraHelper;
import com.picsart.videocollage.camera.CameraPreview;
import com.picsart.videocollage.camera.SaveRecordedVideoFrames;
import com.picsart.videocollage.utils.Constants;
import com.picsart.videocollage.utils.FileUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private RelativeLayout firstCameraPreview;
    private ImageButton cameraSwitchButton, cameraRecordButton;
    private ProgressDialog progressDialog;

    private CameraPreview cameraPreview;
    private Camera camera;
    public static boolean cameraFront = false;
    private boolean recording = false;

    private int recordedVideoDuration = 0;
    private int videoFrameRate = 25;

    private VideoCaptureCountDownTimer videoCaptureCountDownTimer;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        FileUtils.createDir(Constants.MY_DIR);
        FileUtils.createAndClearDir(Constants.VIDEO_FRAMES);

        init();

    }

    private void init() {

        firstCameraPreview = (RelativeLayout) findViewById(R.id.camera_preview);
        cameraSwitchButton = (ImageButton) findViewById(R.id.camera_preview_switch_button);
        cameraRecordButton = (ImageButton) findViewById(R.id.camera_capture_button);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(android.R.style.Theme_Holo_Light_Dialog);
        progressDialog.setTitle(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        cameraPreview = new CameraPreview(this, camera);
        firstCameraPreview.addView(cameraPreview);

        cameraSwitchButton.setOnClickListener(switchCameraListener);
        cameraRecordButton.setOnClickListener(captureListener);

        findViewById(R.id.camera_preview_flash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cameraFront) {
                    CameraHelper.setNextFlashLightMode(MainActivity.this, camera);
                }
            }
        });

    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!CameraHelper.hasCamera(this)) {
            Toast toast = Toast.makeText(this, R.string.no_camera, Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (camera == null) {
            // if the front facing camera does not exist
            if (CameraHelper.findFrontFacingCamera() < 0) {

                Toast.makeText(this, R.string.no_front_camera, Toast.LENGTH_LONG).show();
            }
            camera = Camera.open(CameraHelper.findBackFacingCamera());
            cameraFront = false;
            cameraPreview.refreshCamera(camera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        if (recording) {
            progressDialog.show();
            cameraRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.capture));
            videoCaptureCountDownTimer.cancel();
            cameraPreview.stopRecord();
            recording = false;
        }
    }

    private void releaseCamera() {
        // stop and release camera
        try {
            if (camera != null) {
                cameraPreview.stopRecord();
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
                    releaseCamera();
                    chooseCamera();
                    try {
                        camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        ((ImageButton)findViewById(R.id.camera_preview_flash_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_action_flash_off));
                    }catch (Exception e){

                    }

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.only_one_camera, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    };

    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!recording) {
                cameraPreview.setRenderingListener(new CameraPreview.StartRendering() {
                    @Override
                    public void toStart(boolean start) {
                        SaveRecordedVideoFrames saveRecordedVideoFrames = new SaveRecordedVideoFrames(getApplicationContext(), cameraFront);
                        saveRecordedVideoFrames.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        saveRecordedVideoFrames.setOnFramesSavedListener(new SaveRecordedVideoFrames.FramesAreSaved() {
                            @Override
                            public void framesAreSaved(ArrayList<String> filePaths) {
                                if (filePaths != null) {
                                    videoFrameRate = FileUtils.checkVideoFrameRate(filePaths.size(), recordedVideoDuration);
                                    Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
                                    intent.putStringArrayListExtra(Constants.INTENT_FILE_PATHS, filePaths);
                                    intent.putExtra(Constants.INTENT_FPS, videoFrameRate);
                                    startActivity(intent);
                                } else {
                                    cameraPreview.refreshCamera(camera);
                                }
                                progressDialog.dismiss();
                                updateAfterRestart();
                            }
                        });
                    }
                });
                cameraPreview.startRecord();
                cameraRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.capturing));
                videoCaptureCountDownTimer = new VideoCaptureCountDownTimer(6100, 1000);
                videoCaptureCountDownTimer.start();
                recording = true;
            } else {
                progressDialog.show();
                cameraRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.capture));
                videoCaptureCountDownTimer.cancel();
                cameraPreview.stopRecord();
                recording = false;
            }
        }
    };

    private void updateAfterRestart() {
        //cameraFront = false;
        cameraRecordButton.setOnClickListener(captureListener);
        cameraSwitchButton.setOnClickListener(switchCameraListener);
        recordedVideoDuration = 0;
        cameraFront = false;
        if (camera == null) {
            // if the front facing camera does not exist
            if (CameraHelper.findFrontFacingCamera() < 0) {

                Toast.makeText(this, R.string.no_front_camera, Toast.LENGTH_LONG).show();
            }
            camera = Camera.open(CameraHelper.findBackFacingCamera());
            cameraPreview.refreshCamera(camera);
        }
        try {
            camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            ((ImageButton)findViewById(R.id.camera_preview_flash_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_action_flash_off));
        }catch (Exception e){
        }

    }


    public class VideoCaptureCountDownTimer extends CountDownTimer {
        public VideoCaptureCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
        }

        @Override
        public void onTick(long millisUntilFinished) {
            recordedVideoDuration = 6 - (int) ((millisUntilFinished / 1000) - 1);
            ((TextView) findViewById(R.id.recording_time)).setText("00:0" + ((millisUntilFinished / 1000) - 1));
            Log.d(LOG_TAG, "time: " + millisUntilFinished);
            if (millisUntilFinished <= 2000) {
                if (recording) {
                    progressDialog.show();
                    cameraRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.capture));
                    cameraPreview.stopRecord();
                    recording = false;
                }
            }
        }
    }


}
