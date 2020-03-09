package com.nutrition.express.imageviewer;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeTransition;
import com.nutrition.express.R;
import com.nutrition.express.common.DismissFrameLayout;
import com.nutrition.express.imageviewer.zoomable.ZoomableDraweeView;

/**
 * Created by huang on 2/22/17.
 */

public class PhotoViewActivity extends AppCompatActivity {
    private ColorDrawable colorDrawable;
    private int ALPHA_MAX = 0xFF;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setSharedElementEnterTransition(DraweeTransition.createTransitionSet(
                    ScalingUtils.ScaleType.FIT_CENTER, ScalingUtils.ScaleType.FIT_CENTER));
            getWindow().setSharedElementReturnTransition(DraweeTransition.createTransitionSet(
                    ScalingUtils.ScaleType.FIT_CENTER, ScalingUtils.ScaleType.FIT_CENTER));
        }
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_photo_view);

        Uri uri = getIntent().getParcelableExtra("photo_source");
        String transitionName = getIntent().getStringExtra("transition_name");

        DismissFrameLayout layout = (DismissFrameLayout) findViewById(R.id.dismiss_layout);
        colorDrawable = new ColorDrawable(getResources().getColor(R.color.divider_color));
        layout.setBackgroundDrawable(colorDrawable);
        layout.setDismissListener(new DismissFrameLayout.OnDismissListener() {
            @Override
            public void onDismiss() {
                finishAction(null);
            }

            @Override
            public void onCancel() {
                colorDrawable.setAlpha(ALPHA_MAX);
            }

            @Override
            public void onScaleProgress(float scale) {
                colorDrawable.setAlpha(
                        Math.min(ALPHA_MAX, colorDrawable.getAlpha() - (int) (scale * ALPHA_MAX)));
            }
        });

        colorDrawable = new ColorDrawable(getResources().getColor(R.color.divider_color));
        layout.setBackgroundDrawable(colorDrawable);
        ZoomableDraweeView draweeView = (ZoomableDraweeView) findViewById(R.id.photoView);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            draweeView.setTransitionName(transitionName);
        }
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setPlaceholderImage(R.color.divider_color)
                .build();
        draweeView.setHierarchy(hierarchy);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(draweeView.getController())
                .setAutoPlayAnimations(true)
                .setUri(uri)
                .build();
        draweeView.setController(controller);
        draweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAction((ZoomableDraweeView) v);
            }
        });
    }

    private void finishAction(@Nullable ZoomableDraweeView draweeView) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (draweeView != null) {
                draweeView.reset();
            }
            finishAfterTransition();
        } else {
            finish();
        }
    }

}
