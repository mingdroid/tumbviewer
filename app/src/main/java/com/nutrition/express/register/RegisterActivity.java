package com.nutrition.express.register;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.nutrition.express.R;
import com.nutrition.express.login.LoginActivity;
import com.nutrition.express.model.data.DataManager;

/**
 * Created by huang on 11/22/16.
 */

public class RegisterActivity extends AppCompatActivity {
    private String key, secret;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        setTitle("Register");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (DataManager.getInstance().getAccountCount() > 1) {
            DataManager.getInstance().clearCookies();
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
                }
            }
        });

        webView.loadUrl("https://www.tumblr.com/oauth/apps");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tumblr_app:
                showSettingAppDialog();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showSettingAppDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.item_register_tumblr, null);
        final AppCompatEditText keyEditText = (AppCompatEditText) view.findViewById(R.id.api_key);
        final AppCompatEditText secretEditText = (AppCompatEditText) view.findViewById(R.id.api_secret);
        keyEditText.setText(key);
        secretEditText.setText(secret);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.register_tumblr_app);
        builder.setPositiveButton(R.string.pic_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (keyEditText.getText().length() > 0 && secretEditText.getText().length() > 0) {
                    DataManager.getInstance().saveTumblrApp(keyEditText.getText().toString(),
                            secretEditText.getText().toString());
                    Toast.makeText(RegisterActivity.this, R.string.pic_saved, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("type", LoginActivity.NEW_ROUTE);
                    startActivity(intent);
                    finish();
                }
            }
        });
        builder.setNeutralButton(R.string.register_continue_copy, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                key = keyEditText.getText().toString();
                secret = secretEditText.getText().toString();
            }
        });
        builder.setNegativeButton(R.string.pic_cancel, null);
        builder.setView(view);
        builder.show();
    }

}
