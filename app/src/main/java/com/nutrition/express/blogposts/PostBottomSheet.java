package com.nutrition.express.blogposts;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nutrition.express.R;
import com.nutrition.express.util.Utils;

/**
 * Created by huang on 5/30/16.
 */
public class PostBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {
    BottomSheetBehavior behavior;
    private String videoUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        videoUrl = bundle.getString("video_url");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.bottom_sheet_video_list, null);
        TextView textView = (TextView) view.findViewById(R.id.item_link);
        textView.setText(videoUrl);
        textView.setOnClickListener(this);
        view.findViewById(R.id.item_copy).setOnClickListener(this);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());
        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_copy:
                Utils.copy2Clipboard(getActivity(), videoUrl);
                break;
            case R.id.item_link:
                if (!TextUtils.isEmpty(videoUrl)) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)));
                }
                break;
        }
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
