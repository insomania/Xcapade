package com.metaio.Template.scene_playback.scene_picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.superman.capade.R;

/**
 * Created by darsta on 01-Apr-16.
 */
public class SceneListViewAdapter extends ArrayAdapter<UISceneList.UIScene>
{
    public SceneListViewAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public SceneListViewAdapter(Context context, int resource, UISceneList uiModelList) {
        super(context, resource, uiModelList.getSceneList());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.scene_picker_list_row, null);
        }

        UISceneList.UIScene p = getItem(position);

        if (p != null)
        {
            TextView name = (TextView) v.findViewById(R.id.sceneName);

            if (name != null)
            {
                name.setText(p.getName());
            }

            TextView dateTime = (TextView) v.findViewById(R.id.sceneDateTime);

            if (dateTime != null)
            {
                dateTime.setText(p.getDateTime());
            }

        }

        return v;
    }
}
