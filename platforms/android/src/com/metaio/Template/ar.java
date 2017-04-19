package com.metaio.Template;

import android.content.Intent;

import com.metaio.Template.scene_creation.SceneCreationActivity;
import com.metaio.Template.scene_playback.ScenePlaybackActivity;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class ar extends CordovaPlugin {

    public static String actionStr = "";
    public static String[] spotData = new String[9];
    public static CallbackContext callbackContextKeepCallback;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        callbackContextKeepCallback = callbackContext;

        Intent intent;

        switch(action) {
            case "playback":
                actionStr = "playback";

                spotData[0] = data.getString(0);
                spotData[1] = data.getString(1);
                spotData[2] = data.getString(2);
                spotData[3] = data.getString(3);
                spotData[4] = data.getString(4);
                spotData[5] = data.getString(5);
                spotData[6] = data.getString(6); // AWS_ACCESS_KEY_ID
                spotData[7] = data.getString(7); // AWS_SECRET_ACCESS_KEY
                spotData[8] = data.getString(8); // AWS Scene Folder Name
                actionStr = "playback";

                intent = new Intent(cordova.getActivity().getApplicationContext(), AssetManager.class);
                cordova.getActivity().startActivity(intent);
                /*
                Intent intent = new Intent(cordova.getActivity().getApplicationContext(), ScenePlaybackActivity.class);
                cordova.getActivity().startActivity(intent);
                callbackContext.success("success");
                */

                break;
            case "creation":
                actionStr = "creation";

                spotData[0] = data.getString(0);
                spotData[1] = data.getString(1);
                spotData[2] = data.getString(2);
                spotData[3] = data.getString(3); // AWS_ACCESS_KEY_ID
                spotData[4] = data.getString(4); // AWS_SECRET_ACCESS_KEY
                actionStr = "creation";
                /*
                Intent intent = new Intent(cordova.getActivity().getApplicationContext(), SceneCreationActivity.class);
                intent.putExtra("spotData", spotData);
                cordova.getActivity().startActivity(intent);
                */
                intent = new Intent(cordova.getActivity().getApplicationContext(), AssetManager.class);
                intent.putExtra("spotData", spotData);
                cordova.getActivity().startActivity(intent);

                break;
            default:
                actionStr = "preload";

                intent = new Intent(cordova.getActivity().getApplicationContext(), AssetManager.class);
                cordova.getActivity().startActivity(intent);
                // callbackContext.success("success");

        }

        return true;

    }
}
