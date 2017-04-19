package com.zumoko.metaiohelper.xml;

import android.util.Log;
import android.util.Pair;

import com.metaio.sdk.MetaioDebug;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by darsta on 15-Nov-15.
 */
public class XMLParserHelper
{
    private final static String TAG = "[XMLParserHelper]";

    public static Node getRootNode(String xmlFilePath, String rootNodeTag)
    {
        File file = new File(xmlFilePath);
        if ( (file==null) || (!file.exists()) )
        {
            return null;
        }

        Document xmlDoc = XMLParserHelper.getDomElement(file);
        if (xmlDoc==null)
        {
            return null;
        }

        NodeList nodes = xmlDoc.getElementsByTagName(rootNodeTag);
        if (nodes.getLength()==0)
        {
            return null;
        }

        return nodes.item(0);
    }


    public static Document getDomElement(File XMLFile)
    {
        String stringXML = XMLParserHelper.getStringFromFile(XMLFile);

        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(stringXML));
            doc = db.parse(is);

        }
        catch (ParserConfigurationException e)
        {
            Log.e(TAG + "Error: ", e.getMessage());
            return null;
        }
        catch (SAXException e)
        {
            Log.e(TAG + "Error: ", e.getMessage());
            return null;
        }
        catch (IOException e) {
            Log.e(TAG + "Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }

    private static String convertStreamToString(InputStream is) throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(File fl)
    {
        try
        {
            FileInputStream fin = new FileInputStream(fl);
            String ret = convertStreamToString(fin);
            // Make sure you close all streams.
            fin.close();
            return ret;
        }
        catch (Exception e)
        {
            MetaioDebug.log(TAG + "Error: unable to convert XML file to String: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

}
