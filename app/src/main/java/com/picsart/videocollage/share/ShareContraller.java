package com.picsart.videocollage.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.picsart.videocollage.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Tigran on 10/30/15.
 */
public class ShareContraller {

    private static final String Title = ShareContraller.class.getSimpleName();
    private static final String MARKET_PREFIX = "market://details?id=";
    public static final String url1 = "http://cdn76.picsart.com/187889742003202.gif";

    private Activity activity;

    private String filePath;
    private String fileMimeType;
    private ShareItem shareItem;

    private ArrayList<String> supportedPackageNames = new ArrayList<>();

    public ShareContraller(String filePath, ShareItem shareItem, Activity activity) {
        this.filePath = filePath;
        this.shareItem = shareItem;
        this.activity = activity;
        fileMimeType = checkFileMimeType(filePath);
        supportedPackageNames = PackageContraller.getAllSupportedPackageNames(fileMimeType, activity);
    }

    public void shareTo(String packageName) {
        if (filePath != null) {
            if (packageName == ShareConstants.MORE_PACKAGE_OPTIONS) {
                androidNativeShare();
            } else {
                if (PackageContraller.isPackageExisted(activity, packageName)) {
                    nativeShare(packageName);
                } else {
                    Toast.makeText(activity, activity.getString(shareItem.getItemTextStringId()) + " doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(activity, "file not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void nativeShare(String packageName) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, Title);
        share.setType(fileMimeType);
        Uri uri = Uri.fromFile(new File(filePath));  //Uri.parse(filePath);
        try {
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setPackage(packageName);
            activity.startActivity(share);
        } catch (Exception e) {
            e.printStackTrace();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(MARKET_PREFIX + packageName));
            activity.startActivity(intent);
        }
    }

    public void androidNativeShare() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, Title);
        share.setType(fileMimeType);
        File media = new File(filePath);
        Uri uri = Uri.fromFile(media);
        try {
            share.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(share, activity.getString(R.string.share_file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getSupportedPackageNames() {
        return supportedPackageNames;
    }

    public static String checkFileMimeType(String filePath) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            try {
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

}
