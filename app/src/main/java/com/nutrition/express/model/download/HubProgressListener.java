package com.nutrition.express.model.download;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class HubProgressListener implements ProgressResponseBody.ProgressListener, Runnable {
    private List<ProgressResponseBody.ProgressListener> listeners = new ArrayList<>();
    private static Handler handler = new Handler(Looper.getMainLooper());
    private long bytesRead, contentLength;
    private boolean done;

    public HubProgressListener() {
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {
        Log.d("RxDownload", bytesRead + "->" + contentLength);
        if (listeners.isEmpty()) return;
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
        this.done = done;
        handler.post(this);
    }

    public void addProgressListener(ProgressResponseBody.ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressResponseBody.ProgressListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void run() {
        for (ProgressResponseBody.ProgressListener listener : listeners) {
            listener.update(bytesRead, contentLength, done);
            Log.d("RxDownload-main", bytesRead + "->" + contentLength);
        }
    }

}
