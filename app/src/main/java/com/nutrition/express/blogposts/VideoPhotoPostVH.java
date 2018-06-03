package com.nutrition.express.blogposts;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nutrition.express.R;
import com.nutrition.express.common.CommonExoPlayerView;
import com.nutrition.express.common.ExoPlayerInstance;
import com.nutrition.express.model.data.bean.OnlineVideo;
import com.nutrition.express.model.data.bean.VideoPostsItem;
import com.nutrition.express.model.download.RxDownload;
import com.nutrition.express.util.Utils;

/**
 * Created by huang on 2/23/17.
 */

public class VideoPhotoPostVH extends PhotoPostVH<VideoPostsItem> implements View.OnLongClickListener {
    private CommonExoPlayerView playerView;
    private ImageView downloadView;

    private OnlineVideo onlineVideo;

    public VideoPhotoPostVH(View itemView, ExoPlayerInstance playerInstance) {
        super(itemView);
        playerView = (CommonExoPlayerView) itemView.findViewById(R.id.post_video);
        playerView.setPlayerInstance(playerInstance);
        playerView.setOnClickListener(this);
        downloadView = (ImageView) itemView.findViewById(R.id.post_download);
        downloadView.setOnClickListener(this);
        downloadView.setOnLongClickListener(this);
    }

    @Override
    public void bindView(VideoPostsItem videoPostsItem) {
        onlineVideo = videoPostsItem.getOnlineVideo(); //set this before call super.bindView();
        super.bindView(videoPostsItem);

        if (TextUtils.isEmpty(postsItem.getVideo_url())) {
            downloadView.setVisibility(View.GONE);
        } else {
            downloadView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void setVideoContent() {
        playerView.bindVideo(onlineVideo);
        if (TextUtils.equals("tumblr", postsItem.getVideo_type())) {
            playerView.setPlayerClickable(true);
        } else {
            playerView.setPlayerClickable(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.post_video) {
            if (!TextUtils.isEmpty(postsItem.getPermalink_url())) {
                //case "vine","youtube","instagram".
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(postsItem.getPermalink_url()));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
        } else if (v.getId() == R.id.post_download) {
            if (Utils.canWrite2Storage()) {
                int status = RxDownload.getInstance().start(postsItem.getVideo_url(), null);
                if (status == RxDownload.PROCESSING) {
                    Toast.makeText(context, R.string.download_start, Toast.LENGTH_SHORT).show();
                } else if (status == RxDownload.FILE_EXIST) {
                    Toast.makeText(context, R.string.video_exist, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.reblog_failure, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.post_download) {
            openBottomSheet();
            return true;
        }
        return false;
    }

    private void openBottomSheet() {
        Bundle bundle = new Bundle();
        bundle.putString("video_url", postsItem.getVideo_url());
        PostBottomSheet bottomSheet = new PostBottomSheet();
        bottomSheet.setArguments(bundle);
        bottomSheet.show(((FragmentActivity) itemView.getContext()).getSupportFragmentManager(),
                bottomSheet.getTag());
    }
}
