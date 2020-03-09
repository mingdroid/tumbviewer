package com.nutrition.express.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nutrition.express.R;
import com.nutrition.express.application.Constants;
import com.nutrition.express.main.v2.Main2Activity;
import com.nutrition.express.model.data.DataManager;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    public static final int NORMAL = 0;
    public static final int NEW_ACCOUNT = 1;   //using default tumblr app
    public static final int NEW_ROUTE = 2;    //using user's tumblr app
    public static final int ROUTE_SWITCH = 3; //using default tumblr app

    private int type = NORMAL; //login type;

    private ProgressDialog progressDialog;

    private WebView webView;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", NORMAL);
        }
        DataManager dataManager = DataManager.getInstance();
        if ((type == NORMAL) && dataManager.isLogin()) {
            gotoMainActivity();
            return;
        }
        if (type == NEW_ROUTE || type == ROUTE_SWITCH) {
            //if there are two different accounts, then clear cookies;
            if (dataManager.getAccountCount() > 1) {
                dataManager.clearCookies();
            }
        } else if (type == NEW_ACCOUNT) {
            dataManager.clearCookies();
        }

        getWindow().setBackgroundDrawableResource(android.R.color.white);
        setContentView(R.layout.activity_web);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        setTitle("Login");
        if ((type == NEW_ACCOUNT) && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("webView url", url);
                if (url.startsWith(Constants.REDIRECT_URI)) {
                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                    if (!TextUtils.isEmpty(oauthVerifier)) {
                        loginPresenter.getAccessToken(oauthVerifier);
                        return true;
                    }
                    return false;
                }
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
        loginPresenter = new LoginPresenter(this, type);
        loginPresenter.getRequestToken();
        progressDialog = ProgressDialog.show(this, null, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginPresenter != null) {
            loginPresenter.onDetach();
        }
    }

    @Override
    public void loadUrl(String url) {
        webView.loadUrl(url);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void showLoginSuccess() {
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_LONG).show();
        if (type == NEW_ACCOUNT) {
            setResult(RESULT_OK);
            finish();
        } else {
            gotoMainActivity();
        }
    }

    @Override
    public void onError(int code, String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        //// TODO: 10/18/16 show a failure view
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
        finish();
    }

}
