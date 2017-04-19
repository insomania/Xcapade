package com.zumoko.metaiohelper.aws.s3;

import android.app.Activity;
import android.content.Context;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.List;

/**
 * Created by darsta on 31-Mar-16.
 */
public class S3InternalFolderUpdaterAsyncTask extends S3AsyncTask
{
    protected S3Adapter mS3a;

    public S3InternalFolderUpdaterAsyncTask(Context appContext, Activity caller, String progressDialogTitle,
                       Runnable runOnSuccess, Runnable runOnFailure)
    {
        super(appContext, caller, progressDialogTitle, runOnSuccess, runOnFailure);
    }

    /**
     *
     * @param params access_key, secret_access_key, bucket_name, folder_name
     * @return
     */
    @Override
    protected Boolean doInBackground(String... params)
    {
        try
        {
            // Agenda
            //  1. get all objects in a bucket
            //  2. get the list of objects that don't exist in the internal memory
            //  3. download this list of objects, with updating the progress

            mS3a = new S3Adapter(mAppContext, params[0], params[1]);
            List<S3ObjectSummary> allObjects = mS3a.listAllObjects(params[2], params[3]);
            if (allObjects==null)
            {
                return false;
            }

            return mS3a.syncFilesInInternalMemory(params[3], allObjects, mProgressNotifier);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
