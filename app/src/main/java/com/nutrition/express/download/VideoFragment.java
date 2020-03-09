package com.nutrition.express.download;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.nutrition.express.R;
import com.nutrition.express.common.CommonExoPlayerView;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.common.ExoPlayerInstance;
import com.nutrition.express.model.data.bean.LocalVideo;
import com.nutrition.express.util.FileUtils;
import com.nutrition.express.util.PreferencesUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by huang on 2/17/17.
 */

public class VideoFragment extends Fragment {
    private static final String SHOW_USER_VIDEO = "SUV";
    private RecyclerView recyclerView;
    private CommonRVAdapter adapter;
    private ExoPlayerInstance playerInstance;
    private DownloadManagerActivity activity;

    private List<Object> userVideo;
    private List<Object> allVideo;
    private List<Object> videoList;
    private boolean showUserVideo;

    private boolean isChoiceState = false;
    private int checkedCount;
    private ActionMode actionMode;
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_choice, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    showDeleteDialog();
                    return true;
                case R.id.select_all:
                    checkAllVideos();
                    adapter.notifyDataSetChanged();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            finishMultiChoice();
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DownloadManagerActivity) context;
        playerInstance = ExoPlayerInstance.getInstance();
        showUserVideo = PreferencesUtils.getBoolean(SHOW_USER_VIDEO, false);
        if (showUserVideo) {
            initVideoDataUser();
        } else {
            initVideoDataAll();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (playerInstance != null) {
                playerInstance.pausePlayer();
            }
            if (actionMode != null) {
                actionMode.finish();
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        playerInstance.resumePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        playerInstance.releasePlayer();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_video, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = CommonRVAdapter.newBuilder()
                .addItemType(LocalVideo.class, R.layout.item_download_video,
                        new CommonRVAdapter.CreateViewHolder() {
                            @Override
                            public CommonViewHolder createVH(View view) {
                                return new VideoViewHolder(view);
                            }
                        })
                .build();
        adapter.resetData(videoList.toArray(), false);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE ||
                        newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume();
                } else {
                    Fresco.getImagePipeline().pause();
                }
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_download_video, menu);
        MenuItem menuItem = menu.findItem(R.id.show_user_video);
        if (showUserVideo) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_user_video) {
            item.setChecked(!item.isChecked());
            showUserVideo = item.isChecked();
            if (showUserVideo) {
                initVideoDataUser();
            } else {
                initVideoDataAll();
            }
            adapter.resetData(videoList.toArray(), false);
            PreferencesUtils.putBoolean(SHOW_USER_VIDEO, showUserVideo);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initVideoDataUser() {
        if (userVideo == null) {
            userVideo = new ArrayList<>();
            File userVideoDir = FileUtils.getVideoDir();
            if (userVideoDir.isDirectory()) {
                getVideoFile(userVideoDir, userVideo);
                sortPhotoData(userVideo);
            }
        }
        videoList = userVideo;
    }

    private void initVideoDataAll() {
        if (allVideo == null) {
            allVideo = new ArrayList<>();
            File publicVideoDir = FileUtils.getPublicVideoDir();
            if (publicVideoDir.isDirectory()) {
                getVideoFile(publicVideoDir, allVideo);
                sortPhotoData(allVideo);
            }
        }
        videoList = allVideo;
    }

    private void getVideoFile(File dir, List<Object> list) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getVideoFile(file, list);
            } else {
                if (file.getName().endsWith(".mp4")) {
                    list.add(new LocalVideo(file));
                }
            }
        }
    }

    private void sortPhotoData(List<Object> videos) {
        Collections.sort(videos, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                long x = ((LocalVideo) o1).getFile().lastModified();
                long y = ((LocalVideo) o2).getFile().lastModified();
                return (x < y) ? 1 : ((x == y) ? 0 : -1);
            }
        });
    }

    private void startMultiChoice() {
        //reset
        LocalVideo tmpVideo;
        for (Object video : videoList) {
            tmpVideo = (LocalVideo) video;
            tmpVideo.setChecked(false);
        }
        actionMode = activity.startMultiChoice(callback);
        checkedCount = 0;
        isChoiceState = true;
    }

    private void finishMultiChoice() {
        actionMode.finish();
        actionMode = null;
        isChoiceState = false;
    }

    private void onItemChecked(LocalVideo localVideo) {
        localVideo.setChecked(!localVideo.isChecked());
        if (localVideo.isChecked()) {
            checkedCount++;
        } else {
            checkedCount--;
        }
        actionMode.setTitle(String.valueOf(checkedCount));
    }

    private void deleteCheckedVideos() {
        LocalVideo tmpVideo;
        Iterator<Object> i = videoList.iterator();
        while (i.hasNext()) {
            tmpVideo = (LocalVideo) i.next();
            if (tmpVideo.isChecked()) {
                FileUtils.deleteFile(tmpVideo.getFile());
                i.remove();
            }
        }
        if (videoList == allVideo && userVideo != null) {
            i = userVideo.iterator();
        } else if (videoList == userVideo && allVideo != null) {
            i = allVideo.iterator();
        } else {
            i = null;
        }
        if (i != null) {
            while (i.hasNext()) {
                tmpVideo = (LocalVideo) i.next();
                if (!tmpVideo.getFile().exists()) {
                    i.remove();
                }
            }
        }
        adapter.resetData(videoList.toArray(), false);
    }

    private void checkAllVideos() {
        LocalVideo tmpVideo;
        for (Object video : videoList) {
            tmpVideo = (LocalVideo) video;
            tmpVideo.setChecked(true);
        }
        checkedCount = videoList.size();
        actionMode.setTitle(String.valueOf(checkedCount));
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.delete_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCheckedVideos();
                finishMultiChoice();
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setTitle(R.string.download_video_delete_title);
        builder.show();
    }


    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

    private class VideoViewHolder extends CommonViewHolder<LocalVideo>
            implements View.OnClickListener, View.OnLongClickListener {
        private CommonExoPlayerView playerView;
        private ImageView checkView;

        private LocalVideo video;

        private VideoViewHolder(View itemView) {
            super(itemView);
            playerView = (CommonExoPlayerView) itemView.findViewById(R.id.player_view);
            checkView = (ImageView) itemView.findViewById(R.id.check_view);
            playerView.setPlayerInstance(playerInstance);
            playerView.setOnClickListener(this);
            playerView.setOnLongClickListener(this);
            playerView.setPlayerClickable(false);
        }

        @Override
        public void bindView(LocalVideo localVideo) {
            video = localVideo;
            playerView.bindVideo(localVideo);
            if (isChoiceState && video.isChecked()) {
                checkView.setVisibility(View.VISIBLE);
            } else {
                checkView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isChoiceState) {
                onItemChecked(video);
                if (video.isChecked()) {
                    checkView.setVisibility(View.VISIBLE);
                } else {
                    checkView.setVisibility(View.GONE);
                }
            } else {
                playerView.performPlayerClick();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (actionMode != null) {
                return true;
            }
            startMultiChoice();
            onItemChecked(video);
            checkView.setVisibility(View.VISIBLE);
            return true;
        }
    }

}
