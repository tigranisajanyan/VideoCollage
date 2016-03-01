package com.picsart.videocollage.share;

/**
 * Created by Tigran on 10/30/15.
 */
public class ShareItem {

    private int resourceId;
    private int itemTextStringId;
    private String packageName;

    public ShareItem(int resourceId, int itemTextStringId, String packageName) {
        this.resourceId = resourceId;
        this.itemTextStringId = itemTextStringId;
        this.packageName = packageName;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getItemTextStringId() {
        return itemTextStringId;
    }

    public void setItemTextStringId(int itemTextStringId) {
        this.itemTextStringId = itemTextStringId;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


}
