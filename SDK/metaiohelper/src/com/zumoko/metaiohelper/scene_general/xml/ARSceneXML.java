package com.zumoko.metaiohelper.scene_general.xml;

import com.metaio.sdk.jni.IGeometry;
import com.zumoko.metaiohelper.file.FileHelpers;
import com.zumoko.metaiohelper.scene_general.ARModel;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by darsta on 04-Apr-16.
 */
public class ARSceneXML
{
    private static String DEFAULT_SCENE_XML_FILENAME = "scene.xml";
    private static String DEFAULT_SCENE_XCOIN_FILENAME = ".xcoin";
    private int mID = 0;
    private String mName = "";
    private String mDescription = "";
    private String mHint = "";
    private double mLat = 0.0;
    private double mLong = 0.0;
    private String mTimeCreated = "";
    private String mTrackingFileRelativePath = "";
    private List<String> mARGeomDataPathList = new ArrayList<>();

    private ARSceneXML()
    {

    }

    public ARSceneXML(String name, String descr, String hint,
                      double lat, double lngt, String timeCreated,
                      String trackingFileRelativePath, List<String> arGeomDataPathList)
    {
        mName = name;
        mDescription = descr;
        mHint = hint;
        mLat = lat;
        mLong = lngt;
        mTimeCreated = timeCreated;
        mTrackingFileRelativePath = trackingFileRelativePath;
        mARGeomDataPathList = arGeomDataPathList;
    }

    private boolean isValid()
    {
        return ((mARGeomDataPathList.size()>0) && (!mTrackingFileRelativePath.equalsIgnoreCase("")));
    }

    public static ARSceneXML parseXML(Node node)
    {
        ARSceneXML arSceneXML = new ARSceneXML();

        NodeList childNodes = node.getChildNodes();

        for (int i=0; i<childNodes.getLength(); i++)
        {
            Node child = childNodes.item(i);
            if (!child.hasChildNodes())
            {
                continue;
            }
            String tag = child.getNodeName();
            if (tag.equalsIgnoreCase("ID"))
            {
                arSceneXML.mID = Integer.parseInt(child.getFirstChild().getNodeValue());
            }
            else if (tag.equalsIgnoreCase("Name"))
            {
                arSceneXML.mName = child.getFirstChild().getNodeValue();
            }
            else if (tag.equalsIgnoreCase("Description"))
            {
                arSceneXML.mDescription = child.getFirstChild().getNodeValue();
            }
            else if (tag.equalsIgnoreCase("Hint"))
            {
                arSceneXML.mHint = child.getFirstChild().getNodeValue();
            }
            else if (tag.equalsIgnoreCase("TimeCreated"))
            {
                arSceneXML.mTimeCreated = child.getFirstChild().getNodeValue();
            } else if (tag.equalsIgnoreCase("Lat"))
            {
                arSceneXML.mLat = Double.parseDouble(child.getFirstChild().getNodeValue());
            } else if (tag.equalsIgnoreCase("Long"))
            {
                arSceneXML.mLong = Double.parseDouble(child.getFirstChild().getNodeValue());
            } else if (tag.equalsIgnoreCase("File"))
            {
                arSceneXML.mTrackingFileRelativePath = child.getFirstChild().getNodeValue();
            }
            else if (tag.equalsIgnoreCase("Models"))
            {
                parseModels(child, arSceneXML.mARGeomDataPathList);
            }
        }

        return arSceneXML.isValid() ? arSceneXML : null;
    }

    private static void parseModels(Node node, List<String> geomDataList)
    {
        NodeList childNodes = node.getChildNodes();

        for (int i=0; i<childNodes.getLength(); i++)
        {
            Node child = childNodes.item(i);
            if (!child.hasChildNodes())
            {
                continue;
            }
            String tag = child.getNodeName();
            if (tag.equalsIgnoreCase("File"))
            {
                geomDataList.add(child.getFirstChild().getNodeValue());
            }
        }
    }

    /**
     * Take care that file paths are set to relative
     * @return xmlString
     */
    public String createXMLString()
    {
        String xmlStart = "\t<ARScene>\n" +
                "\t\t<ID>"+ mID +"</ID>\n" +
                "\t\t<Name>"+ mName +"</Name>\n" +
                "\t\t<Description>"+ mDescription +"</Description>\n" +
                "\t\t<Hint>"+ mHint +"</Hint>\n" +
                "\t\t<Lat>"+ mLat +"</Lat>\n" +
                "\t\t<Long>"+ mLong +"</Long>\n" +
                "\t\t<TimeCreated>"+ mTimeCreated+"</TimeCreated>\n" +
                "\t\t<File>"+ mTrackingFileRelativePath + "</File>\n" +
                "\t\t<Models>\n";
        String xmlModels = "";
        for (String model : mARGeomDataPathList)
        {
            xmlModels = xmlModels + "\t\t\t<File>"+ model +"</File>\n";
        }
        String xmlEnd = "\t\t</Models>\n\t</ARScene>";
        return xmlStart + xmlModels + xmlEnd;
    }

