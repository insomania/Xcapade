package com.metaio.Template.scene_creation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.metaio.Template.ar;
import com.xcapade.MainActivity;
import com.xcapade.R;
import com.metaio.Template.scene_creation.model_picker.ModelPickerFragment;
import com.metaio.Template.scene_creation.model_picker.UIModelList;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.GestureHandlerAndroid;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ELIGHT_TYPE;
import com.metaio.sdk.jni.GestureHandler;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.ILight;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.zumoko.metaiohelper.SectionParent;
import com.zumoko.metaiohelper.aws.s3.S3FolderUploadAsyncTask;
import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.geometry.ARViewHelper;
import com.zumoko.metaiohelper.scene_general.ARModel;
import com.zumoko.metaiohelper.scene_general.ARModelList;
import com.zumoko.metaiohelper.scene_general.ARSceneList;
import com.zumoko.metaiohelper.scene_general.xml.ARSceneXML;
import com.zumoko.metaiohelper.tracking.TrackingConfigCreatorMultiMarker;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class SceneCreationActivity extends SectionParent implements ModelPickerFragment.OnModelPickerListener,
        SceneSavingFragment.OnFragmentSceneSavingListener,
        FragmentMarkerManagement.OnFragmentMarkerManagementInteractionListener {
    private MetaioSDKCallbackHandler mSDKCallback;
    private static String TAG = "SceneCreationActivity";

    // CONSTANTS
    final static private int COS_ID = 1;
    final static private String PATH_FOLDER_MODELS = ARModelList.getDefaultModelsFolder() + "/";
    final static private String PATH_FOLDER_SCENES = ARSceneList.getDefaultScenesFolder() + "/";

    final static private String SAVE_FOLDER_PATH = "SceneSaved/";
    final static private String TEMP_FOLDER_PATH = "SceneTemp/";

    final static private String TRACKING_CONFIG_NAME = "config.xml";
    final static private String TRACKING_CONFIG_PATH = SAVE_FOLDER_PATH + TRACKING_CONFIG_NAME;
    final static private String TRACKING_CONFIG_TEMP_PATH = TEMP_FOLDER_PATH + TRACKING_CONFIG_NAME;

    final static boolean ENABLE_LIGHTING = true;

    private ILight mDirectionalLight1;

    private GestureHandlerAndroid mGestureHandler;

    private int mStreamWidth = 256;
    private int mStreamHeight = 256;

    private boolean mFirstTrackingEvent = true;

    private ARModelList mModelList;
    private UIModelList mUIModelList;

    private List<ARModel> mSceneModels = new ArrayList<>();
    private List<ARModel> mSavedSceneModels;
    private List<IGeometry> mSceneGeoms = new ArrayList<>();
    private int mGestureHandlerGeomCounter = 0;

    private IGeometry mLastTouchedGeom;

    private LocationManager mLocationManager;
    private boolean mLocalizationEnabled;

    // The minimum distance to change Updates in meters
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.f; // 5 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 30; // 0.5 minute

    private FragmentMarkerManagement mMarkerManagementFragment;

    TrackingConfigCreatorMultiMarker mTrackingConfigCreator = new TrackingConfigCreatorMultiMarker();
    TrackingConfigCreatorMultiMarker mSavedTrackingConfigCreator = new TrackingConfigCreatorMultiMarker();

    private int mActiveCOS = -1;

    // used only when adding/replacing marker
    private int mMarkerToReplace = -1; // -1 means that a new cos/marker is to be added

    private String userID;
    private String spotName;
    private String spotUniqueID;
    private String spotLatitude;
    private String spotLongitude;
    private String spotDifficulty;
    private String spotCoin;
    private String spotDescription;
    private String spotHint;
    private String spotPuzzle;
    private String spotPuzzleHint;
    private String spotPuzzleAnswer;

    private String[] spotData;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        spotData = getIntent().getStringArrayExtra("spotData");
        // add metaio SDK key
        final String keyTag = ARViewActivity.TAG_BUNDLE_METAIO_KEY;
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        String key = savedInstanceState.getString(keyTag);
        if (key == null) {
            savedInstanceState.putString(keyTag, "YZpufBfpgRbeeLmmLlqlK9SjCVCgTf5E32/gOvM4Dp0=");
        }

        super.onCreate(savedInstanceState);

        setContentView(mGUIView);

        // clean saved dir on startup
        clearDir(SAVE_FOLDER_PATH);

        // init location manager and request location update
        initLocationManager();

        mSDKCallback = new MetaioSDKCallbackHandler();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        reAttachMarkerManagementFragment();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void reAttachMarkerManagementFragment() {
        MetaioDebug.log("[reAttachMarkerManagementFragment]");
        mMarkerManagementFragment = null;

        try {
            mMutexGUIView.acquire();

            FragmentManager fm = getFragmentManager();

            // remove the fragment if exists
            FragmentMarkerManagement frag = (FragmentMarkerManagement) fm.findFragmentByTag(FragmentMarkerManagement.TAG);
            if (frag != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(frag);
                ft.commit();
            }

            // add new Marker Management Fragment
            mMarkerManagementFragment = FragmentMarkerManagement.newInstance();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.marker_fragment_placeholder, mMarkerManagementFragment, FragmentMarkerManagement.TAG);
            ft.commit();

            mMutexGUIView.release();
        } catch (InterruptedException exc) {
            MetaioDebug.log(TAG + "[reAttachMarkerManagementFragment] ERRRROORRRR");
            exc.printStackTrace();
        }
    }

    private void initLocationManager() {
        try {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            // Getting GPS status
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                showToast("Please enable the GPS localization before scene creation");
                mLocalizationEnabled = false;
            } else {
                mLocalizationEnabled = true;
                LocationListener listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {

                    }

                    @Override
                    public void onStatusChanged(final String provider, final int status, final Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(final String provider) {

                    }

                    @Override
                    public void onProviderDisabled(final String provider) {

                    }
                };

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = mLocationManager.getBestProvider(criteria, true);
                mLocationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, listener);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSDKCallback.delete();
        mSDKCallback = null;
    }

    @Override
    protected int getGUILayout() {
        // Attaching layout to the activity
        return R.layout.activity_scene_creation;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        MetaioDebug.log("[onConfigurationChanged]");
        super.onConfigurationChanged(newConfig);
        // reAttachMarkerManagementFragment();
    }

    public void onXButton(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have not uploaded the puzzle yet, do you want to quit?")
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

    /*
    public void onReloadButton(View view)
    {
        if (mSavedSceneModels!=null)
        {
            resetSavedModels();
        }

        if (mSavedTrackingConfigCreator!=null)
        {
            // reset the tracking config creator
            mTrackingConfigCreator = new TrackingConfigCreatorMultiMarker(mSavedTrackingConfigCreator);
        }

        clearDir(TEMP_FOLDER_PATH);

        copySavedTrackingToTempFolder();

        mFirstTrackingEvent = true;
        loadSavedTrackingConfig();
    }
    */
    public void onNewSceneButton(View view) {
        MetaioDebug.log(TAG + "[onNewSceneButton]");
        new AlertDialog.Builder(SceneCreationActivity.this)
                .setTitle("New Scene")
                .setMessage("Are you sure you want to clear everything and start from blank scene?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        restartActivity();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();

    }

    /*
    public void onInstant2DButton(View view)
    {
        MetaioDebug.log("[onInstant2DButton]");
        FileHelpers.deleteLocalDataFile(TRACKING_CONFIG_TEMP_PATH);
        metaioSDK.startInstantTracking("INSTANT_2D_GRAVITY", FileHelpers.getLocalDataFile(TRACKING_CONFIG_TEMP_PATH));
    }
    */
    public void onSaveButton(View view) {
        showSaveSceneDialog();
    }

    private void showSaveSceneDialog() {
        // check if the tracking config exists
        File tempTrackingFile = FileHelpers.getLocalDataFile(TRACKING_CONFIG_TEMP_PATH);
        if (!tempTrackingFile.exists()) {
            showToast("Error encountered. You first need to capture the marker, in order to save the scene.");
            return;
        }

        if (mSceneGeoms.size() == 0) {
            showToast("Error encountered. You first need to insert models to the scene, in order to save it.");
            return;
        }

        showSceneSavingFragment();
    }

    private void showSceneSavingFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(SceneSavingFragment.newInstance(), SceneSavingFragment.TAG);
        ft.commit();
    }

    private void removeSceneSavingFragment() {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentByTag(SceneSavingFragment.TAG);
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(f);
        ft.commit();
    }

    @Override
    public void onSceneSavingConfirmed(String name, String coin, String descr, String hint, String puzzle, String puzzlehint, String puzzleanswer) {
        spotName = name;
        spotCoin = coin;
        spotDescription = descr;
        spotHint = hint;
        spotPuzzle = puzzle;
        spotPuzzleHint = puzzlehint;
        spotPuzzleAnswer = puzzleanswer;

        // generating Unique number.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = Calendar.getInstance().getTime();
        int randNum = (int) Math.random() * 1000;
        spotUniqueID = sdf.format(date).toString() + Integer.toString(randNum);

        saveScene(name, descr, spotUniqueID);
        removeSceneSavingFragment();
    }

    @Override
    public void onSceneSavingCanceled() {
        removeSceneSavingFragment();
    }

    @Override
    public String[] onRestoreForm() {
        String[] spotStoreData = new String[7];

        spotStoreData[0] = spotName;
        spotStoreData[1] = spotCoin;
        spotStoreData[2] = spotDescription;
        spotStoreData[3] = spotHint;
        spotStoreData[4] = spotPuzzle;
        spotStoreData[5] = spotPuzzleHint;
        spotStoreData[6] = spotPuzzleAnswer;

        return spotStoreData;
    }

    private void saveScene(String sceneName, String sceneDescr, String sceneUniqueID) {
        // get date/time
        Date date = Calendar.getInstance().getTime();
        final String dateAndTimeOfCreation = date.toString();

        // get location
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(criteria, true);
        Location location = mLocationManager.getLastKnownLocation(provider);
        double lat = 0.0;
        double lngt = 0.0;
        if (location != null) {
            lat = location.getLatitude();
            lngt = location.getLongitude();
        } else {
            showToast("Error encountered. Please enable the GPS/Network based localization prior the scene saving.");
            return;
        }

        mSavedSceneModels = ARSceneXML.saveScene(TRACKING_CONFIG_TEMP_PATH, SAVE_FOLDER_PATH,
                sceneName, sceneDescr, sceneUniqueID,
                lat, lngt, dateAndTimeOfCreation,
                mSceneModels, mSceneGeoms);

        if (mSavedSceneModels == null) {
            showToast("Error encountered. You first need to capture the marker and insert models to the scene, in order to save it.");
            return;
        }

        // duplicate current state of all geometries in the scene (used for reloading)
        mSavedSceneModels = ARModelList.deepCopySceneModels(mSceneModels);
        mSavedTrackingConfigCreator = new TrackingConfigCreatorMultiMarker(mTrackingConfigCreator);
        for (int i = 0; i < mSavedSceneModels.size(); i++) {
            ARModel arModel = mSavedSceneModels.get(i);
            IGeometry geom = mSceneGeoms.get(i);
            arModel.getARGeomData().setFromGeometry(geom);
        }
    }

    public void onAddButton(View view) throws UnsupportedEncodingException {
        addMarker();
    }

    public void onUploadButton(View view) throws UnsupportedEncodingException {
        //  Agenda
        //  0. check if saved config exists
        //  1. check if internet connectivity exists
        //  2. create scene folder
        //  3. copy saved folder to the scene folder
        //  4. upload scene folder
        //  5. delete scene folder and any aws data, if failure
        //  6. Register a scene on Web server

        //  0. check if saved config exists
        if (!savedSceneExists()) {
            showToast("Error encountered. A scene needs to be saved first, in order to upload it.");
            return;
        }

        //  1. check if internet connectivity exists
        if (!isNetworkAvailable()) {
            showToast("Error encountered. Please check your Internet connectivity.");
            return;
        }

        //  2. create scene folder
        Long tsLong = System.currentTimeMillis() / 1000;
        String relativeFolderPath = FileHelpers.setFolderDelimiters(PATH_FOLDER_SCENES) + tsLong.toString() + "/";
        final File sceneFolder = FileHelpers.createLocalDataFile(relativeFolderPath, false);

        String savedSceneXMLPath = FileHelpers.setFolderDelimiters(SAVE_FOLDER_PATH) + ARSceneXML.getDefaultSceneXmlFilename();
        File savedSceneXML = FileHelpers.getLocalDataFile(savedSceneXMLPath);

        //  3. copy saved folder to the scene folder
        FileHelpers.copyFolderRecursive(savedSceneXML.getParentFile(), sceneFolder);

        // 4. Register a scene on Web server
        String urlParameters = "mode=" + URLEncoder.encode("addscene", "UTF-8");
        urlParameters = urlParameters + "&Bucket_name=" + URLEncoder.encode(tsLong.toString(), "UTF-8");

        userID = spotData[0];
        spotLatitude = spotData[1];
        spotLongitude = spotData[2];
        spotDifficulty = "";

        urlParameters = urlParameters + "&userid=" + URLEncoder.encode(userID, "UTF-8");
        urlParameters = urlParameters + "&spotName=" + URLEncoder.encode(spotName, "UTF-8");
        urlParameters = urlParameters + "&spotUniqueID=" + URLEncoder.encode(spotUniqueID, "UTF-8");
        urlParameters = urlParameters + "&spotLatitude=" + URLEncoder.encode(spotLatitude, "UTF-8");
        urlParameters = urlParameters + "&spotLongitude=" + URLEncoder.encode(spotLongitude, "UTF-8");
        urlParameters = urlParameters + "&spotDifficulty=" + URLEncoder.encode(spotDifficulty, "UTF-8");
        urlParameters = urlParameters + "&spotCoin=" + URLEncoder.encode(spotCoin, "UTF-8");
        urlParameters = urlParameters + "&spotDescription=" + URLEncoder.encode(spotDescription, "UTF-8");
        urlParameters = urlParameters + "&spotHint=" + URLEncoder.encode(spotHint, "UTF-8");
        urlParameters = urlParameters + "&spotPuzzle=" + URLEncoder.encode(spotPuzzle, "UTF-8");
        urlParameters = urlParameters + "&spotPuzzleHint=" + URLEncoder.encode(spotPuzzleHint, "UTF-8");
        urlParameters = urlParameters + "&spotPuzzleAnswer=" + URLEncoder.encode(spotPuzzleAnswer, "UTF-8");

        String targetURL = "http://xcapade.co/Orienteering/include/api.php";

        (new FetchRtmpURL(targetURL, urlParameters, sceneFolder)).execute();

    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
    }

    private boolean savedSceneExists() {
        String savedSceneXMLPath = FileHelpers.setFolderDelimiters(SAVE_FOLDER_PATH) + ARSceneXML.getDefaultSceneXmlFilename();
        File savedSceneXML = FileHelpers.getLocalDataFile(savedSceneXMLPath);
        return (savedSceneXML.exists());
    }

    private void clearDir(final String relativePath) {
        File folder = FileHelpers.getLocalDataFile(relativePath);
        if (folder.exists() && folder.isDirectory()) {
            FileHelpers.deleteRecursive(folder);
        }
        FileHelpers.createFolderStructure(folder, true);
    }

    private void copySavedTrackingToTempFolder() {
        File savedTracking = FileHelpers.getLocalDataFile(TRACKING_CONFIG_PATH);
        File tempTracking = FileHelpers.getLocalDataFile(TRACKING_CONFIG_TEMP_PATH);

        if (savedTracking.exists()) {
            FileHelpers.overwriteFile(savedTracking, tempTracking);
        }

        // copy all markers
        if (mSavedTrackingConfigCreator != null) {
            List<String> markers = mSavedTrackingConfigCreator.getMarkerFilenames();
            Iterator<String> iterMarkers = markers.iterator();
            while (iterMarkers.hasNext()) {
                final String fileName = iterMarkers.next();
                final String savedFileName = SAVE_FOLDER_PATH + fileName;
                final String tempFileName = TEMP_FOLDER_PATH + fileName;

                File tempMarker = FileHelpers.getLocalDataFile(tempFileName);
                File savedMarker = FileHelpers.getLocalDataFile(savedFileName);

                if (savedMarker.exists()) {
                    FileHelpers.overwriteFile(savedMarker, tempMarker);
                }
            }
        }
    }

    private void resetSavedModels() {
        if ((mSavedSceneModels != null) && (mSavedSceneModels.size() > 0)) {
            final ARViewHelper arViewHelper = this;
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    removeAllSceneModels();
                    for (ARModel arModel : mSavedSceneModels) {
                        IGeometry geom = arModel.getARGeomData().loadGeometry(arViewHelper);
                        if (geom != null) {
                            geom.setVisible(true);
                            mSceneGeoms.add(geom);
                            mSceneModels.add(arModel);
                            mGestureHandler.addObject(geom, mGestureHandlerGeomCounter++);
                        }
                    }
                }
            });
        }
    }

    private void removeAllSceneModels() {
        for (int i = 0; i < mSceneModels.size(); i++) {
            IGeometry geomToRemove = mSceneGeoms.get(i);
            metaioSDK.unloadGeometry(geomToRemove);
            mGestureHandler.removeObject(geomToRemove);
        }
        mSceneModels.clear();
        mSceneGeoms.clear();
    }

    private void setLighting() {
        mDirectionalLight1 = metaioSDK.createLight();
        mDirectionalLight1.setCoordinateSystemID(COS_ID);
        mDirectionalLight1.setType(ELIGHT_TYPE.ELIGHT_TYPE_DIRECTIONAL);
        mDirectionalLight1.setAmbientColor(new Vector3d(0.33f, 0.33f, 0.33f));
        mDirectionalLight1.setDiffuseColor(new Vector3d(0.99f, 0.99f, 0.9f));
        mDirectionalLight1.setDirection(new Vector3d(-150.f, 300.f, -150.f));
        mDirectionalLight1.setEnabled(true);
    }

    private void setLightingCOS(int cosID) {
        mDirectionalLight1.setCoordinateSystemID(cosID);
    }

    private void loadSavedTrackingConfig() {
        File trackingConfig = FileHelpers.getLocalDataFile(TRACKING_CONFIG_PATH);
        if (trackingConfig.exists()) {
            MetaioDebug.log("[loadContents] Set saved tracking config");
            boolean result = metaioSDK.setTrackingConfiguration(trackingConfig);
            MetaioDebug.log("[loadContents] Tracking data loaded: " + result);
        }
    }

    @Override
    protected void loadContents() {
        try {
            clearDir(SAVE_FOLDER_PATH);
            clearDir(TEMP_FOLDER_PATH);
            FileHelpers.createFolderStructure(new File(SAVE_FOLDER_PATH), true);
            FileHelpers.createFolderStructure(new File(TEMP_FOLDER_PATH), true);

            ////////////////////////////////////////////////////////////////////////////////////////
            // Set tracking configuration
            loadSavedTrackingConfig();

            ////////////////////////////////////////////////////////////////////////////////////////
            // Set lighting
            if (ENABLE_LIGHTING) {
                setLighting();
            }

            ////////////////////////////////////////////////////////////////////////////////////////
            // Load model list
            final String absPath = FileHelpers.getAbsolutePath(PATH_FOLDER_MODELS);
            mModelList = ARModelList.createFromFolder(absPath);
            mUIModelList = new UIModelList(mModelList);

            ////////////////////////////////////////////////////////////////////////////////////////
            // Create gesture handler
            mGestureHandler = new GestureHandlerAndroid(metaioSDK, GestureHandler.GESTURE_ALL);

            ////////////////////////////////////////////////////////////////////////////////////////
            mFirstTrackingEvent = true;

            useAbsolutePaths(true);
        } catch (Exception e) {
            MetaioDebug.log(Log.ERROR, "Failed to load content: " + e);
            this.finish();
        }
    }

    @Override
    protected void onGeometryTouched(IGeometry geometry) {
        MetaioDebug.log("[onGeometryTouched] geom = " + geometry);
        mLastTouchedGeom = geometry;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return mSDKCallback;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SceneCreation Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
        @Override
        public void onMovieEnd(IGeometry geometry, File filePath) {
            MetaioDebug.log("movie ended" + filePath.getPath());
        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String animationName) {
            MetaioDebug.log("[onAnimationEnd] Animation: " + animationName);
        }

        @Override
        public void onNewCameraFrame(ImageStruct cameraFrame) {
            MetaioDebug.log("[onNewCameraFrame] image W, H: " + cameraFrame.getWidth() + ", " + cameraFrame.getHeight());
        }

        @Override
        public void onCameraImageSaved(File filePath) {
            MetaioDebug.log("[onCameraImageSaved] file: " + filePath.toString());
            mStreamWidth = metaioSDK.getCamera().getResolution().getX();
            mStreamHeight = metaioSDK.getCamera().getResolution().getY();

            MetaioDebug.log("[onCameraImageSaved] W, H: " + mStreamWidth + ", " + mStreamHeight);
            if (mMarkerToReplace == -1) {
                mTrackingConfigCreator.addCOS(filePath.getName(), mStreamWidth, mStreamHeight);
            } else {
                final String markerFileToRemove = mTrackingConfigCreator.replaceCOS(mMarkerToReplace, filePath.getName(), mStreamWidth, mStreamHeight);
                deleteUnusedMarker(markerFileToRemove);
            }

            resetTempTrackingConfig();
        }

//        @Override
//        public void onInstantTrackingEvent(boolean success, File filePath)
//        {
//            if (success)
//            {
//                metaioSDK.setTrackingConfiguration(filePath);
//                File file = FileHelpers.getLocalDataFile(TRACKING_CONFIG_TEMP_PATH);
//                MetaioDebug.log("[onInstantTrackingEvent] config exists = " + file.exists());
//                MetaioDebug.log("[onInstantTrackingEvent] config size = " + file.length());
//            }
//        }

        @Override
        public void onTrackingEvent(TrackingValuesVector trackingValuesChanged) {
            MetaioDebug.log("[onTrackingEvent]");
            boolean oneCosVisible = false;

            TrackingValuesVector trackingValues = metaioSDK.getTrackingValues();

            for (int i = 0; i < trackingValues.size(); i++) {
                final TrackingValues v = trackingValues.get(i);
                final int id = v.getCoordinateSystemID();
                final boolean trackingState = v.isTrackingState();

                MetaioDebug.log("[onTrackingEvent] COS ID = " + id + "; TRACKING = " + trackingState);

                if (trackingState) {
                    setCurrentCOSVisible(id, true);
                    oneCosVisible = true;
                    if (mFirstTrackingEvent) {
                        mFirstTrackingEvent = false;
                    }
                }
            }

            if (!oneCosVisible) {
                setCurrentCOSVisible(mActiveCOS, false);
            }
        }
    }

    private void resetTempTrackingConfig() {
        MetaioDebug.log("[resetTempTrackingConfig]");
        // re-create config, write to file and load it
        String config = mTrackingConfigCreator.createConfig();

        File tempConfigFile = FileHelpers.createLocalDataFile(TRACKING_CONFIG_TEMP_PATH, true);
        FileHelpers.writeToFile(config, tempConfigFile);

        boolean result = metaioSDK.setTrackingConfiguration(tempConfigFile);
        MetaioDebug.log("[resetTempTrackingConfig] Tracking data loaded: " + result);
    }

    private void setCurrentCOSVisible(final int cosID, final boolean visible) {
        mActiveCOS = cosID;
        if (visible) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    setLightingCOS(cosID);
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMarkerManagementFragment != null) {
                    // If cosID = 1 (the case of main object) , fragment(object list) is hidden. Changed by Kenji
                    mMarkerManagementFragment.setCOSVisibility(visible, cosID);

                    View btnAddMarker = findViewById(R.id.add_marker_button);
                    btnAddMarker.setVisibility(visible ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        MetaioDebug.log(TAG + "[onTouch][mGestureHandler]");
        mGestureHandler.onTouch(v, event);
        MetaioDebug.log(TAG + "[onTouch][super]");
        return super.onTouch(v, event);
    }

    private void restartActivity() {
        MetaioDebug.log("[restartActivity]");
        clearDir(TEMP_FOLDER_PATH);
        clearDir(SAVE_FOLDER_PATH);
        finish();
        startActivity(getIntent());
    }

    public void addMarker() {
        MetaioDebug.log("[addMarker]");
        final int newCOSID = mTrackingConfigCreator.getTotalCOSes() + 1;
        mMarkerToReplace = -1;  // indicate that a new marker is to be added
        final String tempMarkerPath = TEMP_FOLDER_PATH + TrackingConfigCreatorMultiMarker.createMarkerFilename(newCOSID);
        File imageFile = FileHelpers.createLocalDataFile(tempMarkerPath, true);
        MetaioDebug.log("[addMarker] imageFile created = " + imageFile);
        metaioSDK.requestCameraImage(imageFile);

        // If newCOSID == 1, main object will be added automatically.
        if (newCOSID == 1) {
            MetaioDebug.log("Main object is added. newCOSID = " + newCOSID);

            mActiveCOS = 1;
            final int mainModelNumber = mModelList.getModelList().size();
            final ARModel model = new ARModel(mModelList.getModelList().get(mainModelNumber - 1));
            final ARViewHelper arViewHelper = this;
            if (model != null) {
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        model.getARGeomData().setCOSID(mActiveCOS);
                        IGeometry geom = model.getARGeomData().loadGeometry(arViewHelper);
                        if (geom != null) {
                            geom.setVisible(true);
                            mSceneGeoms.add(geom);
                            mSceneModels.add(model);
                            mGestureHandler.addObject(geom, mGestureHandlerGeomCounter++);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void removeMarker() {
        MetaioDebug.log("[removeMarker]");
        final int activeCOS = mActiveCOS;
        final String markerFilename = mTrackingConfigCreator.removeCOS(activeCOS);

        if (mTrackingConfigCreator.getTotalCOSes() == 0) {
            restartActivity();
        }

        // delete unused marker
        deleteUnusedMarker(markerFilename);

        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                removeCOSModels(activeCOS);
                resetTempTrackingConfig();
            }
        });
    }

    @Override
    public void addModel() {
        MetaioDebug.log(TAG + "[onInsertModelButton]");
        showModelPicker();
    }

    @Override
    public void removeModel() {
        MetaioDebug.log(TAG + "[onDeleteModelButton]");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(SceneCreationActivity.this)
                        .setTitle("Remove Model")
                        .setMessage("Are you sure you want to delete the last manipulated model?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeModel(mLastTouchedGeom);
                                mLastTouchedGeom = null;
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }

    private void deleteUnusedMarker(final String markerFilename) {
        final String markerPath = TEMP_FOLDER_PATH + markerFilename;
        FileHelpers.deleteLocalDataFile(markerPath);
    }

    /*
    @Override
    public void recaptureMarker()
    {
        MetaioDebug.log("[recaptureMarker]");
        mMarkerToReplace = mActiveCOS;
        final String tempMarkerPath = TEMP_FOLDER_PATH + TrackingConfigCreatorMultiMarker.createMarkerFilename(mMarkerToReplace);
        File imageFile = FileHelpers.createLocalDataFile(tempMarkerPath, true);
        MetaioDebug.log("[addMarker] imageFile created = " + imageFile);
        metaioSDK.requestCameraImage(imageFile);
    }
    */

    private void showModelPicker() {
        MetaioDebug.log(TAG + "[showModelPicker]");
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(ModelPickerFragment.newInstance(), ModelPickerFragment.TAG);
        ft.commit();

    }

    private void removeModelPicker() {
        MetaioDebug.log(TAG + "[removeModelPicker]");
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        final Fragment fragment = fm.findFragmentByTag(ModelPickerFragment.TAG);
        ft.remove(fragment);
        ft.commit();
    }

    @Override
    public void selectModel(int modelNumber) {
        MetaioDebug.log(TAG + "[selectModel] model id = " + modelNumber);
        removeModelPicker();
        final ARModel model = new ARModel(mModelList.getModelList().get(modelNumber));
        final ARViewHelper arViewHelper = this;
        if (model != null) {
            mSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    model.getARGeomData().setCOSID(mActiveCOS);
                    IGeometry geom = model.getARGeomData().loadGeometry(arViewHelper);
                    if (geom != null) {
                        geom.setVisible(true);

                        /////////////////////////////////////////
                        MetaioDebug.log("$$$$$$$$$$$$$$$$ = " + geom);
                        /////////////////////////////////////////

                        mSceneGeoms.add(geom);
                        mSceneModels.add(model);
                        mGestureHandler.addObject(geom, mGestureHandlerGeomCounter++);
                    }
                }
            });
        }
    }

    private void removeCOSModels(int cos) {
        MetaioDebug.log(TAG + "[removeCOSModels]");
        List<IGeometry> geomsToRemove = new ArrayList<>();
        for (int i = 0; i < mSceneGeoms.size(); i++) {
            IGeometry currentGeom = mSceneGeoms.get(i);
            final int currentCOSid = currentGeom.getCoordinateSystemID();
            if (currentCOSid == cos) {
                geomsToRemove.add(currentGeom);
            } else if (currentCOSid > cos) {
                currentGeom.setCoordinateSystemID(currentCOSid - 1);
                mSceneModels.get(i).getARGeomData().setCOSID(currentCOSid - 1);
            }
        }

        for (int i = 0; i < geomsToRemove.size(); i++) {
            MetaioDebug.log(TAG + "[removeCOSModels] model id = " + i);
            removeModel(geomsToRemove.get(i));
        }
    }

    private void removeModel(final IGeometry geomToRemove) {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (geomToRemove == null) {
                    return;
                }

                int id = -1;
                for (int i = 0; i < mSceneGeoms.size(); i++) {
                    if (mSceneGeoms.get(i).equals(geomToRemove)) {
                        id = i;
                        break;
                    }
                }

                if (id == -1) {
                    return;
                }

                final int idToRemove = id;

                if (idToRemove != 0) {
                    MetaioDebug.log(TAG + "[removeModel] model id = " + id);
                    mSceneGeoms.remove(idToRemove);
                    mSceneModels.remove(idToRemove);
                    metaioSDK.unloadGeometry(geomToRemove);
                    mGestureHandler.removeObject(geomToRemove);
                }
                else {
                    showToast("The main model can't be removed from the first scene.");
                }

            }
        });
    }

    @Override
    public UIModelList getModelList() {
        return mUIModelList;
    }

    class FetchRtmpURL extends AsyncTask<String, String, String> {

        String targetURL;
        String urlParameters;
        File sceneFolder;

        public FetchRtmpURL(String targetURL, String urlParameters, File sceneFolder) {
            this.targetURL = targetURL;
            this.urlParameters = urlParameters;
            this.sceneFolder = sceneFolder;
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
            //showToast("Successfully registered.");

            //  4. upload scene folder
            Runnable success = new Runnable() {
                @Override
                public void run() {
                    showToast("Scene uploaded successfully.");
                    clearDir(SAVE_FOLDER_PATH);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);

                    ar.callbackContextKeepCallback.success("success");
                }
            };

            Runnable failure = new Runnable() {
                @Override
                public void run() {
                    showToast("Error uploading scene!\n\nPlease check your Internet connectivity.");
                    FileHelpers.deleteRecursive(sceneFolder);
                    Intent intent = new Intent(getApplicationContext(), SceneCreationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            };

            S3FolderUploadAsyncTask uploader = new S3FolderUploadAsyncTask(getApplicationContext(),
                    SceneCreationActivity.this, "Saved scene uploading",
                    success, failure);

            final String bucketName = getResources().getString(R.string.aws_s3_bucket);
            uploader.execute("AKIAIBWTHFHR4RRBFGTA", "5gpPz9iBuDbZoAAMOCRtIQnV58P4fxB/tv7UjXip", bucketName, sceneFolder.getAbsolutePath());

        }
    }
}
