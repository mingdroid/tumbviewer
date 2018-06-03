package com.nutrition.express.downloading;

import android.graphics.drawable.ClipDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nutrition.express.R;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.model.download.ProgressResponseBody;
import com.nutrition.express.model.download.Record;
import com.nutrition.express.model.download.RxDownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huang on 4/10/17.
 */

public class DownloadFragment extends Fragment {
    private CommonRVAdapter adapter;
    private HashMap<String, ProgressResponseBody.ProgressListener> listeners = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = CommonRVAdapter.newBuilder()
                .addItemType(Record.class, R.layout.item_download, RxDownloadVH::new)
                .build();
        recyclerView.setAdapter(adapter);

        setContentData(new ArrayList<>(RxDownload.getInstance().getRecords()));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for (Map.Entry<String, ProgressResponseBody.ProgressListener> entry : listeners.entrySet()) {
            RxDownload.getInstance().removeProgressListener(entry.getKey(), entry.getValue());
        }
    }

    private void setContentData(List<Record> records) {
        if (records == null || records.isEmpty()) {
            adapter.append(null, false);
        } else {
            adapter.append(records.toArray(), false);
        }
    }

    private class RxDownloadVH extends CommonViewHolder<Record>
            implements View.OnClickListener, ProgressResponseBody.ProgressListener {
        private ImageView stateView;
        private TextView urlView, progress;
        private ClipDrawable progressDrawable;
        private Record record;

        RxDownloadVH(View itemView) {
            super(itemView);
            progressDrawable = (ClipDrawable) itemView.getBackground();
            stateView = itemView.findViewById(R.id.download_state);
            progress = itemView.findViewById(R.id.progress);
            urlView = itemView.findViewById(R.id.url);
            stateView.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bindView(final Record record) {
            this.record = record;
            urlView.setText(record.getUrl());
        }

        @Override
        public void onAttach() {
            RxDownload.getInstance().start(record.getUrl(), this);
            listeners.put(record.getUrl(), this);
        }

        @Override
        public void onDetach() {
            RxDownload.getInstance().removeProgressListener(record.getUrl(), this);
            listeners.remove(record.getUrl());
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public void update(long bytesRead, long contentLength, boolean done) {
            if (done) {
                adapter.remove(getAdapterPosition());
            } else {
                int percent = (int) (100 * bytesRead / contentLength);
                progressDrawable.setLevel(percent * 100);
                progress.setText(percent + "%");
            }
        }
    }
}
