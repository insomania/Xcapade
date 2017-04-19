package com.zumoko.metaiohelper.aws.s3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.WindowManager;

import com.metaio.sdk.MetaioDebug;

/**
 * Created by darsta on 31-Mar-16.
 */
public abstract class S3AsyncTask extends AsyncTask<String, Integer, Boolean>
{
    protected Context mAppContext;
    protected Activity mCallerActivity;
    protected Runnable mRunnableSuccess;
    protected Runnable mRunnableFailure;
    protected String mProgressDialogTitle;
    protected ProgressDialog mProgressDialog;

    public S3AsyncTask(Context appContext, Activity caller, String progressDialogTitle,
                                            Runnable runOnSuccess, Runnable runOnFailure)
    {
        mAppContext = appContext;
        mCallerActivity = caller;
        mProgressDialogTitle = progressDialogTitle;
        mRunnableFailure = runOnFailure;
        mRunnableSuccess = runOnSuccess;
    }

    class MyProgressNotifier extends S3Adapter.ProgressNotifier
    {
        protected void setTotalData(long total)
        {
            onProgressUpdate(-1, (int)total);
        }
        protected void notifyTotalProgress(long addedProgress)
        {
            onProgressUpdate(1, (int)addedProgress);
        }
    }

    MyProgressNotifier mProgressNotifier = new MyProgressNotifier();

    @Override
    protected void onPreExecute()
    {
        mProgressDialog = new ProgressDialog(mCallerActivity);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(mProgressDialogTitle);
        mProgressDialog.show();

        // position window
        mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
        params.y = 300;
        mProgressDialog.getWindow().setAttributes(params);
    }

    @Override
    protected void onProgressUpdate(Integer... progress)
    {
        if (progress[0]==-1)
        {
            mProgressDialog.setMax(progress[1]);
        }
        else
        {
            mProgressDialog.setProgress(progress[1]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        mProgressDialog.setProgress(mProgressDialog.getMax());
        mProgressDialog.dismiss();
        if (result)
        {
            MetaioDebug.log("[S3AsyncTask] Finished with success!");
            mRunnableSuccess.run();
        }
        else
        {
            MetaioDebug.log("[S3AsyncTask] Finished with FAILURE!");
            mRunnableFailure.run();
        }
    }
}
