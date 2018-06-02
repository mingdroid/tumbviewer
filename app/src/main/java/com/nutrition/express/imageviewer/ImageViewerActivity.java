package com.nutrition.express.imageviewer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeTransition;
import com.nutrition.express.R;
import com.nutrition.express.common.DismissFrameLayout;
import com.nutrition.express.imageviewer.zoomable.ZoomableDraweeView;
import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.model.event.EventPermission;
import com.nutrition.express.util.FileUtils;
import com.nutrition.express.util.FrescoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.nutrition.express.R.id.save;

/**
 * Created by huang on 1/21/16.
 */
public class ImageViewerActivity extends AppCompatActivity
        implements DismissFrameLayout.OnDismissListener {
    private final String ACTION = "SAVE_IMAGE";
    private ViewPager viewPager;
    private LinearLayout indicator;
    private ImageView mImageViews[];
    private List<Uri> photoUris;
    private int savedCount, failureCount;
    private int selectedIndex;
    private int desiredSavedCount;
    private boolean isTransitionEnd = false;
    private FloatingActionButton saveButton;
    private ColorDrawable colorDrawable;
    private int ALPHA_MAX = 0xFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(DraweeTransition.createTransitionSet(
                    ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.FIT_CENTER));
            getWindow().setSharedElementReturnTransition(DraweeTransition.createTransitionSet(
                    ScalingUtils.ScaleType.FIT_CENTER, ScalingUtils.ScaleType.CENTER_CROP));
            postponeEnterTransition();
        }
        setContentView(R.layout.activity_view_image);
        saveButton = (FloatingActionButton) findViewById(save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveAll();
                return true;
            }
        });

        final CoordinatorLayout container = (CoordinatorLayout) findViewById(R.id.container);
        colorDrawable = new ColorDrawable(getResources().getColor(R.color.divider_color));
        container.setBackgroundDrawable(colorDrawable);
        ViewCompat.setOnApplyWindowInsetsListener(container, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                if (v instanceof CoordinatorLayout) {
                    CoordinatorLayout layout = (CoordinatorLayout) v;
                    final int count = layout.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View view = layout.getChildAt(i);
                        if (view instanceof FloatingActionButton) {
                            ViewGroup.LayoutParams lp = view.getLayoutParams();
                            if (lp instanceof CoordinatorLayout.LayoutParams) {
                                ((CoordinatorLayout.LayoutParams) lp).bottomMargin += insets.getSystemWindowInsetBottom();
                            }
                        } else {
                            view.setPadding(view.getLeft(), view.getTop(), view.getRight(),
                                    view.getBottom() + insets.getSystemWindowInsetBottom());
                        }
                    }
                    ViewCompat.setOnApplyWindowInsetsListener(container, null);
                }
                return insets;
            }
        });
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        indicator = (LinearLayout) findViewById(R.id.indicator_container);
        selectedIndex = getIntent().getIntExtra("selected_index", 0);
        List<String> photoUrls = getIntent().getStringArrayListExtra("image_urls");
        convert2Uri(photoUrls);
        if (photoUris != null) {
            viewPager.setAdapter(new ViewImageAdapter(photoUris));
            if (photoUris.size() > 1) {
                viewPager.addOnPageChangeListener(pageChangeListener);
                setIndicator(photoUris.size());
                if (selectedIndex < photoUris.size()) {
                    viewPager.setCurrentItem(selectedIndex);
                }
            }
        } else {
            Toast.makeText(this, R.string.pic_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(ACTION));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(
                    new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {
                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().getSharedElementEnterTransition().removeListener(this);
                                if (!FileUtils.imageSaved(photoUris.get(selectedIndex))) {
                                    saveButton.show();
                                }
                            }
                            isTransitionEnd = true;
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {
                        }

                        @Override
                        public void onTransitionPause(Transition transition) {
                        }

                        @Override
                        public void onTransitionResume(Transition transition) {
                        }
                    });
        } else {
            if (!FileUtils.imageSaved(photoUris.get(selectedIndex))) {
                saveButton.show();
            }
            isTransitionEnd = true;
        }
    }

    @Override
    public void onScaleProgress(float scale) {
        colorDrawable.setAlpha(
                Math.min(ALPHA_MAX, colorDrawable.getAlpha() - (int) (scale * ALPHA_MAX)));
        saveButton.hide();
    }

    @Override
    public void onDismiss() {
        finishAction(null);
    }

    @Override
    public void onCancel() {
        colorDrawable.setAlpha(ALPHA_MAX);
        if (!FileUtils.imageSaved(photoUris.get(selectedIndex))) {
            saveButton.show();
        }
    }

    private void finishAction(@Nullable ZoomableDraweeView draweeView) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (draweeView != null) {
                draweeView.reset();
            }
            saveButton.hide();
            finishAfterTransition();
        } else {
            finish();
        }
    }

    private void convert2Uri(List<String> urls) {
        photoUris = new ArrayList<>(urls.size());
        for (String url : urls) {
            if (!TextUtils.isEmpty(url)) {
                photoUris.add(Uri.parse(url));
            }
        }

    }

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (ImageView imageView : mImageViews) {
                imageView.setImageResource(R.mipmap.radiobutton_default);
            }
            mImageViews[position].setImageResource(R.mipmap.radiobutton_select);
            if (isTransitionEnd) {
                if (FileUtils.imageSaved(photoUris.get(position))) {
                    saveButton.hide();
                } else {
                    saveButton.show();
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void setIndicator(int imageCount) {
        indicator.removeAllViews();
        ImageView mImageView;
        float mScale = getResources().getDisplayMetrics().density;
        // 图片广告数量
         mImageViews = new ImageView[imageCount];
        for (int i = 0; i < imageCount; i++) {
            mImageView = new ImageView(this);
            // int imageParams = (int) (mScale * 10 + 0.5f);// XP与DP转换，适应不同分辨率
            int imagePadding = (int) (mScale * 2 + 0.5f);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mImageView.setPadding(imagePadding, imagePadding, imagePadding,
                    imagePadding);
            mImageViews[i] = mImageView;
            if (i == 0) {
                mImageViews[i].setImageResource(R.mipmap.radiobutton_select);
            } else {
                mImageViews[i].setImageResource(R.mipmap.radiobutton_default);
            }
            if (imageCount > 1) {
                indicator.addView(mImageViews[i]);
            }
        }
    }

    private class ViewImageAdapter extends PagerAdapter implements View.OnClickListener {
        private List<Uri> uris;
        private LinkedList<View> viewCache = new LinkedList<>();

        public ViewImageAdapter(List<Uri> uris) {
            this.uris = uris;
        }

        @Override
        public int getCount() {
            return uris.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            DismissFrameLayout layout;
            ZoomableDraweeView draweeView;
            if (viewCache.size() == 0) {
                draweeView = new ZoomableDraweeView(container.getContext());
                draweeView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .setPlaceholderImage(R.color.divider_color)
                        .build();
                draweeView.setHierarchy(hierarchy);

                layout = new DismissFrameLayout(container.getContext());
                layout.setDismissListener(ImageViewerActivity.this);
                layout.setLayoutParams(new ViewPager.LayoutParams());
                layout.addView(draweeView);
            } else {
                layout = (DismissFrameLayout) viewCache.removeFirst();
                draweeView = (ZoomableDraweeView) layout.getChildAt(0);
            }
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setAutoPlayAnimations(true)
                    .setUri(uris.get(position))
                    .build();
            draweeView.setController(controller);
            draweeView.setOnClickListener(this);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                String tag = "name" + position;
                draweeView.setTransitionName(tag);
                draweeView.setTag(tag);
                if (position == selectedIndex) {
                    setStartPostTransition(draweeView);
                }
            }

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            viewCache.add((View) object);
        }

        @Override
        public void onClick(View v) {
            finishAction((ZoomableDraweeView) v);
        }
    }

    @Override
    public void finishAfterTransition() {
        int pos = viewPager.getCurrentItem();
        if (selectedIndex != pos) {
            DataManager.getInstance().setPosition(viewPager.getCurrentItem());
            View view = viewPager.findViewWithTag("name" + pos);
            setSharedElementCallback(view);
        }
        super.finishAfterTransition();
    }

    @TargetApi(21)
    private void setSharedElementCallback(final View view) {
        SharedElementCallback callback = new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                names.clear();
                sharedElements.clear();
                names.add(view.getTransitionName());
                sharedElements.put(view.getTransitionName(), view);
            }
        };
        setEnterSharedElementCallback(callback);
    }

    @TargetApi(21)
    private void setStartPostTransition(final View sharedView) {
        sharedView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedView.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return false;
                    }
                });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra("success", false);
            if (success) {
                savedCount++;
                Uri uri = intent.getParcelableExtra("uri");
                if (photoUris.get(viewPager.getCurrentItem()).equals(uri)) {
                    saveButton.hide();
                }
            } else {
                failureCount++;
            }
            if (savedCount + failureCount == desiredSavedCount) {
                if (failureCount > 0) {
                    Toast.makeText(ImageViewerActivity.this, R.string.pic_saved_failure,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ImageViewerActivity.this, R.string.pic_saved,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void save() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            EventBus.getDefault().post(new EventPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        } else {
            desiredSavedCount = 1;
            savedCount = 0;
            failureCount = 0;
            FrescoUtils.save(photoUris.get(viewPager.getCurrentItem()), ACTION);
        }
    }

    private void saveAll() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            EventBus.getDefault().post(new EventPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        } else {
            desiredSavedCount = 0;
            savedCount = 0;
            failureCount = 0;
            for (Uri uri : photoUris) {
                if (!FileUtils.imageSaved(uri)) {
                    FrescoUtils.save(uri, ACTION);
                    desiredSavedCount++;
                }
            }
        }
    }
}
