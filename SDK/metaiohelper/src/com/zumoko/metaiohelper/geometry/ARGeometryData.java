package com.zumoko.metaiohelper.geometry;

import com.metaio.sdk.jni.AnimationKeyFrame;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector2di;
import com.metaio.sdk.jni.Vector3d;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

/**
 * Created by darsta on 28-Jan-16.
 */
public class ARGeometryData
{
    public enum GeomType
    {
        IMAGE,
        VIDEO,
        MODEL
    }

    private String mName = "";
    private String mFilename = null;

    private GeomType mGeomType = GeomType.IMAGE;
    private boolean mVideoTransparent = false;

    private float mScale = 0.f;
    private Vector3d mTranslation = new Vector3d(0.f, 0.f, 0.f);
    private Rotation mRotation = new Rotation(new Vector3d(0.f, 0.f, 0.f));

    private int mRenderOrder = 0;
    private int mCOSID = 1;
    private float mTransparency = 0.f;

    private boolean isInit()
    {
        return (mFilename!=null);
    }

    public ARGeometryData()
    {

    }

    public ARGeometryData(ARGeometryData data)
    {
        mName = data.mName;
        mFilename = data.mFilename;

        mGeomType = data.mGeomType;
        mVideoTransparent = data.mVideoTransparent;

        mScale = data.mScale;
        mTranslation = data.mTranslation;
        mRotation = data.mRotation;

        mRenderOrder = data.mRenderOrder;
        mCOSID = data.mCOSID;
        mTransparency = data.mTransparency;
    }

    public ARGeometryData(String name, String filename, float scale, Vector3d transl, Rotation rot)
    {
        mName = name;
        mFilename = filename;
        mScale = scale;
        mTranslation = transl;
        mRotation = rot;
    }

    public static ARGeometryData parseXMLData(Node node)
    {
        ARGeometryData geomData = new ARGeometryData();

        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++)
        {
            Node child = children.item(j);
            if (!child.hasChildNodes())
            {
                continue;
            }
            final String nodeName = child.getNodeName();
            if (nodeName.compareTo("Name")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mName = valueNode.getNodeValue();
            }
            else if (nodeName.compareTo("File")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mFilename = valueNode.getNodeValue();
            }
            else if (nodeName.compareTo("Scale")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mScale = Float.parseFloat(valueNode.getNodeValue());
            }
            else if (nodeName.compareTo("Transparency")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mTransparency = Float.parseFloat(valueNode.getNodeValue());
            }
            else if (nodeName.compareTo("Translation")==0)
            {
                Vector3d vec = parseVector3D(child);
                if (vec!=null) geomData.mTranslation = vec;
            }
            else if (nodeName.compareTo("Rotation")==0)
            {
                Vector3d vec = parseVector3D(child);
                if (vec!=null) geomData.mRotation = new Rotation(vec);
            }
            else if (nodeName.compareTo("RenderOrder")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mRenderOrder = Integer.parseInt(valueNode.getNodeValue());
            }
            else if (nodeName.compareTo("COSID")==0)
            {
                final Node valueNode = child.getFirstChild();
                geomData.mCOSID = Integer.parseInt(valueNode.getNodeValue());
            }
            else if ((nodeName.equalsIgnoreCase("GeomType")) || (nodeName.equalsIgnoreCase("Type")))
            {
                final String value = child.getFirstChild().getNodeValue();
                if (value.equalsIgnoreCase("image"))
                {
                    geomData.mGeomType = GeomType.IMAGE;
                }
                else if (value.equalsIgnoreCase("video"))
                {
                    geomData.mGeomType = GeomType.VIDEO;
                }
                else if (value.equalsIgnoreCase("model"))
                {
                    geomData.mGeomType = GeomType.MODEL;
                }
            }
            else if (nodeName.equalsIgnoreCase("VideoTransparent"))
            {
                final String value = child.getFirstChild().getNodeValue();
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"))
                {
                    geomData.mVideoTransparent = true;
                }
                else
                {
                    geomData.mVideoTransparent = false;
                }
            }
        }

        if (!geomData.isInit())
        {
            return null;
        }

