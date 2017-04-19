package com.zumoko.metaiohelper.scene_general;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDK;
import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.geometry.ARGeometryData;
import com.zumoko.metaiohelper.geometry.ARViewHelper;
import com.zumoko.metaiohelper.scene_general.xml.ARSceneXML;
import com.zumoko.metaiohelper.xml.XMLParserHelper;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darsta on 04-Apr-16.
 */
public class ARScene
{
    ARSceneXML mXML;
    File mTrackingData;
    List<ARGeometryData> mARGeomDataList = new ArrayList<>();
    List<IGeometry> mGeoms = new ArrayList<>();
    String mTrackingDataFileParentFolderPath;

    private ARScene()
    {

    }

    public static ARScene createFromFile(File xmlFile, ARViewHelper helper)
    {
        Node arSceneXMLNode = XMLParserHelper.getRootNode(xmlFile.getAbsolutePath(), "ARScene");
        ARSceneXML arSceneXML = ARSceneXML.parseXML(arSceneXMLNode);
        return createFromXMLData(xmlFile.getParent(), arSceneXML, helper);
    }

    public static ARScene createFromXMLData(String folderAbsolutePath, ARSceneXML xmlData, ARViewHelper helper)
    {
        ARScene arScene = new ARScene();

        arScene.mXML = xmlData;

        folderAbsolutePath = FileHelpers.setFolderDelimiters(folderAbsolutePath);

        File parentFolder = new File(folderAbsolutePath);
        if ( (!parentFolder.exists()) && (!parentFolder.isDirectory()) )
        {
            return null;
        }

        final String parentFolderPath = parentFolder.getAbsolutePath();
        arScene.mTrackingDataFileParentFolderPath = parentFolderPath;

        String trackingFilePath = parentFolderPath + "/" + xmlData.getTrackingFileRelativePath();
        File trackingFile = new File(trackingFilePath);
        if (!trackingFile.exists())
        {
            return null;
        }

        arScene.mTrackingData = trackingFile;

        for (String arGeomPath : xmlData.getARGeomDataPathList())
        {
            final String absARGeomPath = parentFolderPath + "/" + arGeomPath;
            final File absARGeomFile = new File(absARGeomPath);
            if (!absARGeomFile.exists())
            {
                return null;
            }

            String arGeomParentFolderPath = absARGeomFile.getParent();
            arGeomParentFolderPath = FileHelpers.setFolderDelimiters(arGeomParentFolderPath);

            Node node = XMLParserHelper.getRootNode(absARGeomPath, "ARGeometry");
            ARGeometryData arData = ARGeometryData.parseXMLData(node);

            if (arData==null)
            {
                return null;
            }
            else
            {
                String absModelPath = arGeomParentFolderPath + arData.getFilename();
                arData.setFilename(absModelPath);
                arScene.mARGeomDataList.add(arData);
            }
        }

        return arScene.mARGeomDataList.size()>0 ? arScene : null;
    }

    public boolean loadGeometries(ARViewHelper helper)
    {
        if (mGeoms.size()>0)
        {
            return false;
        }

        for (ARGeometryData arData : mARGeomDataList)
        {
            IGeometry geom = arData.loadGeometry(helper);
            if (geom==null)
            {
                return false;
            }
            else
            {
                mGeoms.add(geom);
            }
        }

        return true;
    }

    public void unloadGeometries(IMetaioSDK metaioSDK)
    {
        for (IGeometry geom : mGeoms)
        {
            metaioSDK.unloadGeometry(geom);
        }
        mGeoms.clear();
    }

    public boolean loadTrackingConfig(IMetaioSDK metaioSDK)
    {
        File trackingConfig = getTrackingData();
        boolean result = false;
        if (trackingConfig.exists())
        {
            MetaioDebug.log("[loadTrackingConfig] Set saved tracking config");
            result = metaioSDK.setTrackingConfiguration(trackingConfig);
            MetaioDebug.log("[loadTrackingConfig] Tracking data loaded: " + result);
        }
        return result;
    }

    public String getName()
    {
        return mXML.getName();
    }

    public double getLat()
    {
        return mXML.getLat();
    }

    public double getLong()
    {
        return mXML.getLong();
    }

    public String getTimeCreated()
    {
        return mXML.getTimeCreated();
    }

    public File getTrackingData()
    {
        return mTrackingData;
    }

    public List<IGeometry> getGeometries(){ return mGeoms; }

    public String getMarkerImagesPath ()
    {
        Node arSceneXMLNode = XMLParserHelper.getRootNode(mTrackingData.getAbsolutePath(), "ReferenceImage");
        String firstMarkerImagepath = arSceneXMLNode.getFirstChild().getNodeValue();
        return mTrackingDataFileParentFolderPath + "/" + firstMarkerImagepath;
    }
}