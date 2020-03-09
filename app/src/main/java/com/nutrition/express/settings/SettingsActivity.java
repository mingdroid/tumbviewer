package com.nutrition.express.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.nutrition.express.R;
import com.nutrition.express.common.CommonRVAdapter;
import com.nutrition.express.common.CommonViewHolder;
import com.nutrition.express.login.LoginActivity;
import com.nutrition.express.model.data.DataManager;
import com.nutrition.express.model.data.bean.TumblrAccount;
import com.nutrition.express.model.event.EventRefresh;
import com.nutrition.express.register.RegisterActivity;
import com.nutrition.express.util.FrescoUtils;
import com.nutrition.express.util.PreferencesUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huang on 11/11/16.
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String POST_SIMPLE_MODE = "post_simple_mode";

    private static final int REQUEST_LOGIN = 1;
    private CommonRVAdapter adapter;
    List<Object> accounts;

    private AppCompatCheckBox simpleCheckBox;
    private boolean originalMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.settings_register).setOnClickListener(this);
        findViewById(R.id.settings_clear_cache).setOnClickListener(this);
        findViewById(R.id.settings_add_account).setOnClickListener(this);
        findViewById(R.id.settings_option_simple).setOnClickListener(this);

        originalMode = PreferencesUtils.getBoolean(POST_SIMPLE_MODE, false);
        simpleCheckBox = findViewById(R.id.settings_option_simple_checkbox);
        simpleCheckBox.setChecked(originalMode);
        simpleCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferencesUtils.putBoolean(POST_SIMPLE_MODE, isChecked);
                DataManager.getInstance().setSimpleMode(simpleCheckBox.isChecked());
            }
        });

        List<TumblrAccount> tumblrAccounts = DataManager.getInstance().getTumblrAccounts();
        accounts = new ArrayList<>(tumblrAccounts.size());
        accounts.addAll(tumblrAccounts);

        adapter = CommonRVAdapter.newBuilder()
                .addItemType(TumblrAccount.class, R.layout.item_settings_account,
                        new CommonRVAdapter.CreateViewHolder() {
                            @Override
                            public CommonViewHolder createVH(View view) {
                                return new AccountVH(view);
                            }
                        })
                .setData(accounts)
                .build();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.settings_accounts);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (simpleCheckBox.isChecked() != originalMode) {
            EventBus.getDefault().postSticky(new EventRefresh());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_clear_cache:
                showClearCacheDialog();
                break;
            case R.id.settings_add_account:
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("type", LoginActivity.NEW_ACCOUNT);
                startActivityForResult(loginIntent, REQUEST_LOGIN);
                break;
            case R.id.settings_option_simple:
                simpleCheckBox.setChecked(!simpleCheckBox.isChecked());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
            updateAccountsContent();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_tumblr_limit:
                showTumblrLimitInfo();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAccountsContent() {
        accounts.clear();
        accounts.addAll(DataManager.getInstance().getTumblrAccounts());
        adapter.notifyDataSetChanged();
    }

    private void showDeleteAccountDialog(final TumblrAccount account, String accountName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.delete_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataManager dataManager = DataManager.getInstance();
                dataManager.removeAccount(account);
                updateAccountsContent();
                if (!dataManager.isLogin()) {
                    gotoLogin();
                }
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setTitle(getResources().getString(R.string.settings_delete_account, accountName));
        builder.show();
    }

    private void showClearCacheDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.settings_clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearFrescoDiskCache();
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setTitle(R.string.settings_clear_cache);
        builder.show();
    }

    private void showTumblrLimitInfo() {
        DataManager dataManager = DataManager.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.settings_limit_info,
                dataManager.getDayLimit(),
                dataManager.getDayRemaining(),
                DateUtils.formatElapsedTime(dataManager.getDayReset()),
                dataManager.getHourLimit(),
                dataManager.getHourRemaining(),
                DateUtils.formatElapsedTime(dataManager.getHourReset())
        ));
        builder.show();
    }

    private void gotoLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void clearFrescoDiskCache() {
        Fresco.getImagePipeline().clearDiskCaches();
        Toast.makeText(this, R.string.settings_clear_ok, Toast.LENGTH_SHORT).show();
    }

    private void showRoute() {

    }

    private void switchToAccount(TumblrAccount account) {
        DataManager.getInstance().switchToAccount(account);
        EventBus.getDefault().postSticky(new EventRefresh());
        finish();
    }

    private void showSwitchDialog(final TumblrAccount account, String accountName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.settings_switch, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchToAccount(account);
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setTitle(getResources().getString(R.string.settings_accounts_switch, accountName));
        builder.show();
    }

    /**
     *
     * */
    private class AccountVH extends CommonViewHolder<TumblrAccount>
            implements View.OnClickListener, View.OnLongClickListener {
        private ImageView checkedView;
        private TextView nameView, keyView;
        private SimpleDraweeView avatarView;
        private TumblrAccount account;

        public AccountVH(View itemView) {
            super(itemView);
            checkedView = (ImageView) itemView.findViewById(R.id.account_checked);
            nameView = (TextView) itemView.findViewById(R.id.account_name);
            keyView = (TextView) itemView.findViewById(R.id.account_key);
            avatarView = (SimpleDraweeView) itemView.findViewById(R.id.account_avatar);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void bindView(TumblrAccount account) {
            this.account = account;
            if (account.isUsing()) {
                checkedView.setVisibility(View.VISIBLE);
            } else {
                checkedView.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(account.getName())) {
                nameView.setText(account.getName());
                FrescoUtils.setTumblrAvatarUri(avatarView, account.getName(), 128);
            } else {
                nameView.setText(getResources().getString(R.string.settings_accounts_title,
                        getAdapterPosition() + 1));
                ImageRequest imageRequest = ImageRequestBuilder
                        .newBuilderWithResourceId(R.mipmap.ic_account_default).build();
                avatarView.setImageURI(imageRequest.getSourceUri());
            }
            keyView.setText(account.getApiKey());
        }

        @Override
        public void onClick(View v) {
            if (account.isUsing()) {
                showRoute();
            } else {
                showSwitchDialog(account, nameView.getText().toString());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            showDeleteAccountDialog(account, nameView.getText().toString());
            return true;
        }
    }

}
