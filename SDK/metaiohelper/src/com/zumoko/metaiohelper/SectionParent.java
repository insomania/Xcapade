package com.zumoko.metaiohelper;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IShaderMaterialOnSetConstantsCallback;
import com.metaio.sdk.jni.IShaderMaterialSetConstantsService;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SWIGTYPE_p_void;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;
import com.zumoko.metaiohelper.geometry.ARViewHelper;
import com.zumoko.metaiohelper.geometry.IGeometryExt;
import com.zumoko.metaiohelper.expansion.ObbPathConverter;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Semaphore;

public abstract class SectionParent extends ARViewActivity implements ARViewHelper
{
    public static final String TAG_BUNDLE_OBB_IN_USE = "TAG_BUNDLE_OBB_IN_USE";

    protected Semaphore mMutexGUIView;

    // handler for delaying object appearance
    private final Handler mHandler = new Handler();

    private IGeometryExt mParentGeometry = null;
    private int mOriginalVolume;
    private boolean mObbInUse = false;
    private boolean mAbsolutePaths = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        if (savedInstanceState!=null)
        {
            mObbInUse = savedInstanceState.getBoolean(TAG_BUNDLE_OBB_IN_USE, false);
        }
        super.onCreate(savedInstanceState);
        mMutexGUIView = new Semaphore(1, true);
    }

    private boolean isObbFileInUse()
    {
        return mObbInUse;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // store original volume for later reset
        mOriginalVolume = getCurrentAudioStreamVolume();
        // get app volume from resources
        TypedValue resVal = new TypedValue();
        getResources().getValue(R.dimen.app_volume, resVal, true);
        final float appVolume = resVal.getFloat();
        setAudioStreamVolume(appVolume);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        setAudioStreamVolume(mOriginalVolume);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
//        resetGUIView();
    }

    private void resetGUIView()
    {
        final SectionParent thisActivity = this;
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    mMutexGUIView.acquire(1);

                    if (mGUIView != null)
                    {
                        // remove GUI view
                        ((ViewGroup) mGUIView.getParent()).removeView(mGUIView);

                        // Re-assign and reset the mGUIView

                        // Inflate GUI view if provided
                        final int layout = getGUILayout();
                        if (layout != 0)
                        {
                            MetaioDebug.log("onConfigurationChanged: mGUIView.inflate");
                            mGUIView = View.inflate(thisActivity, layout, null);
                            if (mGUIView == null)
                            {
                                MetaioDebug.log(Log.ERROR, "ARViewActivity: error inflating the given layout: " + layout);
                            }
                        }

                        if (mGUIView.getParent() == null)
                        {
                            MetaioDebug.log("onConfigurationChanged: addContentView(mGUIView)");
                            addContentView(mGUIView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        }
                        MetaioDebug.log("onConfigurationChanged: mGUIView.bringToFront");
                        mGUIView.bringToFront();
                    }

                    mMutexGUIView.release();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    MetaioDebug.log("[resetGUIView] mMutexGUIView LOCK FAILED. EXIT.");
                }
            }
        });
    }

    protected void defineParentGeometry(IGeometry parentGeom)
    {
        mParentGeometry = new IGeometryExt(parentGeom);
    }

    protected void upscaleParentGeometry()
    {
        if(mParentGeometry!=null)
        {
            mParentGeometry.incModelsScale();
        }
    }

    protected void downscaleParentGeometry()
    {
        if(mParentGeometry!=null)
        {
            mParentGeometry.decModelsScale();
        }
    }

    private void setSceneParentGeometry(IGeometry geom)
    {
        if (mParentGeometry==null)
        {
            return;
        }
        geom.setParentGeometry(mParentGeometry.model());
    }

    @Override
    public boolean loadInitTrackingConfig(String path)
    {
        // Getting a file path for tracking configuration XML file
        final File trackingConfigFile = AssetsManager.getAssetPathAsFile(getApplicationContext(), path);

        if (trackingConfigFile != null)
        {
            // Assigning tracking configuration
            boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
            MetaioDebug.log("[loadInitTrackingConfig] Tracking data loaded: " + result);
            return result;
        }
        else
        {
            MetaioDebug.log("[loadInitTrackingConfig] file >" + trackingConfigFile + "< does not exist");
        }
        return false;
    }

    protected boolean loadShaderMaterials(final String shaderMtlFile)
    {
        ////////////////////////////////////////////////////////////////////////////////////////
        // Loading shader materials
        final File shaderMaterialsFile = AssetsManager.getAssetPathAsFile(getApplicationContext(), shaderMtlFile);
        boolean result = false;
        if (shaderMaterialsFile != null)
        {
            result = metaioSDK.loadShaderMaterials(shaderMaterialsFile);
        }
        MetaioDebug.log("[loadContents] Shader materials loaded: " + result);
        return result;
    }

    protected void setTransparencyShader(IGeometry model, float transp)
    {
        MetaioDebug.log("[setTransparencyShader]");
        if ((transp<0.f) || (transp>1.f)) {
            MetaioDebug.log("[setTransparencyShader] ERROR: transparency needs to be within [0,1] range");
            return;
        }
        final float invTransp = 1.f - transp;

        model.setShaderMaterial("transparency");
        model.setShaderMaterialOnSetConstantsCallback(new IShaderMaterialOnSetConstantsCallback()
        {
            @Override
            public void onSetShaderMaterialConstants(String shaderMaterialName, SWIGTYPE_p_void extra, IShaderMaterialSetConstantsService constantsService)
            {
                final float time[] = new float[]{invTransp};
                constantsService.setShaderUniformF("myValue", time, 1);
            }
        });
    }

    protected IGeometry loadModel(String path, int cosId, float scale)
    {
        return loadModel(path, cosId, scale, new Rotation(new Vector3d(0.f, 0.f, 0.f)), new Vector3d(0.f, 0.f, 0.f));
    }

    protected IGeometry loadModel(String path, int cosId, float scale, Rotation rotation)
    {
        return loadModel(path, cosId, scale, rotation, new Vector3d(0.f, 0.f, 0.f));
    }

    @Override
    public IGeometry loadModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation)
    {
        return loadModel(path, cosId, new Vector3d(scale, scale, scale), rotation, translation);
    }

    protected IGeometry loadModel(String path, int cosId, Vector3d scale, Rotation rotation, Vector3d translation)
    {
        MetaioDebug.log("[loadModel] " + path);
        // Declare new model
        IGeometry model = null;
        // Getting a file path for a 3D geometry
        File file = getAssetPathAsFile(path);

        if (file != null)
        {
            // Loading 3D geometry
            model = metaioSDK.createGeometry(file);
            if (model != null)
            {
                // Set geometry properties
                model.setScale(scale);
                model.setRotation(rotation);
                model.setTranslation(translation);
                model.setCoordinateSystemID(cosId);
                setSceneParentGeometry(model);
            }
            else
                MetaioDebug.log(Log.ERROR, "Error loading geometry (from file: " + path + "): " + model);
        }
        else
            MetaioDebug.log(Log.ERROR, "Error locating file: " + path);

        return model;
    }

    protected IGeometry loadVideoModel(String path, int cosId, float scale, Rotation rotation, boolean transparent)
    {
        return loadVideoModel(path, cosId, scale, rotation, new Vector3d(0.f, 0.f, 0.f), transparent);
    }

    @Override
    public IGeometry loadVideoModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation, boolean transparent)
    {
        // Declare new model
        IGeometry model = null;
        // Getting a file path for a video
        File file = getAssetPathAsFile(path);

        if (file != null)
        {
            // Loading Video geometry
            model = metaioSDK.createGeometryFromMovie(file, transparent, false);
            if (model != null)
            {
                // Set geometry properties
                model.setScale(scale);
                model.setRotation(rotation);
                model.setTranslation(translation);
                model.setCoordinateSystemID(cosId);
                setSceneParentGeometry(model);
            }
            else
                MetaioDebug.log(Log.ERROR, "Error loading video geometry (from file: " + path + "): " + model);
        }
        else
            MetaioDebug.log(Log.ERROR, "Error locating video file: " + path);

        return model;
    }

    @Override
    public IGeometry loadImageModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation)
    {
        // Declare new model
        IGeometry model = null;
        // Getting a file path for a video
        File file = getAssetPathAsFile(path);

        if (file != null)
        {
            // Loading Video geometry
            model = metaioSDK.createGeometryFromImage(file, false, false);
            if (model != null)
            {
                // Set geometry properties
                model.setScale(scale);
                model.setRotation(rotation);
                model.setTranslation(translation);
                model.setCoordinateSystemID(cosId);
                setSceneParentGeometry(model);
            }
            else
                MetaioDebug.log(Log.ERROR, "Error loading image geometry (from file: " + path + "): " + model);
        }
        else
            MetaioDebug.log(Log.ERROR, "Error locating image file: " + path);

        return model;
    }

    protected boolean setTexture(IGeometry geom, final String path)
    {
        File file = getAssetPathAsFile(path);

        if (file==null)
        {
            MetaioDebug.log("[setTexture] Error: file does not exist!");
            return false;
        }

        geom.setTexture(file);
        return true;
    }

    protected void playAudio(String relativeFilePathInAssetFolder)
    {
        playAudio(relativeFilePathInAssetFolder, false);
    }

    @Override
    public void playAudio(String relativeFilePathInAssetFolder, boolean loop)
    {
        String pathToPass = relativeFilePathInAssetFolder;
        if (isObbFileInUse())
        {
            pathToPass = ObbPathConverter.getPathInUnzippedObb(getApplicationContext(), relativeFilePathInAssetFolder);
        }

        Intent starter = new Intent(this, AudioPlaybackService.class);
        starter.putExtra("path", pathToPass);
        starter.putExtra("absPath", isObbFileInUse());
        starter.putExtra("loop", loop);
        startService(starter);
    }

    @Override
    public void stopAudio()
    {
        Intent audioService = new Intent(this, AudioPlaybackService.class);
        stopService(audioService);
    }

    private int getCurrentAudioStreamVolume()
    {
        AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        return mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void setAudioStreamVolume(float volume)
    {
        MetaioDebug.log("[SectionParent::setAudioStreamVolume] new volume = " + Float.toString(volume));
        if ((volume<0.f) || (volume>1.f))
        {
            return;
        }
        AudioManager mgr = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        final int maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int newVolume = (int)(volume * (float)maxVolume);
        mgr.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
    }


    protected boolean isNetworkAvailable()
    {
        MetaioDebug.log("[SectionParent::isNetworkAvailable]");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    protected boolean isURLAvailable(String urlString)
    {
        MetaioDebug.log("[SectionParent::isURLAvailable]");
        if (isNetworkAvailable())
        {
            MetaioDebug.log("[SectionParent::isURLAvailable] network available");
            try
            {
                URL url = new URL(urlString);
                MetaioDebug.log("[SectionParent::isURLAvailable] open connection");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(1500);
                urlConnection.connect();
                urlConnection.disconnect();
                return true;
            }
            catch (IOException e)
            {
                MetaioDebug.log("[SectionParent::isURLAvailable] exception triggered");
                Log.d("SectionParent", "Exception", e);
            }
        }
        return false;
    }

    protected void informUserSiteNotAvailable()
    {
        MetaioDebug.log("[SectionParent::informUserSiteNotAvailable]");
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                String message = getApplicationContext().getResources().getString(R.string.site_not_available);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public String getAssetPath(String filename)
    {
        String path;
        if (!isObbFileInUse())
        {
            MetaioDebug.log("[getAssetPath] Loading from Assets folder");
            path = AssetsManager.getAssetPath(getApplicationContext(), filename);
        }
        else
        {
            MetaioDebug.log("[getAssetPath] Loading from unpacked Expansion File (OBB)");
            path = ObbPathConverter.getPathInUnzippedObb(getApplicationContext(), filename);
        }
        return path;
    }

    @Override
    public File getAssetPathAsFile(String filename)
    {
        File file;
        if (!mAbsolutePaths)
        {
            if (!isObbFileInUse())
            {
                MetaioDebug.log("[getAssetPathAsFile] Loading from Assets folder");
                file = AssetsManager.getAssetPathAsFile(getApplicationContext(), filename);
            } else
            {
                MetaioDebug.log("[getAssetPathAsFile] Loading from unpacked Expansion File (OBB)");
                file = new File(ObbPathConverter.getPathInUnzippedObb(getApplicationContext(), filename));
            }
        }
        else
        {
            file = new File(filename);
        }
        return file;
    }

    @Override
    public void runOnGLThreadAfterInterval(final Runnable run, final long delay)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mSurfaceView == null)
                        {
                            return;
                        }
                        mSurfaceView.queueEvent(run);
                    }
                }, delay);
            }
        });
    }

    public void useAbsolutePaths(final boolean absolutePaths)
    {
        mAbsolutePaths = absolutePaths;
    }
}
