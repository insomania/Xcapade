//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.metaio.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Environment;
import android.os.Build.VERSION;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.Layout.Alignment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebStorage.QuotaUpdater;

import com.metaio.R;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.MetaioWorldPOIManagerCallback;
import com.metaio.sdk.TextToSpeechHelper;
import com.metaio.sdk.jni.ARELInterpreterAndroid;
import com.metaio.sdk.jni.EAREL_MEDIA_EVENT;
import com.metaio.sdk.jni.ECOLOR_FORMAT;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.EPOI_PREDOMINANT_COLOR;
import com.metaio.sdk.jni.IARELInterpreterCallback;
import com.metaio.sdk.jni.IARELObject;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDK;
import com.metaio.sdk.jni.IMetaioWorldPOIManager;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.ObjectParameter;
import com.metaio.sdk.jni.PathOrURL;
import com.metaio.sdk.jni.Vector2di;
import com.metaio.tools.SystemInfo;
import com.metaio.tools.io.AssetsManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(9)
public class ARELInterpreterAndroidJava2 extends ARELInterpreterAndroid implements OnCompletionListener, OnPreparedListener, OnErrorListener, OnInfoListener {
    protected WebView mWebView = null;
    protected Activity mActivity = null;
    private Bitmap mAnnotationBackground = null;
    private int mAnnotationBackgroundIndex = -1;
    private Bitmap mEmptyStarImage = null;
    private Bitmap mFullStarImage = null;
    private String mWebviewOpened = "";
    private String mFullscreenVideoOpened = "";
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Lock mLockMediaPlayer = new ReentrantLock();
    private String mMediaPlayerDataSource = null;
    private boolean mMediaPlayerLoaded = false;
    private TextPaint mPaint = new TextPaint();
    private TextToSpeechHelper mTextToSpeechHelper;
    private String mVideoPath = "";
    private MetaioWorldPOIManagerCallback mPOIManagerCallback;
    public boolean arelEnabled;

    public ARELInterpreterAndroidJava2() {
        this.mPaint.setFilterBitmap(true);
        this.mPaint.setAntiAlias(true);
        this.mMediaPlayer.setOnCompletionListener(this);
        this.mMediaPlayer.setOnPreparedListener(this);
        this.mMediaPlayer.setOnErrorListener(this);
        this.mMediaPlayer.setOnInfoListener(this);
    }

    public synchronized void release() {
        if(this.mWebView != null) {
            this.mWebView.setWebChromeClient((WebChromeClient)null);
            this.mWebView.setWebViewClient((WebViewClient)null);
            this.mWebView = null;
        }

        this.mActivity = null;
        this.mFullscreenVideoOpened = "";
        this.mWebviewOpened = "";
        if(this.mAnnotationBackground != null) {
            this.mAnnotationBackground.recycle();
            this.mAnnotationBackground = null;
        }

        if(this.mEmptyStarImage != null) {
            this.mEmptyStarImage.recycle();
            this.mEmptyStarImage = null;
        }

        if(this.mFullStarImage != null) {
            this.mFullStarImage.recycle();
            this.mFullStarImage = null;
        }

        this.mLockMediaPlayer.lock();
        this.mMediaPlayerDataSource = null;
        this.mMediaPlayerLoaded = false;
        if(this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            this.mMediaPlayerDataSource = null;
            this.mMediaPlayerLoaded = false;
        }

        this.mLockMediaPlayer.unlock();
        if(!this.mVideoPath.isEmpty()) {
            File file = new File(this.mVideoPath);
            boolean deleted = file.delete();
            this.mVideoPath = "";
            MetaioDebug.logPrivate(3, "ARELInterpreterAndroid.onDestroy: the video file has been deleted: " + deleted);
        }

        if(this.mTextToSpeechHelper != null) {
            this.mTextToSpeechHelper.shutdown();
            this.mTextToSpeechHelper = null;
        }

    }

    public synchronized void setMetaioWorldPOIManagerCallback(MetaioWorldPOIManagerCallback listener) {
        this.mPOIManagerCallback = listener;
    }

    @SuppressLint({"SetJavaScriptEnabled"})
    @TargetApi(16)
    public synchronized void initWebView(WebView webview, Activity activity) {
        this.mActivity = activity;
        if(webview != this.mWebView) {
            this.mWebView = webview;
            if(VERSION.SDK_INT >= 11) {
                try {
                    this.mWebView.setLayerType(1, (Paint)null);
                } catch (Exception var6) {
                    ;
                }
            }

            this.mWebView.setBackgroundColor(0);
            this.mWebView.setScrollBarStyle(33554432);
            this.mWebView.setVerticalScrollBarEnabled(false);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            WebSettings settings = this.mWebView.getSettings();
            settings.setPluginState(PluginState.ON);
            settings.setJavaScriptEnabled(true);
            settings.setGeolocationEnabled(true);
            settings.setLightTouchEnabled(true);
            settings.setSupportZoom(false);
            settings.setAppCacheEnabled(true);
            settings.setDatabaseEnabled(true);
            String databasePath = this.mWebView.getContext().getDir("database", 0).getPath();
            MetaioDebug.log("Local storage database: " + databasePath);
            settings.setDatabasePath(databasePath);
            settings.setDomStorageEnabled(true);
            settings.setGeolocationDatabasePath(databasePath);
            if(VERSION.SDK_INT >= 16) {
                settings.setAllowUniversalAccessFromFileURLs(true);
            }

            this.arelEnabled = true;
            webview.setWebChromeClient(new ARELInterpreterAndroidJava2.ARELWebChromeClient());
            webview.setWebViewClient(new ARELInterpreterAndroidJava2.ARELInterpreterAndroidJavaClient(activity));
            webview.addJavascriptInterface(new ARELInterpreterAndroidJava2.ARELJavascriptInterface(), "Android");
            webview.loadUrl("about:blank");
        }
    }

