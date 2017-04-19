package com.zumoko.metaiohelper;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class AudioPlaybackService extends Service
{
    private final String tag = "[AudioPlaybackService]";

    private MediaPlayer mp;

    public AudioPlaybackService()
    {
        super();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public void onCreate()
    {
        if (mp == null)
        {
            mp = new MediaPlayer();
            mp.setLooping(false);
        }
        else if (mp.isPlaying())
        {
            mp.stop();
            mp.release();
            mp = new MediaPlayer();
            mp.setLooping(false);
        }
    }
    public void onDestroy()
    {
        mp.stop();
        mp.release();
        mp = null;
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(tag, "[onStartCommand]");

        try
        {
            if (mp == null)
            {
                Log.d(tag, "[onStartCommand] Create new player.");
                mp = new MediaPlayer();
                mp.setLooping(false);
            }
            else if (mp.isPlaying())
            {

                mp.stop();
//Option 1
                Log.d(tag, "[onStartCommand] Re-create the player.");
                mp.release();
                mp = new MediaPlayer();
            }

            mp.reset();

            String filePath = intent.getStringExtra("path");
            boolean isAbsolutePath = intent.getBooleanExtra("absPath", false);
            boolean isLooping = intent.getBooleanExtra("loop", false);

            AssetFileDescriptor afd;
            try
            {
                if (!isAbsolutePath)
                {
                    afd = getAssets().openFd(filePath);
                    Log.d(tag, "[onStartCommand] Set file data: " + filePath);
                    mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                }
                else
                {
                    Log.d(tag, "[onStartCommand] Set file data: " + filePath);
                    mp.setDataSource(filePath);
                }
            }
            catch (IOException e)
            {
                Log.d(tag, "[onStartCommand] Error reading file data: " + filePath);
                e.printStackTrace();
                Log.d(tag, "[onStartCommand] Exit without playing audio.");
                return START_NOT_STICKY;
            }

            Log.d(tag, "[onStartCommand] prepare()");
            mp.prepare();
            Log.d(tag, "[onStartCommand] setLooping()");
            mp.setLooping(isLooping);
//            Log.d(tag, "[onStartCommand] setVolume()");
//            final double maxVolume = 10.0;
//            final double currVolume = 3.5;
//            final float logVolume = 1.f - (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
//            mp.setVolume(logVolume, logVolume);
            Log.d(tag, "[onStartCommand] Start audio playback.");
            mp.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return START_NOT_STICKY;
        }

        return START_NOT_STICKY;
    }
}
