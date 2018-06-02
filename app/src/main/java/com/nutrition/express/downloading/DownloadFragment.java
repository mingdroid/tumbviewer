package com.nutrition.express.downloading;

import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nutrition.express.R;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.util.DownloadManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import zlc.season.rxdownload2.RxDownload;
import zlc.season.rxdownload2.entity.DownloadEvent;
import zlc.season.rxdownload2.entity.DownloadFlag;
import zlc.season.rxdownload2.entity.DownloadRecord;
import zlc.season.rxdownload2.entity.DownloadStatus;

/**
 * Created by huang on 4/10/17.
 */

public class DownloadFragment extends Fragment {
    private RxDownload rxDownload;
    private HashSet<Disposable> disposables = new HashSet<>();

    private CommonRVAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = CommonRVAdapter.newBuilder()
                .addItemType(DownloadRecord.class, R.layout.item_download, new CommonRVAdapter.CreateViewHolder() {
                    @Override
                    public CommonViewHolder createVH(View view) {
                        return new RxDownloadVH(view);
                    }
                })
                .build();
        recyclerView.setAdapter(adapter);

        getDownloadStatus();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }

    @Deprecated
    public void setDownloadRecords(List<DownloadRecord> records) {
        List<Object> data = new ArrayList<>();
        rxDownload = DownloadManager.getInstance().getRxDownload();
        for (DownloadRecord record : records) {
            if (record.getFlag() == DownloadFlag.COMPLETED) {
                rxDownload.deleteServiceDownload(record.getUrl(), false);
            } else {
                data.add(record);
            }
        }
        if (adapter != null) {
            adapter.append(data.toArray(), false);
        }
    }

    private void getDownloadStatus() {
        rxDownload = DownloadManager.getInstance().getRxDownload();
        rxDownload.getTotalDownloadRecords()
                .subscribe(new Consumer<List<DownloadRecord>>() {
                    @Override
                    public void accept(List<DownloadRecord> downloadRecords) throws Exception {
                        setContentData(downloadRecords);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        setContentData(null);
                    }
                });
    }

    private void setContentData(List<DownloadRecord> records) {
        if (records != null) {
            List<Object> data = new ArrayList<>();
            for (DownloadRecord record : records) {
                if (record.getFlag() == DownloadFlag.COMPLETED) {
                    rxDownload.deleteServiceDownload(record.getUrl(), false);
                } else {
                    data.add(record);
                }
            }
            adapter.append(data.toArray(), false);
        } else {
            adapter.append(null, false);
        }
    }

    private class RxDownloadVH extends CommonViewHolder<DownloadRecord> implements View.OnClickListener {
        private ImageView stateView;
        private TextView urlView, progress;
        private ClipDrawable progressDrawable;
        private Disposable disposable;
        private DownloadRecord record;
        private int status = 0;

        RxDownloadVH(View itemView) {
            super(itemView);
            progressDrawable = (ClipDrawable) itemView.getBackground();
            stateView = (ImageView) itemView.findViewById(R.id.download_state);
            progress = (TextView) itemView.findViewById(R.id.progress);
            urlView = (TextView) itemView.findViewById(R.id.url);
            stateView.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bindView(final DownloadRecord downloadRecord) {
            this.record = downloadRecord;
            if (TextUtils.isEmpty(downloadRecord.getSaveName())) {
                String url = downloadRecord.getUrl();
                urlView.setText(url.substring(url.lastIndexOf("/") + 1));
            } else {
                urlView.setText(downloadRecord.getSaveName());
            }
            disposable = rxDownload.receiveDownloadStatus(downloadRecord.getUrl())
                    .subscribe(new Consumer<DownloadEvent>() {
                        @Override
                        public void accept(DownloadEvent downloadEvent) throws Exception {
                            Log.d("accept", "-" + downloadEvent.getFlag());
                            if (downloadEvent.getFlag() == DownloadFlag.FAILED) {
                                Log.w("Download", "accept: ", downloadEvent.getError());
                            }
                            disposables.add(disposable);
                            if (downloadEvent.getFlag() == DownloadFlag.STARTED) {
                                updateProgress(downloadEvent);
                            } else if (downloadEvent.getFlag() == DownloadFlag.COMPLETED) {
                                downloadComplete(getAdapterPosition());
                            } else if (downloadEvent.getFlag() == DownloadFlag.PAUSED) {

                            }
                            setStatus(downloadEvent);
                        }
                    });
        }

        private void setStatus(DownloadEvent event) {
            if (event.getFlag() == status) {
                return;
            }
            status = event.getFlag();
            if (status == DownloadFlag.STARTED) {
                stateView.setImageResource(R.mipmap.ic_pause_black_24dp);
            } else if (status == DownloadFlag.FAILED) {
                stateView.setImageResource(R.mipmap.ic_failed);
            } else {
                stateView.setImageResource(R.mipmap.ic_play_arrow_black_24dp);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.download_state) {
                if (status == DownloadFlag.STARTED) {
                    rxDownload.pauseServiceDownload(record.getUrl());
                    stateView.setImageResource(R.mipmap.ic_play_arrow_black_24dp);
                    status = DownloadFlag.PAUSED;
                } else if (status == DownloadFlag.PAUSED) {
                    rxDownload.serviceDownload(record.getUrl());
                    stateView.setImageResource(R.mipmap.ic_pause_black_24dp);
                    status = DownloadFlag.STARTED;
                }
            } else {
                DownloadManager.getInstance().download(record.getUrl());
            }
        }

        private void updateProgress(DownloadEvent event) {
            if (ViewCompat.isAttachedToWindow(stateView)) {
                DownloadStatus status = event.getDownloadStatus();
                progressDrawable.setLevel(100 * (int) status.getPercentNumber());
                progress.setText(status.getFormatStatusString());
            } else {
                if (disposable != null) {
                    disposable.dispose();
                    disposables.remove(disposable);
                    disposable = null;
                }
            }
        }

        private void downloadComplete(int pos) {
            rxDownload.deleteServiceDownload(record.getUrl(), false);
            adapter.remove(pos);
        }
    }
}