    protected void onSurfaceChanged(int width, int height) {
        if(this.mWebviewOpened.length() > 0) {
            this.callMediaEvent(this.mWebviewOpened, EAREL_MEDIA_EVENT.EAME_WEBSITE_CLOSED);
            this.mWebviewOpened = "";
        } else if(this.mFullscreenVideoOpened.length() > 0) {
            this.callMediaEvent(this.mFullscreenVideoOpened, EAREL_MEDIA_EVENT.EAME_VIDEO_CLOSED);
            this.mFullscreenVideoOpened = "";
        }

    }

    public void onResume() {
        this.mWebView.resumeTimers();
        this.mLockMediaPlayer.lock();

        try {
            if(this.mMediaPlayerLoaded) {
                this.mMediaPlayer.start();
            }
        } catch (IllegalStateException var2) {
            MetaioDebug.log(6, "ARELInterpreterAndroid.onResume: " + var2.getMessage());
        }

        this.mLockMediaPlayer.unlock();
    }

    public void onPause() {
        this.mWebView.pauseTimers();
        this.mLockMediaPlayer.lock();

        try {
            if(this.mMediaPlayerLoaded) {
                this.mMediaPlayer.pause();
            }
        } catch (IllegalStateException var2) {
            MetaioDebug.log(6, "ARELInterpreterAndroid.onPause: " + var2.getMessage());
        }

        this.mLockMediaPlayer.unlock();
    }

    public synchronized void loadARELWebPage(final PathOrURL arelWebPage) {
        final File basePath = this.getBasePath();
        this.mActivity.runOnUiThread(new Runnable() {
            public void run() {
                MetaioDebug.log("ARELInterpreterAndroidJava2.loadARELWebPage " + arelWebPage.asStringForLogging());
                if(arelWebPage.isPath()) {
                    File isAbsoluteURI = arelWebPage.asPath();
                    if(isAbsoluteURI.isAbsolute()) {
                        ARELInterpreterAndroidJava2.this.mWebView.loadUrl("file://" + isAbsoluteURI.getAbsolutePath());
                    } else {
                        ARELInterpreterAndroidJava2.this.mWebView.loadUrl("file://" + (new File(basePath, isAbsoluteURI.getPath())).getAbsolutePath());
                    }
                } else {
                    boolean isAbsoluteURI1 = false;

                    try {
                        URI uri = new URI(arelWebPage.asURL());
                        isAbsoluteURI1 = uri.isAbsolute();
                    } catch (URISyntaxException var3) {
                        ;
                    }

                    if(isAbsoluteURI1) {
                        ARELInterpreterAndroidJava2.this.mWebView.loadUrl(arelWebPage.asURL());
                    } else {
                        ARELInterpreterAndroidJava2.this.mWebView.loadUrl("file://" + (new File(basePath, arelWebPage.asURL())).getAbsolutePath());
                    }
                }

            }
        });
    }

    protected synchronized void playVideo(final String videoID, final PathOrURL videoAsset) {
        MetaioDebug.log("ARELInterpreterAndroidJava2.Playing Video: " + videoAsset.asStringForLogging());
//        if(this.mPOIManagerCallback != null) {
//            this.mPOIManagerCallback.playVideo(videoID, videoAsset);
//        } else {
            IARELInterpreterCallback callback = this.getCallback();
            boolean handledByTheCallback = false;
            if(callback != null) {
                handledByTheCallback = callback.playVideo(videoAsset);
            }

            if(!handledByTheCallback) {
                final boolean isValidURL = videoAsset.isURL() && URLUtil.isValidUrl(videoAsset.asURL());
                File file = videoAsset.isPath()?videoAsset.asPath():null;
                if(!videoAsset.empty() && (isValidURL || file != null && file.exists())) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                if(videoAsset.isPath() && videoAsset.asPath().getPath().startsWith(Environment.getDataDirectory().getAbsolutePath())) {
                                    String e = videoAsset.asPath().getPath();
                                    AssetManager assetManager = ARELInterpreterAndroidJava2.this.mActivity.getApplicationContext().getAssets();
                                    String filename = e.substring(e.lastIndexOf(47) + 1);
                                    InputStream in = null;
                                    FileOutputStream out = null;
                                    File e1;
                                    if(ARELInterpreterAndroidJava2.this.mVideoPath != null) {
                                        e1 = new File(ARELInterpreterAndroidJava2.this.mVideoPath);
                                        e1.delete();
                                    }

                                    try {
                                        in = assetManager.open(e.replace(AssetsManager.getAbsolutePath() + "/", ""));
                                        ARELInterpreterAndroidJava2.this.mVideoPath = ARELInterpreterAndroidJava2.this.mActivity.getApplicationContext().getExternalFilesDir((String)null) + "/" + filename;
                                        e1 = new File(ARELInterpreterAndroidJava2.this.mActivity.getApplicationContext().getExternalFilesDir((String)null), filename);
                                        out = new FileOutputStream(e1);
                                        byte[] buffer = new byte[1024];

                                        while(true) {
                                            int read;
                                            if((read = in.read(buffer)) == -1) {
                                                in.close();
                                                in = null;
                                                out.flush();
                                                out.close();
                                                out = null;
                                                break;
                                            }

                                            out.write(buffer, 0, read);
                                        }
                                    } catch (IOException var9) {
                                        Log.e("tag", "Failed to copy asset file: " + filename, var9);
                                    }
                                }

                                Intent e2 = new Intent("android.intent.action.VIEW");
                                if(ARELInterpreterAndroidJava2.this.mVideoPath.isEmpty()) {
                                    if(isValidURL) {
                                        e2.setDataAndType(Uri.parse(videoAsset.asURL()), "video/*");
                                    } else {
                                        e2.setDataAndType(Uri.fromFile(videoAsset.asPath()), "video/*");
                                    }
                                } else {
                                    e2.setDataAndType(Uri.fromFile(new File(ARELInterpreterAndroidJava2.this.mVideoPath)), "video/*");
                                }

                                MetaioDebug.logPrivate(3, "Launching default media player... ");
                                ARELInterpreterAndroidJava2.this.mActivity.startActivity(e2);
                                ARELInterpreterAndroidJava2.this.mFullscreenVideoOpened = videoID;
                            } catch (Exception var10) {
                                MetaioDebug.log(6, "Error starting external media player, probably invalid URL/path: " + videoAsset.asStringForLogging());
                                MetaioDebug.printStackTrace(6, var10);
                            }

                        }
                    };
                    this.mActivity.runOnUiThread(runnable);
