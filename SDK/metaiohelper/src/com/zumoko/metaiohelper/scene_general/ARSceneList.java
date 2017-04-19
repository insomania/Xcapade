package com.zumoko.metaiohelper.scene_general;

import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.geometry.ARViewHelper;
import com.zumoko.metaiohelper.scene_general.xml.ARSceneXML;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by darsta on 06-Apr-16.
 */
public class ARSceneList
{
    private static String DEFAULT_SCENES_FOLDER = "scenes";

    List<ARScene> mSceneList = new ArrayList<>();

    public static ARSceneList createFromFolder(String absoluteRootFolderPath, ARViewHelper helper)
    {
        //  Agenda
        //  1.  get the list of subfolders
        //  2.  create model from each sub-folder

        ARSceneList sceneList = new ARSceneList();

        absoluteRootFolderPath = FileHelpers.setFolderDelimiters(absoluteRootFolderPath);

        File rootFolder = new File(absoluteRootFolderPath);
        if ( (!rootFolder.exists()) || (!rootFolder.isDirectory()))
        {
            return null;
        }

        final File[] subfolders = rootFolder.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(final File pathname)
            {
                return pathname.isDirectory();
            }
        });

        final List<File> subfolderList = new ArrayList<>(Arrays.asList(subfolders));

        // sort folder list
        Comparator<File> comparator = new Comparator<File>()
        {
            @Override
            public int compare(final File f1, final File f2){
                // let your comparator look up your car's color in the custom order
                return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
            }
        };
        Collections.sort(subfolderList, comparator);

        for (File sf : subfolderList)
        {
            File xmlFile = new File(sf, ARSceneXML.getDefaultSceneXmlFilename());
            ARScene scene = ARScene.createFromFile(xmlFile, helper);
            if (scene!=null)
            {
                sceneList.mSceneList.add(scene);
            }
        }

        return sceneList;
    }

    public List<ARScene> getSceneList()
    {
        return mSceneList;
    }

    public static String getDefaultScenesFolder()
    {
        return DEFAULT_SCENES_FOLDER;
    }
}
