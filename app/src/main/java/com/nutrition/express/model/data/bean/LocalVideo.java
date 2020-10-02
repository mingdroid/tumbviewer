package com.nutrition.express.model.data.bean;

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.nutrition.express.application.TumbApp;
import com.nutrition.express.model.api.bean.BaseVideoBean;

import java.io.File;

/**
 * Created by huang on 2/17/17.
 */

public class LocalVideo extends BaseVideoBean {
    private File file;
    private boolean checked = false;

    public LocalVideo(File file) {
        this.file = file;
        sourceUri = Uri.fromFile(file);
        thumbnailUri = sourceUri;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getPath());
            int videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            width = TumbApp.Companion.getApp().getWidth();
            height = width * videoHeight / videoWidth;
//            retriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            width = TumbApp.Companion.getApp().getWidth();
            height = width / 2;
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
//        Log.d("LocalVideo", videoWidth + "-" + videoHeight + ":" + width + "-" + height);
    }

    public File getFile() {
        return file;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
