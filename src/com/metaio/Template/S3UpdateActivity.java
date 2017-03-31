package com.metaio.Template;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

import com.metaio.Template.scene_playback.ScenePlaybackActivity;
import com.xcapade.MainActivity;
import com.zumoko.metaiohelper.aws.s3.S3InternalFolderUpdaterAsyncTask;
import com.zumoko.metaiohelper.scene_general.ARModelList;
import com.zumoko.metaiohelper.scene_general.ARSceneList;

import com.xcapade.R;

public class S3UpdateActivity extends Activity
{
    private String mBucketName;
    private Runnable mFailureAction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s3_update);

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

        updateModels();
    }

    private void updateModels()
    {
        Runnable successModels = new Runnable()
        {
            @Override
            public void run()
            {
                updateScenes();
            }
        };

        S3InternalFolderUpdaterAsyncTask modelsDownloader = new S3InternalFolderUpdaterAsyncTask(getApplicationContext(),
                S3UpdateActivity.this, "Application Updating : Models",
                successModels, mFailureAction);
        modelsDownloader.execute("AKIAIBWTHFHR4RRBFGTA", "5gpPz9iBuDbZoAAMOCRtIQnV58P4fxB/tv7UjXip", mBucketName, ARModelList.getDefaultModelsFolder());
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
        scenesDownloader.execute("AKIAIBWTHFHR4RRBFGTA", "5gpPz9iBuDbZoAAMOCRtIQnV58P4fxB/tv7UjXip", mBucketName, ARSceneList.getDefaultScenesFolder());
    }

    private void startNextActivity()
    {
        //Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        //startActivity(intent);

        if(ar.actionStr.equals("preload")) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            ar.callbackContextKeepCallback.success("success");
        }
        else if (ar.actionStr.equals("playback")) {
            String[] spotData = ar.spotData;

            Intent intent = new Intent(getApplicationContext(), ScenePlaybackActivity.class);
            intent.putExtra("spotData", spotData);
            startActivity(intent);
        }
        /* else if (ar.actionStr.equals("creation")) {
            String[] spotData = ar.spotData;

            Intent intent = new Intent(getApplicationContext(), SceneCreationActivity.class);
            intent.putExtra("spotData", spotData);
            startActivity(intent);
        } */
        else{
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
    }
}
