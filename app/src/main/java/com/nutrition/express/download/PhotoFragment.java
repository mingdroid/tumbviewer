package com.nutrition.express.download;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nutrition.express.R;
import com.nutrition.express.application.ExpressApplication;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.imageviewer.PhotoViewActivity;
import com.nutrition.express.model.data.bean.LocalPhoto;
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

public class PhotoFragment extends Fragment {
    private static final String SHOW_USER_PHOTO = "SUP";
    private final int HALF_WIDTH = ExpressApplication.width / 2;
    private RecyclerView recyclerView;
    private CommonRVAdapter adapter;
    private DownloadManagerActivity activity;

    private List<Object> userPhoto;
    private List<Object> allPhoto;
    private List<Object> photoList;
    private boolean showUserPhoto;

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
                    checkAllPhotos();
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
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (DownloadManagerActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        showUserPhoto = PreferencesUtils.getBoolean(SHOW_USER_PHOTO, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter == null) {
            init();
        }
        if (!isVisibleToUser && actionMode != null) {
            actionMode.finish();
            adapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_photo, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

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
        inflater.inflate(R.menu.menu_download_photo, menu);
        MenuItem menuItem = menu.findItem(R.id.show_user_photo);
        if (showUserPhoto) {
            menuItem.setChecked(true);
        } else {
            menuItem.setChecked(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_user_photo) {
            item.setChecked(!item.isChecked());
            showUserPhoto = item.isChecked();
            if (showUserPhoto) {
                initPhotoDataUser();
            } else {
                initPhotoDataAll();
            }
            adapter.resetData(photoList.toArray(), false);
            PreferencesUtils.putBoolean(SHOW_USER_PHOTO, showUserPhoto);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        if (showUserPhoto) {
            initPhotoDataUser();
        } else {
            initPhotoDataAll();
        }
        adapter = CommonRVAdapter.newBuilder()
                .addItemType(LocalPhoto.class, R.layout.item_download_photo,
                        new CommonRVAdapter.CreateViewHolder() {
                            @Override
                            public CommonViewHolder createVH(View view) {
                                return new PhotoViewHolder(view);
                            }
                        })
                .build();
        adapter.resetData(photoList.toArray(), false);
        recyclerView.setAdapter(adapter);
    }

    private void initPhotoDataUser() {
        if (userPhoto == null) {
            userPhoto = new ArrayList<>();
            File userPhotoDir = FileUtils.getImageDir();
            if (userPhotoDir.isDirectory()) {
                getPhotoFile(userPhotoDir, userPhoto);
                sortPhotoData(userPhoto);
            }
        }
        photoList = userPhoto;
    }

    private void initPhotoDataAll() {
        if (allPhoto == null) {
            allPhoto = new ArrayList<>();
            File publicPhotoDir = FileUtils.getPublicImageDir();
            if (publicPhotoDir.isDirectory()) {
                getPhotoFile(publicPhotoDir, allPhoto);
                sortPhotoData(allPhoto);
            }
        }
        photoList = allPhoto;
    }

    private void getPhotoFile(File dir, List<Object> list) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        LocalPhoto tmp;
        for (File file : files) {
            if (file.isDirectory()) {
                getPhotoFile(file, list);
            } else {
                tmp = new LocalPhoto(file);
                if (tmp.isValid()) {
                    list.add(tmp);
                }
            }
        }
    }

    private void sortPhotoData(List<Object> photos) {
        Collections.sort(photos, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                long x = ((LocalPhoto) o1).getFile().lastModified();
                long y = ((LocalPhoto) o2).getFile().lastModified();
                return (x < y) ? 1 : ((x == y) ? 0 : -1);
            }
        });
    }

    private void startMultiChoice() {
        //reset
        LocalPhoto tmp;
        for (Object photo: photoList) {
            tmp = (LocalPhoto) photo;
            tmp.setChecked(false);
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

    private void onItemChecked(LocalPhoto localPhoto) {
        localPhoto.setChecked(!localPhoto.isChecked());
        if (localPhoto.isChecked()) {
            checkedCount++;
        } else {
            checkedCount--;
        }
        actionMode.setTitle(String.valueOf(checkedCount));
    }

    private void deleteCheckedPhotos() {
        LocalPhoto tmp;
        Iterator<Object> i = photoList.iterator();
        while (i.hasNext()) {
            tmp = (LocalPhoto) i.next();
            if (tmp.isChecked()) {
                FileUtils.deleteFile(tmp.getFile());
                i.remove();
            }
        }
        if (photoList == allPhoto && userPhoto != null) {
            i = userPhoto.iterator();
        } else if (photoList == userPhoto && allPhoto != null) {
            i = allPhoto.iterator();
        } else {
            i = null;
        }
        if (i != null) {
            while (i.hasNext()) {
                tmp = (LocalPhoto) i.next();
                if (!tmp.getFile().exists()) {
                    i.remove();
                }
            }
        }
        adapter.resetData(photoList.toArray(), false);
    }

    private void checkAllPhotos() {
        LocalPhoto tmp;
        for (Object photo: photoList) {
            tmp = (LocalPhoto) photo;
            tmp.setChecked(true);
        }
        checkedCount = photoList.size();
        actionMode.setTitle(String.valueOf(checkedCount));
        adapter.notifyDataSetChanged();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton(R.string.delete_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCheckedPhotos();
                finishMultiChoice();
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setTitle(R.string.download_photo_delete_title);
        builder.show();
    }

    public void scrollToTop() {
        recyclerView.scrollToPosition(0);
    }

    private final class PhotoViewHolder extends CommonViewHolder<LocalPhoto>
            implements View.OnClickListener, View.OnLongClickListener {
        private SimpleDraweeView photoView;
        private ImageView checkView;

        private LocalPhoto photo;

        private final int DEFAULT_PHOTO_WIDTH;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoView = (SimpleDraweeView) itemView.findViewById(R.id.photoView);
            checkView = (ImageView) itemView.findViewById(R.id.check_view);
            photoView.setOnClickListener(this);
            photoView.setOnLongClickListener(this);

            int margin = (int) (8 * itemView.getContext().getResources().getDisplayMetrics().density);
            DEFAULT_PHOTO_WIDTH = HALF_WIDTH - margin;
        }

        @Override
        public void bindView(LocalPhoto localPhoto) {
            photo = localPhoto;
            int height = localPhoto.getHeight() * DEFAULT_PHOTO_WIDTH / localPhoto.getWidth();
            ViewGroup.LayoutParams params = photoView.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(DEFAULT_PHOTO_WIDTH, height);
            }
            params.width = DEFAULT_PHOTO_WIDTH;
            params.height = height;
            photoView.setLayoutParams(params);
            photoView.setImageURI(localPhoto.getUri());
            if (isChoiceState && photo.isChecked()) {
                checkView.setVisibility(View.VISIBLE);
            } else {
                checkView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isChoiceState) {
                onItemChecked(photo);
                if (photo.isChecked()) {
                    checkView.setVisibility(View.VISIBLE);
                } else {
                    checkView.setVisibility(View.GONE);
                }
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        getActivity(), photoView, photo.getUri().getPath());
                intent.putExtra("transition_name", photo.getUri().getPath());
                intent.putExtra("photo_source", photo.getUri());
                startActivity(intent, options.toBundle());
            } else {
                Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                intent.putExtra("photo_source", photo.getUri());
                startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (actionMode != null) {
                return true;
            }
            startMultiChoice();
            onItemChecked(photo);
            checkView.setVisibility(View.VISIBLE);
            return true;
        }
    }

}
