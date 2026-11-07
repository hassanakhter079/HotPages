package com.hassan.pageshub;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private static final String HOME = "file:///android_asset/index.html";
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.parseColor("#0b2e2b"));

        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                if (r.isForMainFrame() && !r.getUrl().toString().startsWith("file:///android_asset/")) {
                    openExternal(r.getUrl());
                    return true;
                }
                return false;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView v, boolean dialog, boolean gesture, Message msg) {
                WebView popup = new WebView(MainActivity.this);
                popup.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView v2, WebResourceRequest r) {
                        openExternal(r.getUrl());
                        return true;
                    }

                    @Override
                    public void onPageStarted(WebView v2, String url, Bitmap icon) {
                        if (url != null && !url.equals("about:blank")) {
                            openExternal(Uri.parse(url));
                            v2.stopLoading();
                        }
                    }
                });
                WebView.WebViewTransport t = (WebView.WebViewTransport) msg.obj;
                t.setWebView(popup);
                msg.sendToTarget();
                return true;
            }
        });

        web.loadUrl(HOME);
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.phBack?window.phBack():false", value -> {
            if (!"true".equals(value)) {
                finish();
            }
        });
    }
}