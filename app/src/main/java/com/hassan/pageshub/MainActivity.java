package com.hassan.pageshub;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
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

        // Allow cookies, localStorage, and IndexedDB inside cross-origin GitHub Pages iframes
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);

        // Native bridge for reliable external browser launching, cache clearing, and status bar tinting
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void openBrowser(final String url) {
                if (url != null && !url.isEmpty()) {
                    runOnUiThread(() -> openExternal(Uri.parse(url)));
                }
            }

            @JavascriptInterface
            public void clearCache() {
                runOnUiThread(() -> {
                    web.clearCache(true);
                    CookieManager.getInstance().flush();
                });
            }

            @JavascriptInterface
            public void setStatusBar(final String hexColor) {
                if (hexColor != null && !hexColor.isEmpty()) {
                    runOnUiThread(() -> {
                        try {
                            getWindow().setStatusBarColor(Color.parseColor(hexColor));
                        } catch (Exception ignored) {
                        }
                    });
                }
            }
        }, "Android");

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

    @Override
    protected void onPause() {
        super.onPause();
        // Flush WebView storage and cookies to disk so progress is never lost
        CookieManager.getInstance().flush();
    }

    private void openExternal(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
