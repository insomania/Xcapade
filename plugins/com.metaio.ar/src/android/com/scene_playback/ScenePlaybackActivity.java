package com.metaio.Template.scene_playback;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.superman.capade.MainActivity;
import com.superman.capade.R;
import com.metaio.Template.scene_playback.scene_picker.ScenePickerFragment;
import com.metaio.Template.scene_playback.scene_picker.UISceneList;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ELIGHT_TYPE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.ILight;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.zumoko.metaiohelper.SectionParent;
import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.geometry.ARViewHelper;
import com.zumoko.metaiohelper.scene_general.ARScene;
import com.zumoko.metaiohelper.scene_general.ARSceneList;

import java.io.File;

public class ScenePlaybackActivity extends SectionParent implements ScenePickerFragment.OnScenePickerListener
{
    private MetaioSDKCallbackHandler mSDKCallback;
    private static String TAG = "SceneCreationActivity";

    // CONSTANTS
    final static private int COS_ID = 1;

    final static boolean ENABLE_LIGHTING = true;

    private ILight mDirectionalLight1;

    private boolean mFirstTrackingEvent = true;

    private ARSceneList mSceneList;
    private UISceneList mUISceneList;
    private ARScene mCurrentScene;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // add metaio SDK key
        final String keyTag = ARViewActivity.TAG_BUNDLE_METAIO_KEY;
        if (savedInstanceState == null)
        {
            savedInstanceState = new Bundle();
        }
        String key = savedInstanceState.getString(keyTag);
        if (key == null)
        {
            savedInstanceState.putString(keyTag, "YZpufBfpgRbeeLmmLlqlK9SjCVCgTf5E32/gOvM4Dp0=");
        }

        super.onCreate(savedInstanceState);

        setContentView(mGUIView);

        mSDKCallback = new MetaioSDKCallbackHandler();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        stopAudio();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mSDKCallback.delete();
        mSDKCallback = null;
    }

    @Override
    protected int getGUILayout()
    {
        // Attaching layout to the activity
        return R.layout.activity_scene_playback;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        MetaioDebug.log("[onConfigurationChanged]");
        super.onConfigurationChanged(newConfig);
    }

    public void onXButton(View view)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void onReloadButton(View view)
    {
        mSurfaceView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                mCurrentScene.loadTrackingConfig(metaioSDK);
            }
        });
        mFirstTrackingEvent = true;
    }

    private void showToast(final String message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
    }

    private void setLighting()
    {
        mDirectionalLight1 = metaioSDK.createLight();
        mDirectionalLight1.setCoordinateSystemID(COS_ID);
        mDirectionalLight1.setType(ELIGHT_TYPE.ELIGHT_TYPE_DIRECTIONAL);
        mDirectionalLight1.setAmbientColor(new Vector3d(0.33f, 0.33f, 0.33f));
        mDirectionalLight1.setDiffuseColor(new Vector3d(0.99f, 0.99f, 0.9f));
        mDirectionalLight1.setDirection(new Vector3d(-150.f, 300.f, -150.f));
        mDirectionalLight1.setEnabled(true);
    }

    private void setLightingCOS(int cosID)
    {
        mDirectionalLight1.setCoordinateSystemID(cosID);
    }

    @Override
    protected void loadContents()
    {
        try
        {
            useAbsolutePaths(true);

            ////////////////////////////////////////////////////////////////////////////////////////
            // Set lighting
            if (ENABLE_LIGHTING)
            {
                setLighting();
            }

            ////////////////////////////////////////////////////////////////////////////////////////
            String scenePath = FileHelpers.getAbsolutePath(ARSceneList.getDefaultScenesFolder());
            mSceneList = ARSceneList.createFromFolder(scenePath, this);
            mUISceneList = new UISceneList(mSceneList);

            loadScene(0);

            ////////////////////////////////////////////////////////////////////////////////////////
            mFirstTrackingEvent = true;
        }
        catch (Exception e)
        {
            MetaioDebug.log(Log.ERROR, "Failed to load content: " + e);
            this.finish();
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry)
    {
        MetaioDebug.log("[onGeometryTouched] geom = " + geometry);
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return mSDKCallback;
    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
    {
        @Override
        public void onMovieEnd(IGeometry geometry, File filePath)
        {
            MetaioDebug.log("movie ended" + filePath.getPath());
        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String animationName)
        {
            MetaioDebug.log("[onAnimationEnd] Animation: " + animationName);
        }

        @Override
        public void onNewCameraFrame(ImageStruct cameraFrame)
        {
            MetaioDebug.log("[onNewCameraFrame] image W, H: " + cameraFrame.getWidth() + ", " + cameraFrame.getHeight());
        }

        @Override
        public void onTrackingEvent(TrackingValuesVector trackingValues)
        {
            MetaioDebug.log("[onTrackingEvent]");
            for (int i = 0; i < trackingValues.size(); i++)
            {
                final TrackingValues v = trackingValues.get(i);
                final int id = v.getCoordinateSystemID();
                final boolean trackingState = v.isTrackingState();

                if (trackingState)
                {
                    setLightingCOS(id);
                    if (mFirstTrackingEvent)
                    {
                        mFirstTrackingEvent = false;
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        MetaioDebug.log(TAG + "[onTouch][super]");
        return super.onTouch(v, event);
    }

    public void onSelectSceneButton(View view)
    {
        MetaioDebug.log(TAG + "[onSelectSceneButton]");
        showScenePicker();
    }

    private void showScenePicker()
    {
        if (mSceneList.getSceneList().size() == 0)
        {
            showToast("Scene list is empty.");
            return;
        }
        MetaioDebug.log(TAG + "[showScenePicker]");
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(ScenePickerFragment.newInstance(), ScenePickerFragment.TAG);
        ft.commit();
    }

    private void removeScenePicker()
    {
        MetaioDebug.log(TAG + "[removeModelPicker]");
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        final Fragment fragment = fm.findFragmentByTag(ScenePickerFragment.TAG);
        ft.remove(fragment);
        ft.commit();
    }

    @Override
    public void selectScene(int sceneNumber)
    {
        loadScene(sceneNumber);
        removeScenePicker();
    }

    private void loadScene(int sceneNumber)
    {
        final ARScene newScene = mSceneList.getSceneList().get(sceneNumber);
        final ARViewHelper helper = this;
        mSurfaceView.queueEvent(new Runnable()
        {
            @Override
            public void run()
            {
                if (mCurrentScene!=null)
                {
                    mCurrentScene.unloadGeometries(metaioSDK);
                }

                boolean loaded = newScene.loadTrackingConfig(metaioSDK);
                if (loaded)
                {
                    newScene.loadGeometries(helper);
                    mCurrentScene = newScene;
                }
                else
                {
                    showToast("Error encountered. The scene configuration is not valid. Please restart the app.");
                    if (mCurrentScene!=null)
                    {
                        mCurrentScene.loadTrackingConfig(metaioSDK);
                        mCurrentScene.loadGeometries(helper);
                    }
                }
            }
        });
    }

    @Override
    public UISceneList getSceneList()
    {
        return mUISceneList;
    }
}
