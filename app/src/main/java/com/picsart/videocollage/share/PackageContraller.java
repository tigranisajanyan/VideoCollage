package com.picsart.videocollage.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tigran on 11/2/15.
 */
public class PackageContraller {

    public static boolean isPackageExisted(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static ArrayList<String> getAllSupportedPackageNames(String fileMimeType, Context context) {

        if (fileMimeType == null) return null;
        PackageManager pm = context.getPackageManager();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType(fileMimeType);

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);

        ArrayList<String> packageNames = new ArrayList<>();
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            packageNames.add(packageName);
            Log.d("gaga", packageName);
        }
        return packageNames;
    }

}
