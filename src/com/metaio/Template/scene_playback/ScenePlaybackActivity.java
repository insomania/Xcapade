package com.metaio.Template.scene_playback;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.metaio.Template.ar;
import com.xcapade.MainActivity;
import com.xcapade.R;
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
import com.zumoko.metaiohelper.tracking.TrackingConfigCreatorMultiMarker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScenePlaybackActivity extends SectionParent implements PuzzleAnsweringFragment.OnFragmentPuzzleAnsweringListener
{
    private MetaioSDKCallbackHandler mSDKCallback;

  // CONSTANTS
    final static private int COS_ID = 1;
    final static boolean ENABLE_LIGHTING = true;

    private ILight mDirectionalLight1;
    private boolean mFirstTrackingEvent = true;
    private ARSceneList mSceneList;
    private ARScene mCurrentScene;
    private String[] spotData;

    // New
    private List<IGeometry> mSceneGeoms = new ArrayList<>();
    private LinearLayout loadProgress;
    private ProgressBar previewImageloadProgress;
    private String firstMarkerPath;
    private static final String FIRST_MARKER_IMAGE = "firstMarkerImage";
    private Handler handler;
    private Runnable myRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        spotData = getIntent().getStringArrayExtra("spotData");

        /*
        * spotData[0] : Login User Id
        * spotData[1] : Spot Id
        * spotData[2] : Spot Name
        * spotData[3] : Spot Puzzle
        * spotData[4] : Spot Puzzle Hint
        * spotData[5] : Spot Puzzle Answer
        */

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

        loadProgress = (LinearLayout) findViewById(R.id.progress_circle);
        loadProgress.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            String path = savedInstanceState.getString(FIRST_MARKER_IMAGE);

            if(path == null) {
                handler = new android.os.Handler();
                myRunnable = new Runnable() {
                    public void run() {
                        File imgFile = new  File(firstMarkerPath);

                        if(imgFile.exists()){
                            Bitmap markerBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            ImageView markerImage = (ImageView) findViewById(R.id.preview_scene_img);
                            markerImage.setImageBitmap(markerBitmap);
                            previewImageloadProgress = (ProgressBar) findViewById(R.id.preview_progress_circle);
                            previewImageloadProgress.setVisibility(View.GONE);
                        }
                    }
                };
                handler.postDelayed(myRunnable, 5000);
            }
            else {
                firstMarkerPath = path;
                File imgFile = new  File(firstMarkerPath);

                if(imgFile.exists()){
                    Bitmap markerBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageView markerImage = (ImageView) findViewById(R.id.preview_scene_img);
                    markerImage.setImageBitmap(markerBitmap);
                    previewImageloadProgress = (ProgressBar) findViewById(R.id.preview_progress_circle);
                    previewImageloadProgress.setVisibility(View.GONE);
                }
            }

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putString(FIRST_MARKER_IMAGE, firstMarkerPath);
        if(handler != null) {
            handler.removeCallbacks(myRunnable);
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder .setMessage("The secret haven’t been solved. You will not receive any rewards. Do you really want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /* public void onReloadButton(View view)
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
    } */

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

            // Set lighting
            if (ENABLE_LIGHTING)
            {
                setLighting();
            }

            ////////////////////////////////////////////////////////////////////////////////////////
            String scenePath = FileHelpers.getAbsolutePath(ARSceneList.getDefaultScenesFolder());
            mSceneList = ARSceneList.createFromFolder(scenePath, this);

            int selectedSceneNumber = 0;
            int num = 0;

            for (ARScene _arScene : mSceneList.getSceneList()){
                String name = _arScene.getName();

                if(name.equals(spotData[2])){
                    selectedSceneNumber = num;
                }
                num++;
            }

            loadScene(selectedSceneNumber);
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

        // Clickable for only main object
        IGeometry tempGeom = mSceneGeoms.get(0);
        if(tempGeom.equals(geometry)) {
            showPuzzleAnswerDialog();
        }
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
    {
        return mSDKCallback;
    }

    private void showPuzzleAnswerDialog()
    {
        showPuzzleAnsweringFragment();
    }
    private void showPuzzleAnsweringFragment()
    {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(PuzzleAnsweringFragment.newInstance(), PuzzleAnsweringFragment.TAG);
        ft.commit();
    }
    private void removePuzzleAnsweringFragment()
    {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(PuzzleAnsweringFragment.TAG);
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(f);
        ft.commit();
    }

    @Override
    public void onPuzzleAnsweringConfirmed(String answerStr)
    {
        if(!answerStr.equals(spotData[5]))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder .setMessage("The answer is not right")
                    .setPositiveButton("Back", null)
                    .show();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder .setMessage("The answer is right")
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //Registering the point as completion of puzzle
                            String urlParameters = "mode=sceneCompleted";
                            // spotData[0] : User ID
                            // spotData[1] : Spot ID
                            urlParameters = urlParameters + "&userId=" + spotData[0];
                            urlParameters = urlParameters + "&spotId=" + spotData[1];

                            String targetURL = "http://xcapade.co/Orienteering/include/api.php";

                            (new ScenePlaybackActivity.FetchRtmpURL(targetURL, urlParameters)).execute();
                            loadProgress.setVisibility(View.VISIBLE);
                        }
                    })
                    .show();
        }
        removePuzzleAnsweringFragment();
    }

    @Override
    public void onPuzzleAnsweringCanceled()
    {
        removePuzzleAnsweringFragment();
    }

    @Override
    public String[] onRestoreForm() {

        String[] puzzleStoreData = new String[3];
        // spotData[3] : Spot Puzzle
        // spotData[4] : Spot Puzzle Hint
        // spotData[5] : Spot Puzzle Answer
        puzzleStoreData[0] = spotData[3];
        puzzleStoreData[1] = spotData[4];
        puzzleStoreData[2] = spotData[5];

        return puzzleStoreData;
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
      String TAG = "SceneCreationActivity";
      MetaioDebug.log(TAG + "[onTouch][super]");
        return super.onTouch(v, event);
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
                    mSceneGeoms = mCurrentScene.getGeometries();

                    if(firstMarkerPath == null) {
                        firstMarkerPath = mCurrentScene.getMarkerImagesPath();
                    }

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

    class FetchRtmpURL extends AsyncTask<String, String, String> {

        String targetURL;
        String urlParameters;

        public FetchRtmpURL(String targetURL, String urlParameters) {
            this.targetURL = targetURL;
            this.urlParameters = urlParameters;
        }

        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {


            URL url;
            HttpURLConnection connection = null;
            try {
                //Create connection
                url = new URL(targetURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
                //connection.setRequestProperty("Content-Language", "en-US");

                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                //Get Response
                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                return response.toString();

            } catch (Exception e) {

                e.printStackTrace();
                return null;

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }

        @Override
        protected void onPostExecute(String result) {
            MetaioDebug.log("Response from server: " + result);
            loadProgress.setVisibility(View.GONE);

            Boolean status;
            try {
                JSONObject jObject = new JSONObject(result);
                status = jObject.getBoolean("success");
            } catch (Exception e) {
                status = false;
            }

            if(status) {
                showToast("Great! You successfully resolved this scene. You can try to resolve the other scene.");

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                ar.callbackContextKeepCallback.success("success");
            }
            else {
                showToast("Server connection error. Please check your Internet connectivity.");
            }

        }
    }

}
