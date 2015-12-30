package com.picsart.videocollage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private FrameLayout firstWindow;

    private FrameLayout firstCameraPreviewContainer;

    private RelativeLayout firstCameraPreview;

    private ImageView imageView1;


    private ImageButton switchCameraPreviewButton, switchFirstCameraButton, recordFirstCameraButton;

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

        initImageLoader(MainActivity.this);
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiskCache();

        FileUtils.createDir(Constants.MY_DIR);
        FileUtils.createDir(Constants.FIRST_VIDEO);
        FileUtils.createDir("VideoCollage/output");

        init();

    }

    private void init() {
        context = this;

        firstWindow = (FrameLayout) findViewById(R.id.first_window);

        firstCameraPreview = (RelativeLayout) findViewById(R.id.camera_preview_first);

        firstCameraPreviewContainer = (FrameLayout) findViewById(R.id.camera_preview_first_container);

        imageView1 = (ImageView) findViewById(R.id.image1);

        switchFirstCameraButton = (ImageButton) findViewById(R.id.camera_preview_first_switch_button);

        recordFirstCameraButton = (ImageButton) findViewById(R.id.first_camera_capture_button);

        cameraPreview = new CameraPreview(this, camera);

        firstCameraPreview.addView(cameraPreview);

        visibilitySwitcher(cameraFirst);


        switchFirstCameraButton.setOnClickListener(switchCameraListener);

        recordFirstCameraButton.setOnTouchListener(captrureListener);

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
                Log.d("gaga", "try");
                cameraPreview.stop();
                camera.stopPreview();
                camera.setPreviewCallback(null);
                //cameraPreview.getHolder().removeCallback(cameraPreview);
                camera.release();
                camera = null;
            }
        } catch (Exception e) {
            Log.d("gaga", "catch");
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
            /*if (!recording) {
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
            }*/
            decode();
        }
    };

    View.OnTouchListener captrureListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    FileUtils.clearDir(new File(Environment.getExternalStorageDirectory() + "/VideoCollage/first_video"));
                    cameraPreview.start("/VideoCollage/first_video");
                    recording = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (recording) {
                        cameraPreview.stop();
                        recording = false;
                        Intent intent = new Intent(MainActivity.this, CollageMakerActivity.class);
                        startActivity(intent);
                        /*final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        Render render = new Render(context);
                        render.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        render.setOnRenderFinishedListener(new Render.OnRenderFinishedListener() {
                            @Override
                            public void onFinish(boolean finished) {
                                Intent intent = new Intent(MainActivity.this, CollageMakerActivity.class);
                                startActivity(intent);
                                progressDialog.dismiss();
                            }
                        });*/
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
        } else {
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

    public static void initImageLoader(Context context) {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(context,
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .considerExifParams(true)
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .decodingOptions(new BitmapFactory.Options())
                    .bitmapConfig(Bitmap.Config.RGB_565).build();

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    /*.memoryCacheExtraOptions(1000, 1000) // width, height
                    .discCacheExtraOptions(1000, 1000, new BitmapProcessor() {
                        @Override
                        public Bitmap process(Bitmap bitmap) {
                            return null;
                        }
                    })*/
                    //.threadPoolSize(3)
                    //.threadPriority(Thread.MIN_PRIORITY + 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .memoryCache(new UsingFreqLimitedMemoryCache(3 * 1024 * 1024)) // 3 Mb
                    .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                    .imageDownloader(new BaseImageDownloader(context)) // connectTimeout (5 s), readTimeout (30 s)
                    .defaultDisplayImageOptions(defaultOptions)
                    .build();

            ImageLoader.getInstance().init(config);

        } catch (Exception e) {
        }
    }

    public void decode() {
        MediaCodec decoder = null;
        Surface surface = null;


        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(Environment.getExternalStorageDirectory() + "/VID_20151210_152901.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                try {
                    decoder = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                decoder.configure(format, surface, null, 0);
                break;
            }
        }

        if (decoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        decoder.start();

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long startMs = System.currentTimeMillis();

        while (!Thread.interrupted()) {
            if (!isEOS) {
                int inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }
            }

            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d("DecodeActivity", "New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buffer);

                    // We use a very simple clock to keep the video FPS, or the video
                    // playback will be too fast
                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                        /*try {
                            //sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }*/
                    }
                    decoder.releaseOutputBuffer(outIndex, true);
                    break;
            }

            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        decoder.stop();
        decoder.release();
        extractor.release();
    }

}
