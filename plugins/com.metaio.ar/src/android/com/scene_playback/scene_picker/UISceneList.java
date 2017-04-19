package com.metaio.Template.scene_playback.scene_picker;

import com.zumoko.metaiohelper.scene_general.ARScene;
import com.zumoko.metaiohelper.scene_general.ARSceneList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darsta on 01-Apr-16.
 */
public class UISceneList
{
    public static class UIScene
    {
        private String mName;
        private String mDateTime;

        UIScene(String name, String dateTime)
        {
            mName = name;
            mDateTime = dateTime;
        }

        public String getName()
        {
            return mName;
        }

        public String getDateTime()
        {
            return mDateTime;
        }
    }

    private List<UIScene> mSceneList = new ArrayList<>();

    private UISceneList()
    {

    }

    public UISceneList(ARSceneList arSceneList)
    {
        for (ARScene arScene : arSceneList.getSceneList())
        {
            UIScene model = new UIScene(arScene.getName(), arScene.getTimeCreated());
            mSceneList.add(model);
        }
    }

    public List<UIScene> getSceneList()
    {
        return mSceneList;
    }
}
