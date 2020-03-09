package com.nutrition.express.util;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.nutrition.express.application.Constants;
import com.nutrition.express.application.ExpressApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by huang on 11/5/16.
 */

public class FrescoUtils {

    public static void setTumblrAvatarUri(SimpleDraweeView view, String name, int size) {
        String url = Constants.BASE_URL + "/v2/blog/" + name + "/avatar/";
        if (size > 0 && size <= 512) {
            url +=  size;
        }
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(view.getController())
                .setImageRequest(imageRequest)
                .build();
        view.setController(controller);
    }

    public static void save(Uri uri, String action) {
        ImageRequest request = ImageRequest.fromUri(uri);
        ImagePipeline pipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<PooledByteBuffer>> dataSource =
                pipeline.fetchEncodedImage(request, null);

        ImageSubscriber dataSubscriber = new ImageSubscriber(uri, action);

        dataSource.subscribe(dataSubscriber,
                ExpressApplication.getApplication().getImagePipelineConfig()
                        .getExecutorSupplier().forLocalStorageWrite());
    }

    public static void saveAll(List<Uri> uris, String action) {
        for (Uri uri : uris) {
            save(uri, action);
        }
    }

    private static class ImageSubscriber extends BaseDataSubscriber<CloseableReference<PooledByteBuffer>> {
        private Uri uri;
        private String action;

        public ImageSubscriber(Uri uri, String action) {
            this.uri = uri;
            this.action = action;
        }

        @Override
        protected void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
            if (!dataSource.isFinished()) {
                return;
            }
            CloseableReference<PooledByteBuffer> ref = dataSource.getResult();
            FileOutputStream outputStream = null;
            if (ref != null) {
                try {
                    File file = FileUtils.createImageFile(uri);
                    PooledByteBuffer buffer = ref.get();
                    if (!file.exists() || file.length() != buffer.size()) {
                        outputStream = new FileOutputStream(file);
                        byte[] bytes = new byte[buffer.size()];
                        buffer.read(0, bytes, 0, buffer.size());
                        outputStream.write(bytes);
                        outputStream.flush();
                        galleryAddPic(file);
                    }
                    if (!TextUtils.isEmpty(action)) {
                        Intent intent = new Intent(action);
                        intent.putExtra("success", true);
                        intent.putExtra("uri", uri);
                        LocalBroadcastManager.getInstance(ExpressApplication.getApplication())
                                .sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    onFailureImpl(dataSource);
                } finally {
                    CloseableReference.closeSafely(ref);
                    ref = null;
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
            if (!TextUtils.isEmpty(action)) {
                Intent intent = new Intent(action);
                intent.putExtra("success", false);
                intent.putExtra("uri", uri);
                LocalBroadcastManager.getInstance(ExpressApplication.getApplication())
                        .sendBroadcast(intent);
            }
        }

        private void galleryAddPic(File f) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            ExpressApplication.getApplication().sendBroadcast(mediaScanIntent);
        }
    }
}
