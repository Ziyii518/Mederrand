package com.app.mederrand.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.mederrand.R;
import com.app.mederrand.databinding.ActivityWebViewBinding;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

public class WebViewActivity extends AppCompatActivity {
    ActivityWebViewBinding binding;
    Context context;
    String pageTitle, pageUrl;

    OnBackPressedCallback onBackPressedCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = WebViewActivity.this;


        init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(){
        Utilities.setCustomStatusAndNavColor(WebViewActivity.this, R.color.app_color_dark, R.color.app_color_bg);

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        pageTitle = getIntent().getStringExtra( "pageTitle");
        pageUrl = getIntent().getStringExtra( "pageUrl");

        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.textTitle.setText(pageTitle);


        if (URLUtil.isValidUrl(pageUrl)){
            WebSettings webSettings = binding.webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setUseWideViewPort(false);
            webSettings.setGeolocationEnabled(true);
            webSettings.setSupportMultipleWindows(true); // This forces ChromeClient enabled.

            binding.webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    setProgressAnimate(binding.progressBar, progress);
                    if (progress == 100) {
                        setProgressAnimate( binding.progressBar, 100);
                        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() ->  binding.layoutLoader.setVisibility(View.GONE), 700);
                    }
                }
            });

            binding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return false;
                }
            });
            binding.webView.loadUrl(pageUrl);
        }else {
            Toast.makeText(context,"Its not a valid url", Toast.LENGTH_LONG).show();
        }
    }

    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webView.canGoBack()){
                    binding.webView.goBack();
                }else {
                    finish();
                    overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
                }
            }
        };
    }

    private void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }
}