    public static List<ARModel> saveScene(String tempTrackingConfigRelativePath, String destFolderRelativePath,
                                            String name, String descr, String hint,
                                            double lat, double lngt, String timeCreated,
                                            List<ARModel> sceneModels, List<IGeometry> sceneGeoms)
    {
        File tempTrackingFile = FileHelpers.getLocalDataFile(tempTrackingConfigRelativePath);

        // check if the tracking config exists
        if( (!tempTrackingFile.exists()) || (sceneGeoms.size()==0) )
        {
            return null;
        }

        List<ARModel> savedSceneModels = new ArrayList<>();

        // clear saved folder content
        final String tempFolderPath = tempTrackingFile.getParent();
        File saveFolder = FileHelpers.getLocalDataFile(destFolderRelativePath);
        FileHelpers.deleteRecursive(saveFolder);
        destFolderRelativePath = FileHelpers.setFolderDelimiters(destFolderRelativePath);
        saveFolder = FileHelpers.createLocalDataFile(destFolderRelativePath, false);

        FileHelpers.copyFolderRecursive(new File(tempFolderPath), saveFolder);

        List<String> arGeomDataPaths = new ArrayList<>();
        for (int i=0; i<sceneModels.size(); i++)
        {
            ARModel arModel = new ARModel(sceneModels.get(i));
            final String sourcePath = arModel.getAbsoluteFolderPath();
            arModel.setRelativePaths();

            // create a new folder
            String modelFolderRelativePath = destFolderRelativePath + i;
            modelFolderRelativePath = FileHelpers.setFolderDelimiters(modelFolderRelativePath);
            File modelFolder = FileHelpers.createLocalDataFile(modelFolderRelativePath, false);

            String modelFolderAbsolutePath = modelFolder.getAbsolutePath();
            modelFolderAbsolutePath = FileHelpers.setFolderDelimiters(modelFolderAbsolutePath);
            arModel.setAbsoluteFolderPath(modelFolderAbsolutePath);

            // copy all files
            FileHelpers.copyFolderRecursive(new File(sourcePath), modelFolder);

            // overwrite the object.xml file
            final String arGeomDataXMLPath = modelFolderRelativePath + ARModel.getGeomDataFilename();
            // set geom parameters
            arModel.getARGeomData().setFromGeometry(sceneGeoms.get(i));
            final String arGeomDataXMLString = arModel.getARGeomData().createXMLString();
            File arGeomDataFile = FileHelpers.createLocalDataFile(arGeomDataXMLPath, true);
            FileHelpers.writeToFile(arGeomDataXMLString, arGeomDataFile);

            savedSceneModels.add(arModel);
            arGeomDataPaths.add(i + "/" + ARModel.getGeomDataFilename());
        }

        ARSceneXML sceneXML = new ARSceneXML(name, descr, hint, lat, lngt, timeCreated, tempTrackingFile.getName(), arGeomDataPaths);
        final String sceneXMLString = sceneXML.createXMLString();
        File sceneXMLFile = FileHelpers.createLocalDataFile(destFolderRelativePath + ARSceneXML.DEFAULT_SCENE_XML_FILENAME, true);
        FileHelpers.writeToFile(sceneXMLString, sceneXMLFile);

        // create xcoin file
        File xcoinFile = FileHelpers.createLocalDataFile(destFolderRelativePath + ARSceneXML.DEFAULT_SCENE_XCOIN_FILENAME, true);
        FileHelpers.writeToFile("0", xcoinFile);

        return savedSceneModels;
    }

    public List<String> getARGeomDataPathList()
    {
        return mARGeomDataPathList;
    }

    public String getName()
    {
        return mName;
    }

    public double getLat()
    {
        return mLat;
    }

    public double getLong()
    {
        return mLong;
    }

    public String getTimeCreated()
    {
        return mTimeCreated;
    }

    public String getTrackingFileRelativePath()
    {
        return mTrackingFileRelativePath;
    }

    public static String getDefaultSceneXmlFilename()
    {
        return DEFAULT_SCENE_XML_FILENAME;
    }
}
