package com.metaio.Template.scene_creation.model_picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.superman.capade.R;

/**
 * Created by darsta on 01-Apr-16.
 */
public class ListViewAdapter extends ArrayAdapter<UIModelList.UIModel>
{
    public ListViewAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListViewAdapter(Context context, int resource, UIModelList uiModelList) {
        super(context, resource, uiModelList.getModelList());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.model_picker_list_row, null);
        }

        UIModelList.UIModel p = getItem(position);

        if (p != null)
        {
            TextView name= (TextView) v.findViewById(R.id.modelName);

            if (name != null) {
                name.setText(p.getName());
            }

            ImageView icon = (ImageView) v.findViewById(R.id.modelIcon);

            if (icon != null)
            {
                icon.setImageBitmap(p.getIcon());
            }
        }

        return v;
    }
}
