package com.picsart.videocollage.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.picsart.videocollage.R;
import com.picsart.videocollage.adapter.ShareAdapter;
import com.picsart.videocollage.share.ShareConstants;
import com.picsart.videocollage.share.ShareContraller;
import com.picsart.videocollage.share.ShareItem;
import com.picsart.videocollage.utils.Constants;
import com.picsart.videocollage.utils.MediaScannerNotifier;
import com.picsart.videocollage.utils.RecyclerItemClickListener;

import java.util.ArrayList;

public class ShareActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShareActivity.class.getSimpleName();
    private String savedFilePath = null;

    private RecyclerView shareItemRecyclerView;

    private ShareAdapter shareAdapter;
    ArrayList<ShareItem> shareItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Intent intent = getIntent();
        savedFilePath = intent.getStringExtra(Constants.INTENT_FILE_PATHS);
        new MediaScannerNotifier(getApplicationContext(), savedFilePath, ShareContraller.checkFileMimeType(savedFilePath));

        shareItems.add(new ShareItem(R.drawable.icon_fb, R.string.share_facebook_string, ShareConstants.FB_PACKAGE_NAME));
        shareItems.add(new ShareItem(R.drawable.icon_insta, R.string.share_instagram_string, ShareConstants.INSTAGRAM_PACKAGE_NAME));
        shareItems.add(new ShareItem(R.drawable.icon_messenger, R.string.share_messenger_string, ShareConstants.FB_MESSENGER_PACKAGE_NAME));
        shareItems.add(new ShareItem(R.drawable.icon_whatsapp, R.string.share_whatsapp_string, ShareConstants.WHATSAPP_PACKAGE));
        shareItems.add(new ShareItem(R.drawable.icon_more, R.string.share_more_string, ShareConstants.MORE_PACKAGE_OPTIONS));

        init();
    }

    private void init() {
        shareItemRecyclerView = (RecyclerView) findViewById(R.id.share_item_recycler_view);
        shareItemRecyclerView.setHasFixedSize(true);
        shareItemRecyclerView.setClipToPadding(true);
        shareItemRecyclerView.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false));
        shareItemRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //shareItemRecyclerView.addItemDecoration(new SpacesItemDecoration((int) Utils.dpToPixel(1, this)));

        shareAdapter = new ShareAdapter(shareItems, getApplicationContext());
        shareItemRecyclerView.setAdapter(shareAdapter);

        shareItemRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ShareItem shareItem = shareItems.get(position);
                ShareContraller shareContraller = new ShareContraller(savedFilePath, shareItem, ShareActivity.this);
                shareContraller.shareTo(shareItem.getPackageName());

            }
        }));
    }
}
