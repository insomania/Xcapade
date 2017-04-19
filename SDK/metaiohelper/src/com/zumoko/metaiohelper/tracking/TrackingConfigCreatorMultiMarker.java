package com.zumoko.metaiohelper.tracking;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.StringVector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by darsta on 01-Mar-16.
 */
public class TrackingConfigCreatorMultiMarker
{
    public final String TAG = "[TrackingConfigCreatorMultiMarker]";

    private class COSDescription
    {
        public String markerPath = "";
        public int width = 960;
        public int height = 600;

        public COSDescription(COSDescription toCopy)
        {
            markerPath = new String(toCopy.markerPath);
            height = toCopy.height;
            width = toCopy.width;
        }

        public COSDescription()
        {

        }
    }

    public List<COSDescription> mCOSes = new ArrayList<>();

    public static String createMarkerFilename(int cosID)
    {
        Date date = Calendar.getInstance().getTime();
        final String timeOfCreation = new Long(date.getTime()).toString();

        return "m" + cosID + "_" + timeOfCreation + "_.jpg";
    }

    public TrackingConfigCreatorMultiMarker()
    {

    }

    public TrackingConfigCreatorMultiMarker(TrackingConfigCreatorMultiMarker toCopy)
    {
        Iterator<COSDescription> iter = toCopy.mCOSes.iterator();
        while (iter.hasNext())
        {
            COSDescription cos = new COSDescription(iter.next());
            mCOSes.add(cos);
        }
    }

    public void addCOS(String markerPath, int width, int height)
    {
        MetaioDebug.log(TAG + "[addCOS] Name, W, H: " +markerPath + "; "+ width + "; " + height);
        COSDescription cos = new COSDescription();
        cos.markerPath = markerPath;
        cos.width = width;
        cos.height = height;

        mCOSes.add(cos);
    }

    public String replaceCOS(int cosID, String markerPath, int width, int height)
    {
        MetaioDebug.log(TAG + "[replaceCOS] cosID, Name, W, H: " + cosID + "; " + markerPath + "; "+ width + "; " + height);
        COSDescription cos = new COSDescription();
        cos.markerPath = markerPath;
        cos.width = width;
        cos.height = height;

        String removedMarkerFilename = "";
        if (mCOSes.size() >= cosID)
        {
            removedMarkerFilename = mCOSes.get(cosID - 1).markerPath;
            mCOSes.set(cosID - 1, cos);
        }

        return removedMarkerFilename;
    }

    public String removeCOS(int cosID)
    {
        String removedMarkerFilename = "";
        if (mCOSes.size() >= cosID)
        {
            COSDescription cosToRemove = mCOSes.get(cosID-1);
            removedMarkerFilename = cosToRemove.markerPath;
//            MetaioDebug.log(TAG + "[removeCOS] markerFileName = " + removedMarkerFilename);
            final boolean removed = mCOSes.remove(cosToRemove);
            MetaioDebug.log(TAG + "[removeCOS] removed = " + removed);
        }
        return removedMarkerFilename;
    }

    public int getTotalCOSes()
    {
        return mCOSes.size();
    }

    public List<String> getMarkerFilenames()
    {
        List<String> markers = new ArrayList<>();
        Iterator<COSDescription> coses = mCOSes.iterator();
        while(coses.hasNext())
        {
            markers.add(coses.next().markerPath);
        }
        return markers;
    }

    private String createSensors()
    {
        String startTag =
                "    <Sensors>\n" +
                "        <Sensor Subtype=\"FAST\" Type=\"FeatureBasedSensorSource\">\n" +
                "            <SensorID>FeatureBasedSensorSource_0</SensorID>\n" +
                "            <Parameters>\n" +
                "                <MaxObjectsToDetectPerFrame>5</MaxObjectsToDetectPerFrame>\n" +
                "                <MaxObjectsToTrackInParallel>1</MaxObjectsToTrackInParallel>\n" +
                "            </Parameters>";
        String closeTag =
                "        </Sensor>\n" +
                "    </Sensors>";

        String cosesXML = "";
        for (int i=0; i<mCOSes.size(); i++)
        {
            COSDescription cos = mCOSes.get(i);
            cosesXML = cosesXML +
                    "            <SensorCOS>\n" +
                    "                <SensorCosID>marker" + i + "</SensorCosID>\n" +
                    "                <Parameters>\n" +
                    "                    <SimilarityThreshold>0.7</SimilarityThreshold>\n" +
                    "                    <ReferenceImage WidthMM=\"" + cos.width + "\" HeightMM=\"" + cos.height + "\">" + cos.markerPath + "</ReferenceImage>\n" +
                    "                </Parameters>\n" +
                    "            </SensorCOS>\n";
        }

        return startTag + cosesXML + closeTag;
    }


    private String createConnections()
    {
        String startTag = "<Connections>";
        String closeTag = "</Connections>";

        String connectionsXML = "";
        for (int i=0; i<mCOSes.size(); i++)
        {
            connectionsXML = connectionsXML +
                    "        <COS>\n" +
                    "            <Name>marker" + i + "</Name>\n" +
                    "            <Fuser Type=\"SmoothingFuser\">\n" +
                    "                <Parameters>\n" +
                    "                    <AlphaRotation>0.8</AlphaRotation>\n" +
                    "                    <AlphaTranslation>1.0</AlphaTranslation>\n" +
                    "                    <GammaRotation>0.8</GammaRotation>\n" +
                    "                    <GammaTranslation>1.0</GammaTranslation>\n" +
                    "                    <KeepPoseForNumberOfFrames>3</KeepPoseForNumberOfFrames>\n" +
                    "                </Parameters>\n" +
                    "            </Fuser>\n" +
                    "            <SensorSource>\n" +
                    "                <SensorID>FeatureBasedSensorSource_0</SensorID>\n" +
                    "                <SensorCosID>marker" + i + "</SensorCosID>\n" +
                    "                <HandEyeCalibration>\n" +
                    "                    <TranslationOffset>\n" +
                    "                        <x>0.0</x>\n" +
                    "                        <y>0.0</y>\n" +
                    "                        <z>0.0</z>\n" +
                    "                    </TranslationOffset>\n" +
                    "                    <RotationOffset>\n" +
                    "                        <x>0.0</x>\n" +
                    "                        <y>0.0</y>\n" +
                    "                        <z>0.0</z>\n" +
                    "                        <w>1.0</w>\n" +
                    "                    </RotationOffset>\n" +
                    "                </HandEyeCalibration>\n" +
                    "                <COSOffset>\n" +
                    "                    <TranslationOffset>\n" +
                    "                        <x>0.0</x>\n" +
                    "                        <y>1.0</y>\n" +
                    "                        <z>0.0</z>\n" +
                    "                    </TranslationOffset>\n" +
                    "                    <RotationOffset>\n" +
                    "                        <x>0.0</x>\n" +
                    "                        <y>0.0</y>\n" +
                    "                        <z>0.0</z>\n" +
                    "                        <w>1.0</w>\n" +
                    "                    </RotationOffset>\n" +
                    "                </COSOffset>\n" +
                    "            </SensorSource>\n" +
                    "        </COS>";
        }

        return startTag + connectionsXML + closeTag;
    }

    public String createConfig()
    {
        if (mCOSes.size()==0)
        {
            return "";
        }

        String startTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<TrackingData>";
        String closeTag = "</TrackingData>";
        String tracking = startTag + createSensors() + createConnections() + closeTag;

        return tracking;
    }
}