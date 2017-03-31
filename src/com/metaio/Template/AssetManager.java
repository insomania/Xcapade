package com.metaio.Template;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xcapade.BuildConfig;
import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;
import com.xcapade.R;

public class AssetManager extends Activity
{
	private AssetsExtracter mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		MetaioDebug.enableLogging(BuildConfig.DEBUG);

		mTask = new AssetsExtracter();
		mTask.execute(0);
	}

	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
		}

		@Override
		protected Boolean doInBackground(Integer... params)
		{
			try
			{
				AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result)
			{
				goToNextActivity();
			}
			else
			{
				Toast toast = Toast.makeText(getApplicationContext(), "Error extracting application assets!", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}

			finish();
	    }
	}

	private void goToNextActivity()
	{
		setProgressCircleVis(View.GONE);
		Intent intent = new Intent(getApplicationContext(), S3UpdateActivity.class);
		startActivity(intent);
	}

	private void setProgressCircleVis(final int vis)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
			View pc = findViewById(R.id.progress_circle);
			if (pc!=null)
			{
				pc.setVisibility(vis);
			}
			}
		});
	}
}

