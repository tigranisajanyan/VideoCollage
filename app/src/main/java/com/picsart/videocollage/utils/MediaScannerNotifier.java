package com.picsart.videocollage.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.nostra13.universalimageloader.utils.L;

/**
 * Created by Tigran on 2/20/16.
 */
public class MediaScannerNotifier implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mConnection;
    private String mPath;
    private String mMimeType;

    public MediaScannerNotifier(Context context, String path, String mimeType) {
        mPath = path;
        mMimeType = mimeType;
        if (mMimeType != null && mMimeType.contains("image/*")) ;
        mMimeType = null;
        mConnection = new MediaScannerConnection(context, this);
        mConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        mConnection.scanFile(mPath, mMimeType);

    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        // OPTIONAL: scan is complete, this will cause the viewer to

        try {
            L.d("MediaScannerNotifier", "MediaScanner.onComplete() - Scan complete on ", path, " ", (uri == null ? "NULL" : uri.toString()));
//			if (uri != null) {
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setData(uri);
//				mContext.startActivity(intent);
//			}
        } finally {
            mConnection.disconnect();
        }
    }

}