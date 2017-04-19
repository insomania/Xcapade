package com.metaio.Template.scene_creation.model_picker;

import android.graphics.Bitmap;

import com.zumoko.metaiohelper.scene_general.ARModel;
import com.zumoko.metaiohelper.scene_general.ARModelList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darsta on 01-Apr-16.
 */
public class UIModelList
{
    public static class UIModel
    {
        private Bitmap mIcon;
        private String mName;

        UIModel(Bitmap icon, String name)
        {
            mIcon = icon;
            mName = name;
        }

        public Bitmap getIcon()
        {
            return mIcon;
        }

        public String getName()
        {
            return mName;
        }
    }

    private List<UIModel> mModelList = new ArrayList<>();

    public UIModelList(ARModelList arModelList)
    {
        for (ARModel arModel : arModelList.getModelList())
        {
            if(arModel.getName().equals("Xcapade Cube"))
            {
                continue;
            }
            else
            {
                UIModel model = new UIModel(arModel.getIconBitmap(), arModel.getName());
                mModelList.add(model);
            }
        }
    }

    public List<UIModel> getModelList()
    {
        return mModelList;
    }
}
