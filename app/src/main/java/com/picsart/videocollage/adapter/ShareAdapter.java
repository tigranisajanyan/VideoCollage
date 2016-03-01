package com.picsart.videocollage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.picsart.videocollage.R;
import com.picsart.videocollage.share.ShareItem;

import java.util.ArrayList;

/**
 * Created by Tigran on 10/30/15.
 */
public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {

    private LayoutInflater inflater = null;
    private Context context;
    private ArrayList<ShareItem> shareItems = new ArrayList<>();

    public ShareAdapter(ArrayList<ShareItem> shareItems, Context context) {
        this.context = context;
        this.shareItems = shareItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.getContext());
        }
        return new ViewHolder(inflater.inflate(R.layout.share_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(context).load(shareItems.get(position).getResourceId()).into(holder.shareItemImage);
        holder.shareItemTitle.setText(context.getString(shareItems.get(position).getItemTextStringId()));
    }

    @Override
    public int getItemCount() {
        return shareItems.size();
    }


    public ShareItem getItem(int position) {
        return shareItems.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView shareItemImage;
        public TextView shareItemTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            shareItemImage = (ImageView) itemView.findViewById(R.id.share_item_image);
            shareItemTitle = (TextView) itemView.findViewById(R.id.share_item_title);
        }
    }

}
