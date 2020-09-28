package com.nutrition.express.model.download;

import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.google.gson.reflect.TypeToken;
import com.nutrition.express.BuildConfig;
import com.nutrition.express.application.TumbApp;
import com.nutrition.express.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import static com.nutrition.express.util.UtilsKt.md5sum;
import static com.nutrition.express.util.UtilsKt.read;

public class RxDownload {
    public static final int EOF = -1;
    public static final int FILE_EXIST = -2;
    public static final int ERROR = -3;
    public static final int PROCESSING = 0;
    public static final String FILE_NAME = "records";
    private OkHttpClient okHttpClient;
    private File parent;
    private HashMap<String, HubProgressListener> downloadingMap = new HashMap<>();
    private List<Record> records;

    private RxDownload() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addNetworkInterceptor(new ProgressInterceptor())
                .readTimeout(5, TimeUnit.MINUTES);
        okHttpClient = builder.build();
        parent = FileUtils.INSTANCE.getVideoDir();
        if (!parent.exists()) parent.mkdirs();
        records = read(FILE_NAME, new TypeToken<ArrayList<Record>>() {
        }.getType());
        if (records == null) {
            records = new ArrayList<>();
        }
    }

    public static RxDownload getInstance() {
        return Holder.holder;
    }

    public List<Record> getRecords() {
        return records;
    }

    public void removeProgressListener(final String url, ProgressResponseBody.ProgressListener listener) {
        if (TextUtils.isEmpty(url)) return;
        HubProgressListener hubProgressListener = downloadingMap.get(url);
        if (hubProgressListener != null) {
            hubProgressListener.removeProgressListener(listener);
        }
    }

    public int start(final String url, ProgressResponseBody.ProgressListener progressListener) {
        if (TextUtils.isEmpty(url)) return ERROR;
        HubProgressListener hubProgressListener = downloadingMap.get(url);
        if (hubProgressListener != null && progressListener != null) {
            hubProgressListener.addProgressListener(progressListener);
            return PROCESSING;
        }
        final HttpUrl parsed = HttpUrl.parse(url);
        if (parsed == null) return ERROR;
        String extension = ".mp4";
        List<String> segments = parsed.pathSegments();
        if (segments.size() > 0) {
            String lastPath = segments.get(segments.size() - 1);
            int index = lastPath.lastIndexOf(".");
            if (index >= 0) {
                extension = lastPath.substring(index);
            }
        }
        File dst = new File(parent, md5sum(url) + extension);
        if (dst.exists()) return FILE_EXIST;
        final HubProgressListener listener = new HubProgressListener();
        final Record record = new Record(url);
        if (progressListener != null) {
            listener.addProgressListener(progressListener);
        }
        downloadingMap.put(url, listener);
        records.add(record);
        //todo save records
//        Utils.store(FILE_NAME, records);
        Observable<Integer> observable = Observable.create(emitter -> {
            if (BuildConfig.DEBUG) {
                TrafficStats.setThreadStatsTag(parsed.hashCode());
            }
            Request.Builder builder = new Request.Builder();
            builder.url(parsed);
            builder.tag(listener);
            download(builder.build(), dst);
            emitter.onNext(EOF);
            emitter.onComplete();
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    //add video to system media
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(dst));
                    TumbApp.Companion.getApp().sendBroadcast(intent);

                    downloadingMap.remove(url);
                    records.remove(record);
                    //todo save records
//                    Utils.store(FILE_NAME, records);
                }, error -> {
                    error.printStackTrace();
                    dst.delete();
                    downloadingMap.remove(url);
                });
        return PROCESSING;
    }

    @WorkerThread
    public boolean download(Request request, File dst) {
        try {
            Log.d("RxDownload", "-> start");
            Response response = okHttpClient.newCall(request).execute();
            BufferedSink sink = Okio.buffer(Okio.sink(dst));
            sink.writeAll(response.body().source());
            sink.close();
            response.close();
            Log.d("RxDownload", "-> end");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static class Holder {
        private static RxDownload holder = new RxDownload();
    }

}
