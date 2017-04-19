package com.metaio.Template;

import android.content.Intent;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class ar extends CordovaPlugin {

    public static String actionStr = "";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("playback")) {

            //String name = data.getString(0);
            //String message = name;
            //callbackContext.success(message);

            actionStr = "playback";

            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), AssetManager.class);
            cordova.getActivity().startActivity(intent);

            return true;
        }
        else if (action.equals("creation")) {

            //String name = data.getString(0);
            //String message = "Hello " + name;
            //callbackContext.success(message);

            actionStr = "creation";

            Intent intent = new Intent(cordova.getActivity().getApplicationContext(), AssetManager.class);
            cordova.getActivity().startActivity(intent);

            return true;
        }
        else{
            return false;
        }
    }
}