        return geomData;
    }

    public String createXMLString()
    {
        final Vector3d rotVec = mRotation.getEulerAngleRadians();
        String xml = "\t<ARGeometry>\n" +
                "\t\t<Name>"+ mName +"</Name>\n" +
                "\t\t<File>"+ mFilename +"</File>\n" +
                "\t\t<Scale>"+ mScale +"</Scale>\t\t\n" +
                "\t\t<Translation><X>"+ mTranslation.getX() +"</X><Y>"+ mTranslation.getY() +"</Y><Z>"+ mTranslation.getZ() +"</Z></Translation>\n" +
                "\t\t<Rotation><X>"+ rotVec.getX() +"</X><Y>"+ rotVec.getY() +"</Y><Z>"+ rotVec.getZ() +"</Z></Rotation>\n" +
                "\t\t<RenderOrder>"+ mRenderOrder +"</RenderOrder>\n" +
                "\t\t<COSID>"+ mCOSID +"</COSID>\n" +
                "\t\t<Transparency>"+ mTransparency +"</Transparency>\n" +
                "\t\t<Type>"+ mGeomType.name() +"</Type>\n" +
                "\t\t<VideoTransparent>"+ mVideoTransparent +"</VideoTransparent>\n" +
                "\t</ARGeometry>";
        return xml;
    }

    public static Vector3d parseVector3D(Node node)
    {
        Vector3d vec = new Vector3d(0.f, 0.f, 0.f);

        NodeList children = node.getChildNodes();
        boolean[] elemsSet = {false, false, false};

        for (int j = 0; j < children.getLength(); j++)
        {
            Node child = children.item(j);
            final String nodeName = child.getNodeName();
            if (child.getFirstChild()==null)
            {
                continue;
            }
            if (nodeName.equalsIgnoreCase("X"))
            {
                vec.setX(Float.parseFloat(child.getFirstChild().getNodeValue()));
                elemsSet[0] = true;
            }
            else if (nodeName.equalsIgnoreCase("Y"))
            {
                vec.setY(Float.parseFloat(child.getFirstChild().getNodeValue()));
                elemsSet[1] = true;
            }
            else if (nodeName.equalsIgnoreCase("Z"))
            {
                vec.setZ(Float.parseFloat(child.getFirstChild().getNodeValue()));
                elemsSet[2] = true;
            }
        }
        return (elemsSet[0] && elemsSet[1] && elemsSet[2]) ? vec : null;
    }

    public static Vector2d parseVector2D(Node node)
    {
        Vector2d vec = new Vector2d(0.f, 0.f);
        NodeList children = node.getChildNodes();
        boolean[] elemsSet = {false, false};

        for (int j = 0; j < children.getLength(); j++)
        {
            Node child = children.item(j);
            final String nodeName = child.getNodeName();
            if (child.getFirstChild()==null)
            {
                continue;
            }
            if (nodeName.equalsIgnoreCase("X"))
            {
                vec.setX(Float.parseFloat(child.getFirstChild().getNodeValue()));
                elemsSet[0] = true;
            }
            else if (nodeName.equalsIgnoreCase("Y"))
            {
                vec.setY(Float.parseFloat(child.getFirstChild().getNodeValue()));
                elemsSet[1] = true;
            }
        }
        return (elemsSet[0] && elemsSet[1]) ? vec : null;
    }

    public static Vector2di parseVector2Di(Node node)
    {
        Vector2di vec = new Vector2di(0, 0);
        NodeList children = node.getChildNodes();
        boolean[] elemsSet = {false, false};

        for (int j = 0; j < children.getLength(); j++)
        {
            Node child = children.item(j);
            final String nodeName = child.getNodeName();
            if (child.getFirstChild()==null)
            {
                continue;
            }
            if (nodeName.equalsIgnoreCase("X"))
            {
                vec.setX(Integer.parseInt(child.getFirstChild().getNodeValue()));
                elemsSet[0] = true;
            }
            else if (nodeName.equalsIgnoreCase("Y"))
            {
                vec.setY(Integer.parseInt(child.getFirstChild().getNodeValue()));
                elemsSet[1] = true;
            }
        }
        return (elemsSet[0] && elemsSet[1]) ? vec : null;
    }

    public static ARGeometryData reScale(ARGeometryData input, float rescaler)
    {
        if (input==null)
        {
            return null;
        }
        ARGeometryData newData = new ARGeometryData(input);
        newData.mScale *= rescaler;
        newData.mTranslation = newData.mTranslation.multiply(rescaler);
        return newData;
    }

    public AnimationKeyFrame getKeyframe(int idx)
    {
        AnimationKeyFrame kf = new AnimationKeyFrame();
        kf.setRotation(mRotation);
        kf.setTranslation(mTranslation);
        kf.setScale(new Vector3d(mScale));
        kf.setIndex(idx);
        return kf;
    }

    public void setFromGeometry(IGeometry geom)
    {
        mScale = geom.getScale().getX();
        mTranslation = geom.getTranslation();
        mRotation = geom.getRotation();

        mRenderOrder = geom.getRenderOrder();
        mCOSID = geom.getCoordinateSystemID();
    }

    public void setToGeometry(IGeometry geom)
    {
        geom.setScale(mScale);
        geom.setTranslation(mTranslation);
        geom.setRotation(mRotation);

        geom.setRenderOrder(mRenderOrder);
        geom.setCoordinateSystemID(mCOSID);
    }

    public IGeometry loadGeometry(ARViewHelper helper)
    {
        IGeometry geom = null;
        switch (mGeomType)
        {
            case IMAGE:
                geom = helper.loadImageModel(mFilename, mCOSID, mScale, mRotation, mTranslation);
                break;
            case VIDEO:
                geom = helper.loadVideoModel(mFilename, mCOSID, mScale, mRotation, mTranslation, mVideoTransparent);
                break;
            case MODEL:
                geom = helper.loadModel(mFilename, mCOSID, mScale, mRotation, mTranslation);
                break;
        }
        if (geom!=null)
        {
            geom.setCoordinateSystemID(mCOSID);
            geom.setRenderOrder(mRenderOrder);
            if (mGeomType.equals(GeomType.IMAGE) || mGeomType.equals(GeomType.MODEL))
            {
                geom.setTransparency(mTransparency);
            }
        }
        return geom;
    }

    public long getTotalAssetFileSize(ARViewHelper helper)
    {
        File temp = helper.getAssetPathAsFile(mFilename);
        return (temp!=null && temp.exists()) ? temp.length() : 0;
    }

    public String getName()
    {
        return mName;
    }

    public String getFilename()
    {
        return mFilename;
    }

    public void setFilename(final String filename)
    {
        mFilename = filename;
    }

    public float getScale()
    {
        return mScale;
    }

    public final Vector3d getTranslation()
    {
        return mTranslation;
    }

    public final Rotation getRotation()
    {
        return mRotation;
    }

    public int getRenderOrder()
    {
        return mRenderOrder;
    }

    public int getCOSID()
    {
        return mCOSID;
    }

    public void setCOSID(final int COSID)
    {
        mCOSID = COSID;
    }

    public GeomType getGeomType()
    {
        return mGeomType;
    }

    public boolean isVideoTransparent()
    {
        return mVideoTransparent;
    }
}
