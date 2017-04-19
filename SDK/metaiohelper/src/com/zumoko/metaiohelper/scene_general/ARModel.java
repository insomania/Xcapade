package com.zumoko.metaiohelper.scene_general;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.geometry.ARGeometryData;
import com.zumoko.metaiohelper.xml.XMLParserHelper;

import org.w3c.dom.Node;

import java.io.File;

/**
 * Created by darsta on 01-Apr-16.
 *
 * Model folder contains:
 *  1.  object.xml - ARGeomData; model name is only referenced by its file name in the same folder
 *  2.  icon.png - model icon file
 *  3.  actual model files referenced in the XML
 */
public class ARModel
{
    private static String GEOM_DATA_FILENAME = "object.xml";
    private static String ICON_FILENAME = "icon.png";

    private String mAbsoluteFolderPath = "";
    private ARGeometryData mARGeomData;
    private Bitmap mIconBitmap;

    private ARModel()
    {

    }

    public ARModel(final ARModel toCopy)
    {
        mAbsoluteFolderPath = toCopy.getAbsoluteFolderPath();
        mARGeomData = new ARGeometryData(toCopy.getARGeomData());
        mIconBitmap = toCopy.getIconBitmap();
    }

    public static ARModel createFromFolder(String absoluteFolderPath)
    {
        //  Agenda:
        //      1. check if object.xml exists, and load it
        //      2. update file path in ARGeomData
        //      3. check if the model from the file path exists
        //      4. create the Bitmap from the icon file icon.png

        ARModel model = new ARModel();

        absoluteFolderPath = FileHelpers.setFolderDelimiters(absoluteFolderPath);

        model.mAbsoluteFolderPath = absoluteFolderPath;

        String xmlPath = absoluteFolderPath + GEOM_DATA_FILENAME;
        Node node = XMLParserHelper.getRootNode(xmlPath, "ARGeometry");
        if (node==null)
        {
            return null;
        }

        model.mARGeomData = ARGeometryData.parseXMLData(node);
        if (model.mARGeomData == null)
        {
            return null;
        }

        String fName = model.mARGeomData.getFilename();
        fName = absoluteFolderPath + fName;
        model.mARGeomData.setFilename(fName);

        File modelFile = new File(model.mARGeomData.getFilename());
        if (!modelFile.exists())
        {
            return null;
        }

        final String iconPath = absoluteFolderPath + ICON_FILENAME;
        model.mIconBitmap = BitmapFactory.decodeFile(iconPath);
        if (model.mIconBitmap == null)
        {
            return null;
        }

        return model;
    }

    public void setRelativePaths()
    {
        String fName = mARGeomData.getFilename();
        String absoluteFolderPath = FileHelpers.setFolderDelimiters(mAbsoluteFolderPath);
        if (fName.startsWith(mAbsoluteFolderPath))
        {
            fName = fName.substring(absoluteFolderPath.length());
        }
        mARGeomData.setFilename(fName);
    }

    public ARGeometryData getARGeomData()
    {
        return mARGeomData;
    }

    public String getName()
    {
        return mARGeomData.getName();
    }

    public Bitmap getIconBitmap()
    {
        return mIconBitmap;
    }

    public String getAbsoluteFolderPath()
    {
        return mAbsoluteFolderPath;
    }

    public void setAbsoluteFolderPath(final String absoluteFolderPath)
    {
        mAbsoluteFolderPath = absoluteFolderPath;
    }

    public static String getGeomDataFilename()
    {
        return GEOM_DATA_FILENAME;
    }
}
