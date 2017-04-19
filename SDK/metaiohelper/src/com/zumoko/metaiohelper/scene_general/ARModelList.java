package com.zumoko.metaiohelper.scene_general;

import com.zumoko.metaiohelper.file.FileHelpers;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by darsta on 01-Apr-16.
 *
 * Parses list of models from the internal folder structure
 */
public class ARModelList
{
    private static String DEFAULT_MODELS_FOLDER = "models";

    private List<ARModel> mModelList = new ArrayList<>();

    private ARModelList()
    {

    }

    public static ARModelList createFromFolder(String absoluteRootFolderPath)
    {
        //  Agenda
        //  1.  get the list of subfolders
        //  2.  create model from each sub-folder

        ARModelList modelList = new ARModelList();

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

        for (File sf : subfolderList)
        {
            ARModel model = ARModel.createFromFolder(sf.getAbsolutePath());
            if (model!=null)
            {
                modelList.mModelList.add(model);
            }
        }

        return modelList;
    }

    public List<ARModel> getModelList()
    {
        return mModelList;
    }

    public static List<ARModel> deepCopySceneModels(List<ARModel> inputModels)
    {
        List<ARModel> listCopy = new ArrayList<>();
        for (ARModel arModel : inputModels)
        {
            ARModel modelCopy = new ARModel(arModel);
            listCopy.add(modelCopy);
        }

        return listCopy;
    }

    public static String getDefaultModelsFolder()
    {
        return DEFAULT_MODELS_FOLDER;
    }
}