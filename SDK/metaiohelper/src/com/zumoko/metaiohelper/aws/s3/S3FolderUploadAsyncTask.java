package com.zumoko.metaiohelper.aws.s3;

import android.app.Activity;
import android.content.Context;

import java.io.File;

/**
 * Created by darsta on 05-Apr-16.
 */
public class S3FolderUploadAsyncTask extends S3AsyncTask
{
    protected S3Adapter mS3a;

    public S3FolderUploadAsyncTask(Context appContext, Activity caller, String progressDialogTitle,
                                            Runnable runOnSuccess, Runnable runOnFailure)
    {
        super(appContext, caller, progressDialogTitle, runOnSuccess, runOnFailure);
    }

    /**
     *
     * @param params access_key, secret_access_key, bucket_name, internal_folder_absolute_path
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

            File internalFolder = new File(params[3]);
            if ( (!internalFolder.exists()) || (!internalFolder.isDirectory()) )
            {
                return false;
            }

            return mS3a.uploadInternalFolder(internalFolder, params[2], mProgressNotifier);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
