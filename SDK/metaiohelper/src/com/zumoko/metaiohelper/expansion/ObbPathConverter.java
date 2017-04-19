package com.zumoko.metaiohelper.expansion;

import android.content.Context;

import java.io.File;

/**
 * Created by darsta on 19-Sep-15.
 */
public abstract class ObbPathConverter
{
    private static String TAG = "[ObbPathConverter]";

    public static File getUnzippedObbParentFolderPath(Context appContext)
    {
        return appContext.getExternalFilesDir(null);
    }

    public static String getPathInUnzippedObb(Context appContext, String relativePath)
    {
        final String parentFolder = ObbPathConverter.getUnzippedObbParentFolderPath(appContext).getAbsolutePath();
        return parentFolder + File.separator + relativePath;
    }
}
