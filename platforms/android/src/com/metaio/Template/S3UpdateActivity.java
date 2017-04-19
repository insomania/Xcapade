package com.metaio.Template;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

import com.metaio.Template.scene_creation.SceneCreationActivity;
import com.metaio.Template.scene_playback.ScenePlaybackActivity;
import com.superman.capade.MainActivity;
import com.zumoko.metaiohelper.aws.s3.S3InternalFolderUpdaterAsyncTask;
import com.zumoko.metaiohelper.scene_general.ARModelList;
import com.zumoko.metaiohelper.scene_general.ARSceneList;

import com.superman.capade.R;

public class S3UpdateActivity extends Activity
{
    private String mBucketName;
    private Runnable mFailureAction;
    private String AWS_ACCESS_KEY_ID;
    private String AWS_SECRET_ACCESS_KEY;
    private String AWS_SceneFoler_Name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s3_update);
        if (ar.actionStr.equals("playback")) {
            String[] spotData = ar.spotData;
            AWS_ACCESS_KEY_ID = spotData[6];
            AWS_SECRET_ACCESS_KEY = spotData[7];
            AWS_SceneFoler_Name = spotData[8];
        }
        else {
            String[] spotData = ar.spotData;
            AWS_ACCESS_KEY_ID = spotData[3];
            AWS_SECRET_ACCESS_KEY = spotData[4];
        }
        mBucketName = getResources().getString(R.string.aws_s3_bucket);
        mFailureAction = new Runnable()
        {
            @Override
            public void run()
            {
                Toast toast = Toast.makeText(getApplicationContext(), "Error updating app assets!\n\nCheck your Internet connectivity", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                startNextActivity();
            }
        };

        if(ar.actionStr.equals("playback")) {
            updateScenes();
        }
        else {
            updateModels();
        }
    }

    private void updateModels()
    {
        Runnable successModels = new Runnable()
        {
            @Override
            public void run()
            {
                startNextActivity();
            }
        };

        S3InternalFolderUpdaterAsyncTask modelsDownloader = new S3InternalFolderUpdaterAsyncTask(getApplicationContext(),
                S3UpdateActivity.this, "Application Updating : Models",
                successModels, mFailureAction);
        modelsDownloader.execute(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, mBucketName, ARModelList.getDefaultModelsFolder());
    }

    private void updateScenes()
    {
        Runnable successScenes = new Runnable()
        {
            @Override
            public void run()
            {
                startNextActivity();
            }
        };

        S3InternalFolderUpdaterAsyncTask scenesDownloader = new S3InternalFolderUpdaterAsyncTask(getApplicationContext(),
                S3UpdateActivity.this, "Application Updating : Scenes",
                successScenes, mFailureAction);
        //scenesDownloader.execute(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, mBucketName, ARSceneList.getDefaultScenesFolder());
        String selectSceneFolderName = ARSceneList.getDefaultScenesFolder() + "/" + AWS_SceneFoler_Name;
        scenesDownloader.execute(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, mBucketName, selectSceneFolderName);
    }

    private void startNextActivity()
    {
        //Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        //startActivity(intent);

        Intent intent;
        String[] spotData;

        switch(ar.actionStr) {
            case "preload":
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);

                ar.callbackContextKeepCallback.success("success");
                break;
            case "playback":
                spotData = ar.spotData;

                intent = new Intent(getApplicationContext(), ScenePlaybackActivity.class);
                intent.putExtra("spotData", spotData);
                startActivity(intent);
                break;
            case "creation":
                spotData = ar.spotData;

                intent = new Intent(getApplicationContext(), SceneCreationActivity.class);
                intent.putExtra("spotData", spotData);
                startActivity(intent);
                break;
            default :
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
        }
    }
}
