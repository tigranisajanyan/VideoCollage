package com.picsart.videocollage.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.decoder.PhotoUtils;
import com.picsart.videocollage.utils.Constants;

import java.util.ArrayList;

/**
 * Created by Tigran on 1/13/16.
 */
public class SaveRecordedVideoFrames extends AsyncTask<Void, Void, Void> {

    private static final String LOG_TAG = SaveRecordedVideoFrames.class.getSimpleName();
    private static final int ROTATE_DEGREE_90 = 90;
    private static final int ROTATE_DEGREE_270 = 270;

    private Context context;

    private boolean cameraFront;

    int counter = 0;

    private FramesAreSaved framesAreSaved;
    private ArrayList<String> filePaths = new ArrayList<>();

    public SaveRecordedVideoFrames(Context context, boolean cameraFront) {
        this.context = context;
        this.cameraFront = cameraFront;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        counter = 0;
        Log.d(LOG_TAG, "start rendering");
    }

    @Override
    protected Void doInBackground(Void... params) {
        for (int i = 0; i < CameraPreview.datas.size(); i++) {
            if (isCancelled()) return null;
            String path = Environment.getExternalStorageDirectory().getPath() + "/" + Constants.VIDEO_FRAMES + Constants.ITEM_PREFIX + counter;
            Bitmap bitmap = BitmapFactory.decodeByteArray(CameraPreview.datas.get(i), 0, CameraPreview.datas.get(i).length);

            Matrix matrix = new Matrix();
            if (cameraFront) {
                matrix.postRotate(ROTATE_DEGREE_270);
            } else {
                matrix.postRotate(ROTATE_DEGREE_90);
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            PhotoUtils.saveRawBitmap(rotatedBitmap, path);
            filePaths.add(path);
            counter++;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        framesAreSaved.framesAreSaved(filePaths);
        CameraPreview.datas.clear();
        Log.d(LOG_TAG, "finish rendering");
    }

    public interface FramesAreSaved {
        void framesAreSaved(ArrayList<String> filePaths);
    }

    public void setOnFramesSavedListener(FramesAreSaved saved) {
        framesAreSaved = saved;
    }

}