//                } else {
//                    MetaioDebug.log(6, "Cannot play video from the location specified. You can stream videos via http or can place it in the assets folder of the application or on the external storage (AREL configuration xml file has to be located on the external storage in this case)");
//                }
            }
        }
    }

    public void onCompletion(MediaPlayer mp) {
        MetaioDebug.log("Audio track completed");
        this.mLockMediaPlayer.lock();
        MetaioDebug.log("mMediaPlayerDataSource:" + this.mMediaPlayerDataSource);
        if(this.mMediaPlayerDataSource != null) {
            this.callMediaEvent(this.mMediaPlayerDataSource, EAREL_MEDIA_EVENT.EAME_SOUND_COMPLETE);
        }

        this.mMediaPlayerLoaded = false;
        this.mMediaPlayerDataSource = null;
        this.mMediaPlayer.reset();
        this.mLockMediaPlayer.unlock();
    }

    public void onPrepared(MediaPlayer mp) {
        MetaioDebug.log("Audio track loaded, now playing...");
        this.mLockMediaPlayer.lock();
        this.mMediaPlayerLoaded = true;
        this.mMediaPlayer.start();
        this.mLockMediaPlayer.unlock();
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        MetaioDebug.log(6, "MediaPlayer.onError: " + what + ", " + extra);
        return false;
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        MetaioDebug.log(4, "MediaPlayer.onInfo: " + what + ", " + extra);
        return false;
    }

    protected synchronized void playSound(String soundID, PathOrURL soundAsset, File localPathToFile) {
//        if(this.mPOIManagerCallback != null) {
//            this.mPOIManagerCallback.playSound(soundID, soundAsset, localPathToFile);
//        } else {
            MetaioDebug.logPrivate(3, "soundId: " + soundID);
            MetaioDebug.logPrivate(3, "soundUrl: " + soundAsset.asStringForLogging());
            MetaioDebug.logPrivate(3, "localPathToFile: " + localPathToFile.getPath());
            if(soundAsset.empty()) {
                MetaioDebug.log(6, "Empty sound path/URL for sound ID: " + soundID);
            } else {
                String dataSource = "";
                boolean isAssetFile = true;
                String e;
                if(soundAsset.isURL()) {
                    e = soundAsset.asURL();
                    if(e.startsWith("http") || e.startsWith("www.") || e.startsWith("sdcard/")) {
                        dataSource = e;
                        isAssetFile = false;
                    }
                } else if(soundAsset.isPath()) {
                    File e1 = soundAsset.asPath();
                    if(e1.exists()) {
                        dataSource = e1.getPath();
                        isAssetFile = false;
                    }
                }

                if(dataSource.isEmpty()) {
                    e = AssetsManager.getAbsolutePath();
                    MetaioDebug.logPrivate(3, "assetsUrl: " + e);
                    String relativeUrl = localPathToFile.getPath().substring(e.length() + 1);
                    MetaioDebug.logPrivate(3, "relativeUrl: " + relativeUrl);
                    dataSource = relativeUrl;
                }

                MetaioDebug.logPrivate(3, "Playing Audio: " + dataSource);
                this.mLockMediaPlayer.lock();

                try {
                    if(!dataSource.isEmpty() && dataSource.equalsIgnoreCase(this.mMediaPlayerDataSource)) {
                        if(this.mMediaPlayerLoaded) {
                            MetaioDebug.logPrivate(3, "Resuming MediaPlayer, already loaded with: " + dataSource);
                            this.mMediaPlayer.start();
                        } else {
                            MetaioDebug.log("MediaPlayer loading in progress: " + dataSource);
                        }
                    } else {
                        this.mMediaPlayer.reset();
                        if(!dataSource.startsWith("http") && !dataSource.startsWith("www.")) {
                            try {
                                if(isAssetFile) {
                                    MetaioDebug.logPrivate(3, "Playing asset file " + dataSource);
                                    AssetFileDescriptor e2 = this.mActivity.getApplicationContext().getAssets().openFd(dataSource);

                                    try {
                                        this.mMediaPlayer.setDataSource(e2.getFileDescriptor(), e2.getStartOffset(), e2.getLength());
                                    } finally {
                                        e2.close();
                                    }
                                } else {
                                    MetaioDebug.logPrivate(3, "Playing local file " + dataSource);
                                    FileInputStream e3 = new FileInputStream(new File(dataSource));
                                    this.mMediaPlayer.setDataSource(e3.getFD());
                                    e3.close();
                                }
                            } catch (IllegalArgumentException var22) {
                                MetaioDebug.printStackTrace(6, var22);
                            } catch (IllegalStateException var23) {
                                MetaioDebug.printStackTrace(6, var23);
                            } catch (IOException var24) {
                                MetaioDebug.printStackTrace(6, var24);
                            }
                        } else {
                            this.mMediaPlayer.setDataSource(dataSource);
                            MetaioDebug.logPrivate(3, "Playing http file " + dataSource);
                        }

                        this.mMediaPlayer.prepareAsync();
                        this.mMediaPlayerDataSource = dataSource;
                    }
                } catch (Exception var25) {
                    MetaioDebug.log(6, "Failed to start audio: " + soundID);
                    MetaioDebug.printStackTrace(6, var25);
                } finally {
                    this.mLockMediaPlayer.unlock();
                }

            }
//        }
    }

    protected synchronized void stopSound(String soundID, PathOrURL soundAsset, File localPathToFile, boolean pause) {
//        if(this.mPOIManagerCallback != null) {
//            this.mPOIManagerCallback.stopSound(soundID, soundAsset, localPathToFile, pause);
//        } else {
            this.mLockMediaPlayer.lock();

            try {
                if(this.mMediaPlayerLoaded && this.mMediaPlayer != null) {
                    if(pause) {
                        this.mMediaPlayer.pause();
                    } else {
                        this.mMediaPlayer.stop();
                        this.mMediaPlayerDataSource = null;
                        this.mMediaPlayerLoaded = false;
                    }
                }
            } catch (Exception var6) {
                MetaioDebug.log(6, "Failed to stop audio: " + var6.getMessage());
            }

            this.mLockMediaPlayer.unlock();
//        }
    }

    protected synchronized void textToSpeech(String text) {
        if(this.mTextToSpeechHelper == null) {
            this.mTextToSpeechHelper = new TextToSpeechHelper(this.mActivity);
        }

        this.mTextToSpeechHelper.speak(text);
    }

    protected synchronized void openWebsite(String websiteID, String url) {
//        if(this.mPOIManagerCallback != null) {
//            this.mPOIManagerCallback.openWebsite(websiteID, url);
//        } else {
            this.openWebsite(websiteID, url, false);
//        }
    }

    protected synchronized void openWebsite(String websiteID, String url, boolean openInExternalApp) {
//        if(this.mPOIManagerCallback != null) {
//            this.mPOIManagerCallback.openWebsite(websiteID, url, openInExternalApp);
//        } else {
            IARELInterpreterCallback callback = this.getCallback();
            boolean handledByTheCallback = false;
            if(callback != null) {
                handledByTheCallback = callback.openWebsite(url, openInExternalApp);
            }

            if(!handledByTheCallback) {
                if(!this.openURL(this.mActivity.getApplicationContext(), url)) {
                    MetaioDebug.log(6, "Error starting browser, probably invalid URL");
                } else {
                    this.mWebviewOpened = url;
                }

            }
//        }
    }

    protected synchronized boolean openURL(Context context, String url) {
//        try {
//            Intent e = null;
//            MetaioDebug.log("Opening URL: " + url);
//            e = MetaioCloudPlugin.getDefaultIntent(url);
//            if(e == null) {
//                e = new Intent("android.intent.action.VIEW", Uri.parse(url));
//            }
//
//            e.setFlags(268435456);
//            context.startActivity(e);
//            return true;
//        } catch (Exception var4) {
//            MetaioDebug.log(6, "Error starting web view activity: " + var4.getMessage());
            return false;
//        }
    }

    protected synchronized void executeJavaScript(final String scriptToExecute) {
        MetaioDebug.log(3, "ARELInterpreterAndroid.executeJavaScript: " + scriptToExecute);
        if(scriptToExecute == null) {
            MetaioDebug.log(6, "Error in JavaScript: " + scriptToExecute);
        } else if(this.mActivity != null && this.mWebView != null) {
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    if(ARELInterpreterAndroidJava2.this.mWebView != null) {
                        ARELInterpreterAndroidJava2.this.mWebView.loadUrl("javascript:(function anon_ARELInterpreterAndroid(){" + scriptToExecute + "})();");
                    } else {
                        MetaioDebug.log(6, "WebView instance doesn\'t exist anymore");
                    }

                }
            });
        }
    }

    protected IGeometry loadPOIAnnotationForAnnotatedGeometriesGroup(IARELObject poi, File thumbnailImagePath, IMetaioSDK sdk) {
        Bitmap[] inOutCachedBitmaps = new Bitmap[]{this.mAnnotationBackground, this.mEmptyStarImage, this.mFullStarImage};
        int[] inOutCachedAnnotationBackgroundIndex = new int[]{this.mAnnotationBackgroundIndex};
        IGeometry ret = getPOIAnnotationForAnnotatedGeometriesGroup(poi, thumbnailImagePath, sdk, this.getPOIManager(), this.mActivity, (Lock)null, this.mPaint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex);
        this.mAnnotationBackground = inOutCachedBitmaps[0];
        this.mEmptyStarImage = inOutCachedBitmaps[1];
        this.mFullStarImage = inOutCachedBitmaps[2];
        this.mAnnotationBackgroundIndex = inOutCachedAnnotationBackgroundIndex[0];
        return ret;
    }

    static IGeometry getPOIAnnotationForAnnotatedGeometriesGroup(IARELObject poi, File thumbnailImagePath, IMetaioSDK sdk, IMetaioWorldPOIManager poiManager, Activity activity, Lock geometryLock, TextPaint paint, Bitmap[] inOutCachedBitmaps, int[] inOutCachedAnnotationBackgroundIndex) {
        Bitmap thumbnail = null;
        boolean shouldRecycleThumbnail = false;
        Bitmap attribution = null;

        IGeometry var18 = null;
//        try {
//            if(thumbnailImagePath != null) {
//                thumbnail = BitmapFactory.decodeFile(thumbnailImagePath.getPath());
//                shouldRecycleThumbnail = true;
//            }
//
//            String url;
//            if(thumbnail == null) {
//                url = poi.getParameter(ObjectParameter.ObjectParameterIconURI);
//                if(MetaioCloudPlugin.getRemoteAssetsManager(activity.getApplicationContext()).downloadImage(new AssetDownloader(activity, url, poiManager), url, (String)null)) {
//                    thumbnail = MetaioCloudPlugin.getRemoteAssetsManager(activity.getApplicationContext()).getImage(url);
//                }
//            }
//
//            url = poi.getARELParameter("poi-attribution-image");
//            if(!TextUtils.isEmpty(url) && MetaioCloudPlugin.getRemoteAssetsManager(activity.getApplicationContext()).downloadImage(new AssetDownloader(activity, url, poiManager), url, (String)null)) {
//                attribution = MetaioCloudPlugin.getRemoteAssetsManager(activity.getApplicationContext()).getImage(url);
//            }
//
//            String[] textureHash = new String[1];
//
//            ImageStruct texture;
//            try {
//                texture = getAnnotationImageForPOI(poi, thumbnail, attribution, sdk.getRenderSize(), activity, paint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, textureHash);
//            } catch (Exception var25) {
//                MetaioCloudPlugin.log(6, "Error creating annotation texture!");
//                MetaioDebug.printStackTrace(6, var25);
//                texture = null;
//            }
//
//            IGeometry geometry = null;
//            if(texture != null) {
//                if(geometryLock != null) {
//                    geometryLock.lock();
//                }
//
//                try {
//                    geometry = sdk.createGeometryFromImage(textureHash[0], texture, true, false);
//                } finally {
//                    if(geometryLock != null) {
//                        geometryLock.unlock();
//                    }
//
//                }
//            }
//
//            var18 = geometry;
//        } finally {
            if(shouldRecycleThumbnail && thumbnail != null) {
                thumbnail.recycle();
                thumbnail = null;
            }

//        }

        return var18;
    }

    private static Rect makeRectWH(int x, int y, int width, int height) {
        return new Rect(x, y, x + width, y + height);
    }

    private static ImageStruct getAnnotationImageForPOI(IARELObject poi, Bitmap thumbnail, Bitmap attribution, Vector2di viewportSize, Activity activity, TextPaint paint, Bitmap[] inOutCachedBitmaps, int[] inOutCachedAnnotationBackgroundIndex, String[] outTextureHash) throws Exception {
        return getAnnotationImageForPOI(poi.getID(), poi.getTitle(), poi.getCurrentDistance(), poi.getARELParameter("poi-rating"), thumbnail, attribution, viewportSize, activity, paint, inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, outTextureHash);
    }

    public static ImageStruct getAnnotationImageForPOI(String poiID, String poiTitle, float poiCurrentDistance, String poiRating, Bitmap thumbnail, Bitmap attribution, Vector2di viewportSize, Activity activity, TextPaint paint, Bitmap[] inOutCachedBitmaps, int[] inOutCachedAnnotationBackgroundIndex, String[] outTextureHash) throws Exception {
        float distanceInM = poiCurrentDistance;
//        String distance = poiCurrentDistance > 0.0F?MetaioCloudUtils.getRelativeLocationString(poiCurrentDistance, 0.0F, false, Settings.useImperialUnits):"0";
        String distance = Float.toString(distanceInM);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        float widthInches = (float)viewportSize.getX() / displayMetrics.xdpi;
        float heightInches = (float)viewportSize.getY() / displayMetrics.ydpi;
        boolean isTablet = Math.max(widthInches, heightInches) >= 14.0F;
        float xdpi = displayMetrics.xdpi;
        if(SystemInfo.isWearableDevice()) {
            xdpi *= 0.7F;
        }

        int[] backgroundFiles = new int[]{0, 186, 64, 6, 9, 10, 0, 272, 94, 10, 13, 15, 0, 382, 132, 15, 21, 21, 0, 508, 176, 20, 28, 28, 0, 612, 212, 24, 33, 34, 0, 712, 246, 28, 39, 39, 1, 276, 82, 10, 13, 14, 1, 384, 114, 13, 18, 19, 1, 512, 152, 18, 24, 26, 1, 612, 182, 22, 29, 31, 1, 714, 212, 25, 33, 36};

        assert backgroundFiles.length % 6 == 0;

        int chosenBackgroundIndex = -1;
        int limit = Math.min(viewportSize.getX() * 3 / 4, (int)(xdpi * (isTablet?5.5F:4.0F) / 2.54F));

        int backgroundWidth;
        for(backgroundWidth = 0; backgroundWidth < backgroundFiles.length / 6; ++backgroundWidth) {
            if(backgroundFiles[backgroundWidth * 6] != 0 == isTablet) {
                if(chosenBackgroundIndex == -1) {
                    chosenBackgroundIndex = backgroundWidth;
                } else {
                    if(backgroundFiles[backgroundWidth * 6 + 1] > limit) {
                        break;
                    }

                    chosenBackgroundIndex = backgroundWidth;
                }
            }
        }

        outTextureHash[0] = poiID + "-" + chosenBackgroundIndex + "-" + distance.hashCode();
        backgroundWidth = backgroundFiles[chosenBackgroundIndex * 6 + 1];
        int backgroundHeight = backgroundFiles[chosenBackgroundIndex * 6 + 2];
        int starWidth = backgroundFiles[chosenBackgroundIndex * 6 + 5];
        float scale = (float)backgroundHeight / 152.0F * 4.0F;
        int borderL = backgroundFiles[chosenBackgroundIndex * 6 + 3];
        int borderT = borderL;
        int borderR = backgroundFiles[chosenBackgroundIndex * 6 + 4];
        int borderB = borderR;
        borderL = (int)((double)borderL + Math.floor((double)scale * 2.5D));
        String billboard;
        String starImageEmpty;
        if(inOutCachedBitmaps[0] == null || inOutCachedAnnotationBackgroundIndex[0] != chosenBackgroundIndex) {
            billboard = "NewPOI--" + (isTablet?"t":"p") + "-" + backgroundWidth + "px.png";
            starImageEmpty = AssetsManager.getAssetPath(activity, "junaio/" + billboard);
            if(starImageEmpty == null) {
                throw new Exception("Annotation background does not exist (asset: junaio/" + billboard + ")");
            }

            if(inOutCachedBitmaps[0] != null) {
                inOutCachedBitmaps[0].recycle();
            }

            inOutCachedBitmaps[0] = BitmapFactory.decodeFile(starImageEmpty);
            if(inOutCachedBitmaps[0] == null) {
                throw new Exception("Annotation background could not be loaded (asset: junaio/" + billboard + ")");
            }

            inOutCachedAnnotationBackgroundIndex[0] = chosenBackgroundIndex;
        }

        Bitmap var58 = inOutCachedBitmaps[0].copy(Config.ARGB_8888, true);
        Bitmap var59 = null;
        Bitmap starImageFull = null;

        try {
            Canvas c = new Canvas(var58);
            int textRight = var58.getWidth() - borderR;
            int distanceTextRight = textRight;
            float desiredThumbnailSize = (float)(var58.getHeight() - borderT - borderB);
            int attributionImageSize;
            int textLeft;
            int distanceTextLeft;
            if(thumbnail != null) {
                int rating = thumbnail.getWidth();
                attributionImageSize = thumbnail.getHeight();
                if(rating > attributionImageSize) {
                    textLeft = (int)desiredThumbnailSize;
                    distanceTextLeft = (int)(desiredThumbnailSize * (float)attributionImageSize / (float)rating);
                } else if(rating < attributionImageSize) {
                    textLeft = (int)(desiredThumbnailSize * (float)rating / (float)attributionImageSize);
                    distanceTextLeft = (int)desiredThumbnailSize;
                } else {
                    textLeft = (int)desiredThumbnailSize;
                    distanceTextLeft = (int)desiredThumbnailSize;
                }

                textRight = (int)((float)textRight - (1.25F * scale + (float)textLeft));
                distanceTextRight = (int)((float)distanceTextRight - (2.0F * scale + (float)textLeft));
                Rect textToDraw = makeRectWH(var58.getWidth() - borderR - textLeft, (int)((float)(borderT + (var58.getHeight() - borderB)) / 2.0F - (float)distanceTextLeft / 2.0F), textLeft, distanceTextLeft);
                c.drawBitmap(thumbnail, (Rect)null, textToDraw, paint);
            }

            float var60 = -1.0F;
            if(poiRating != null) {
                try {
                    var60 = Float.parseFloat(poiRating);
                } catch (NumberFormatException var56) {
                    ;
                }
            }

            if(var60 != -1.0F) {
                attributionImageSize = Math.max(0, Math.min(5, Math.round(var60)));
                String var61;
                String var63;
                if(attributionImageSize < 5) {
                    if(inOutCachedBitmaps[1] == null) {
                        var61 = "NewPOI_star_empty--" + starWidth + "px.png";
                        var63 = AssetsManager.getAssetPath(activity, "junaio/" + var61);
                        if(var63 == null) {
                            throw new Exception("Image does not exist (asset: junaio/" + var61 + ")");
                        }

                        if(inOutCachedBitmaps[1] != null) {
                            inOutCachedBitmaps[1].recycle();
                        }

                        inOutCachedBitmaps[1] = BitmapFactory.decodeFile(var63);
                        if(inOutCachedBitmaps[1] == null) {
                            throw new Exception("Image could not be loaded (asset: junaio/" + var61 + ")");
                        }
                    }

                    var59 = inOutCachedBitmaps[1].copy(Config.ARGB_8888, true);
                }

                if(attributionImageSize > 0) {
                    if(inOutCachedBitmaps[2] == null) {
                        var61 = "NewPOI_star_full--" + starWidth + "px.png";
                        var63 = AssetsManager.getAssetPath(activity, "junaio/" + var61);
                        if(var63 == null) {
                            throw new Exception("Image does not exist (asset: junaio/" + var61 + ")");
                        }

                        if(inOutCachedBitmaps[2] != null) {
                            inOutCachedBitmaps[2].recycle();
                        }

                        inOutCachedBitmaps[2] = BitmapFactory.decodeFile(var63);
                        if(inOutCachedBitmaps[2] == null) {
                            throw new Exception("Image could not be loaded (asset: junaio/" + var61 + ")");
                        }
                    }

                    starImageFull = inOutCachedBitmaps[2].copy(Config.ARGB_8888, true);
                }

                assert var59 != null || starImageFull != null;

                assert var59 == null || starImageFull == null || var59.getWidth() == starImageFull.getWidth() && var59.getHeight() == starImageFull.getHeight();

                float var62 = (float)borderL;

                for(distanceTextLeft = 0; distanceTextLeft < 5; ++distanceTextLeft) {
                    Bitmap var64 = distanceTextLeft <= attributionImageSize - 1?starImageFull:var59;
                    Rect titleWidth = makeRectWH((int)var62, (int)((float)(var58.getHeight() - borderB - var64.getHeight()) - 0.75F * scale), var64.getWidth(), var64.getHeight());
                    c.drawBitmap(var64, (Rect)null, titleWidth, paint);
                    var62 += (float)titleWidth.width() + 1.0F * scale;
                }
            }

            attributionImageSize = (int)(8.0F * scale);
            if(attribution != null) {
                Rect var65 = makeRectWH(borderL, (int)((double)borderT + Math.floor((double)(0.75F * scale))), attributionImageSize, attributionImageSize);
                c.drawBitmap(attribution, (Rect)null, var65, paint);
            }

            textLeft = (int)((float)borderL + (attribution != null?(float)attributionImageSize + 3.0F * scale:0.0F));
            int var66 = textRight - textLeft;
            int titleHeight = var58.getHeight() - borderT - borderB;
            int fontYCorrection = (int)(0.5F * scale);
            if(distanceInM > 0.0F) {
                float titleFontSize = (float)(distanceTextRight - textLeft);
                float titleRect = (float)Math.ceil((double)(6.75F * scale));
                paint.setTextSize(titleRect);
                paint.setTypeface(Typeface.DEFAULT);

                try {
                    paint.setTypeface(Typeface.create("sans-serif-light", 0));
                } catch (Exception var55) {
                    ;
                }

                paint.setColor(Color.rgb(22, 22, 22));
                Rect layout = new Rect();
                paint.getTextBounds(distance, 0, distance.length(), layout);
                Rect b = makeRectWH(distanceTextRight - (int)Math.min(titleFontSize, (float)layout.width()), var58.getHeight() - borderB - layout.height() - fontYCorrection, (int)Math.min(titleFontSize, (float)layout.width()), layout.height());
                titleHeight -= layout.height();
                c.drawText(distance, (float)(b.left - layout.left), (float)(b.top - layout.top), paint);
            }

            int var67 = (int)Math.ceil((double)((float)(isTablet?9:7) * scale));
            if(titleHeight < var67) {
                assert false : "Title available drawing height not large enough for one line, shouldn\'t happen";

                titleHeight = var67;
            }

            Rect var68 = makeRectWH(textLeft, borderT, var66, titleHeight);
            paint.setTextSize((float)var67);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(Color.rgb(12, 12, 12));
            StaticLayout var69 = new StaticLayout(poiTitle, paint, var68.width(), Alignment.ALIGN_NORMAL, 1.0F, 1.0F, true);
            c.save();
            int var70 = 0;

            while(true) {
                if(var70 < var69.getLineCount()) {
                    if(var69.getLineBottom(var70) <= var68.height()) {
                        ++var70;
                        continue;
                    }

                    if(var70 > 0) {
                        var68.set(var68.left, var68.top, var68.right, var68.top + var69.getLineBottom(var70 - 1));
                    } else {
                        var68.set(var68.left, var68.top, var68.right, var68.top + (int)paint.getFontSpacing());
                    }
                }

                var68.top -= fontYCorrection;
                var68.bottom -= fontYCorrection;
                c.clipRect(var68);
                c.translate((float)var68.left, (float)var68.top);
                if(var69.getLineCount() > 0) {
                    var69.draw(c);
                }

                c.restore();
                ByteBuffer var71 = ByteBuffer.allocate(var58.getWidth() * var58.getHeight() * 4);
                var58.copyPixelsToBuffer(var71);
                ImageStruct texture = new ImageStruct(var71.array(), var58.getWidth(), var58.getHeight(), ECOLOR_FORMAT.ECF_RGBA8, true, 1.0D);
                ImageStruct var51 = texture;
                return var51;
            }
        } finally {
            var58.recycle();
            billboard = null;
            if(var59 != null) {
                var59.recycle();
                starImageEmpty = null;
            }

            if(starImageFull != null) {
                starImageFull.recycle();
                starImageFull = null;
            }

        }
    }

    protected IGeometry loadPOIGeometryForAnnotatedGeometriesGroup(IMetaioSDK sdk) {
        return getPOIGeometryForAnnotatedGeometriesGroup(this.mActivity, sdk);
    }

    static IGeometry getPOIGeometryForAnnotatedGeometriesGroup(Context context, IMetaioSDK sdk) {
        File path = AssetsManager.getAssetPathAsFile(context, "junaio/NewPOI_model.obj");
        if(path == null) {
            MetaioDebug.log(6, "POI model does not exist (asset: junaio/NewPOI_model.obj)");
            return null;
        } else {
            IGeometry ret = sdk.createGeometry(path);
            if(ret == null) {
                MetaioDebug.log(6, "Failed to load POI model");
                return null;
            } else {
                ret.setLLALimitsEnabled(true);
                return ret;
            }
        }
    }

    protected void onPOIGeometryInAnnotatedGeometriesGroupChangedState(IGeometry geometry, IARELObject poi, EGEOMETRY_FOCUS_STATE oldState, EGEOMETRY_FOCUS_STATE newState) {
        onPOIGeometryInAnnotatedGeometriesGroupChangedState(this.mActivity, geometry, poi, oldState, newState, this.getPOIManager());
    }

    static void onPOIGeometryInAnnotatedGeometriesGroupChangedState(Context context, IGeometry geometry, IARELObject poi, EGEOMETRY_FOCUS_STATE oldState, EGEOMETRY_FOCUS_STATE newState, IMetaioWorldPOIManager poiManager) {
        String textureName = "NewPOI_unfocused.png";
        switch (EGEOMETRY_FOCUS_STATE.swigToEnum(newState.ordinal()).ordinal()) {
            case 2:
                EPOI_PREDOMINANT_COLOR filePath = poiManager.calcPredominantColorForThumbnailOrGetCached(poi);
                String predominantColorSuffix = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(filePath == EPOI_PREDOMINANT_COLOR.EPPC_UNKNOWN?1:filePath.swigValue())});
                textureName = "NewPOI_focused__" + predominantColorSuffix + ".png";
                break;
            case 3:
                textureName = "NewPOI_selected.png";
            default:
                break;
        }

        String filePath1 = AssetsManager.getAssetPath(context, "junaio/" + textureName);
        if(filePath1 == null) {
            MetaioDebug.log(6, "POI texture does not exist (asset: junaio/" + textureName + ")");
        } else {
            poiManager.cachePOITexture(filePath1);
            geometry.setTexture(filePath1);
        }

    }

    public class ARELInterpreterAndroidJavaClient extends WebViewClient {

        private Activity mActivity;

        public ARELInterpreterAndroidJavaClient(Activity activity) {
            mActivity = activity;
        }

        public void onLoadResource(WebView view, String url) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.onLoadResource: " + url);
            ARELInterpreterAndroidJava2.this.arelEnabled = true;
            super.onLoadResource(view, url);
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.onPageStarted: " + url);
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.onPageFinished: " + url);
//            if(ARELInterpreterAndroidJava2.this.mPOIManagerCallback != null) {
//                ARELInterpreterAndroidJava2.this.mPOIManagerCallback.onLoadArelComplete();
//            } else {
                ARELInterpreterAndroidJava2.this.loadFinished();
//            }

            super.onPageFinished(view, url);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.onReceivedError: " + description + "errorCode" + errorCode + " url " + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
            if(description.contains("arel is not defined")) {
                ARELInterpreterAndroidJava2.this.arelEnabled = false;
            }

            if(ARELInterpreterAndroidJava2.this.arelEnabled) {
                view.loadUrl("javascript:arel.Debug.error(\"" + description + "\")");
            }

            if(description.toLowerCase(Locale.US).contains("the url could not be found")) {
                view.loadUrl("about:blank");
            }

        }

        public synchronized boolean shouldOverrideUrlLoading(WebView view, String url) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.shouldOverrideUrlLoading: " + url);
            if(!url.startsWith("arel://") && !url.startsWith("junaio://")) {
                if(!url.startsWith("http") && !url.startsWith("www")) {
                    try {
                        Intent e = new Intent("android.intent.action.VIEW", Uri.parse(url));
                        e.addFlags(131072);
                        List list = view.getContext().getPackageManager().queryIntentActivities(e, 65536);
                        if(list.size() > 0) {
                            view.getContext().startActivity(e);
                            return true;
                        }

                        list = view.getContext().getPackageManager().queryIntentServices(e, 65536);
                        if(list.size() > 0) {
                            view.getContext().startService(e);
                            return true;
                        }
                    } catch (Exception var5) {
                        MetaioDebug.log(6, "Invalid URI: " + url);
                    }
                }

                return false;
            } else {
//                if(ARELInterpreterAndroidJava2.this.mPOIManagerCallback != null) {
//                    ARELInterpreterAndroidJava2.this.mPOIManagerCallback.processURL(url);
//                } else {
                    ARELInterpreterAndroidJava2.this.processURL(url);
//                }

                view.loadUrl("javascript:(function(){arel.flush();})();");
                return true;
            }
        }

        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            MetaioDebug.log("ARELInterpreterAndroidJava2 shouldOverrideKeyEvent: " + event);
            return super.shouldOverrideKeyEvent(view, event);
        }

        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            MetaioDebug.log("ARELInterpreterAndroidJava2.onReceivedSslError: " + error);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage("SSL certificate is invalid. Do you want to continue?");
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public class ARELJavascriptInterface {
        public ARELJavascriptInterface() {
        }

        @JavascriptInterface
        public void flush(String commands) {
            ReentrantLock lock = new ReentrantLock();
            MetaioDebug.log("flush: " + commands);
//            if(ARELInterpreterAndroidJava2.this.mPOIManagerCallback != null) {
//                lock.lock();
//                ARELInterpreterAndroidJava2.this.mPOIManagerCallback.processURL(commands);
//                lock.unlock();
//            } else {
                ARELInterpreterAndroidJava2.this.processURL(commands);
//            }

        }
    }

    public class ARELWebChromeClient extends WebChromeClient {
        public ARELWebChromeClient() {
        }

        private String stupidEscapeStringForJS(String org) {
            if(org == null) {
                return "<null>";
            } else {
                String s = org.replace("\\", "\\\\");
                s = s.replace("\"", "\\\"");
                s = s.replace("\'", "\\\'");
                return s;
            }
        }

        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            MetaioDebug.log("ARELWebChromeClient.onJsAlert " + url + ":" + message);
            return super.onJsAlert(view, url, message, result);
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            MetaioDebug.log("ARELWebChromeClient onConsoleMessage (" + consoleMessage.sourceId() + "):" + consoleMessage.lineNumber() + ":" + consoleMessage.message());
            if(consoleMessage.message().contains("arel is not defined")) {
                ARELInterpreterAndroidJava2.this.arelEnabled = false;
            }

            if(ARELInterpreterAndroidJava2.this.arelEnabled) {
                String s = "javascript:arel.Debug.log(\"" + this.stupidEscapeStringForJS(consoleMessage.sourceId()) + ":" + consoleMessage.lineNumber() + ":" + this.stupidEscapeStringForJS(consoleMessage.message()) + "\")";
                ARELInterpreterAndroidJava2.this.mWebView.loadUrl(s);
            }

            return super.onConsoleMessage(consoleMessage);
        }

        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, QuotaUpdater quotaUpdater) {
            MetaioDebug.log("ARELWebChromeClient.onExceededDatabaseQuota: " + url + ", " + databaseIdentifier + ", " + currentQuota + ", " + estimatedSize + ", " + totalUsedQuota);
            quotaUpdater.updateQuota(estimatedSize * 2L);
        }

        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            callback.invoke(origin, true, false);
        }
    }
}
