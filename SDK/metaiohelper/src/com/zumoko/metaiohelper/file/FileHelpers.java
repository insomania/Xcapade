package com.zumoko.metaiohelper.file;

import android.content.Context;
import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by darsta on 08-Mar-16.
 */
public class FileHelpers
{
    public static void writeToFile(String data, File file)
    {
        try
        {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.flush();
            outputStream.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFromFile(File file)
    {
        try
        {
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null)
            {
                if (firstLine)
                {
                    // don't append EOL character if only one line in file, i.e. don't append EOL to the last line in the file
                    firstLine = false;
                    sb.append(line);
                }
                else
                {
                    sb.append("\n").append(line);
                }
            }
            reader.close();
            inputStream.close();

            return sb.toString();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return null;
    }

    public static void overwriteLocalDataFile(String srcRelativePath, String dstRelativePath)
    {
        File src = getLocalDataFile(srcRelativePath);
        File dst = createLocalDataFile(dstRelativePath, true);
        if (src.exists())
        {
            overwriteFile(src, dst);
        }
    }

    public static void overwriteFile(File src, File dst)
    {
        try
        {
            FileInputStream inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        }
        catch (IOException exc)
        {
            exc.printStackTrace();
        }
    }

    public static String setFolderDelimiters(String folderRelativePath)
    {
        if ( ! (folderRelativePath.endsWith("/") || folderRelativePath.endsWith("\\")))
        {
            folderRelativePath = folderRelativePath + "/";
        }
        return folderRelativePath.replace("\\", "/");
    }

    public static File getLocalDataFile(String relativePath)
    {
        final String path = getAbsolutePath(relativePath);
        return new File(path);
    }

    public static String getAbsolutePath(final String relativePath)
    {
        return AssetsManager.getAbsolutePath() + "/" + relativePath;
    }

    public static String getRelativePath(final String absolutePath)
    {
        final String rootPath = AssetsManager.getAbsolutePath() + "/";

        if (absolutePath.startsWith(rootPath))
        {
            return absolutePath.substring(rootPath.length());
        }
        return absolutePath;
    }

    public static void deleteLocalDataFile(String relativePath)
    {
        MetaioDebug.log("[deleteLocalDataFile] File : " + relativePath);
        File localFile = getLocalDataFile(relativePath);
        // delete existing file
        if (localFile.exists())
        {
            MetaioDebug.log("[deleteLocalDataFile] Delete file : " + relativePath);
            localFile.delete();
        }
    }

    public static void deleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static List<File> listAllFilesAndFoldersRecursive(File directory)
    {
        if (!directory.isDirectory())
        {
            return new ArrayList<>();
        }

        List<File> filesInFolder = new ArrayList<>(Arrays.asList(directory.listFiles()));
        List<File> filesModifiable = new ArrayList<>(Arrays.asList(directory.listFiles()));
        for (File file : filesInFolder)
        {
            if (file.isDirectory())
            {
                List<File> subfolderFiles = listAllFilesAndFoldersRecursive(file);
                filesModifiable.addAll(subfolderFiles);
            }
        }

        return filesModifiable;
    }

    public static List<File> listAllSubFolders(File directory)
    {
        if (!directory.isDirectory())
        {
            return new ArrayList<>();
        }

        List<File> subFolders = new ArrayList<>(Arrays.asList(directory.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(final File pathname)
            {
                return pathname.isDirectory();
            }
        })));

        return subFolders;
    }

    public static void createFolderStructure(File folder, boolean isFolder)
    {
        String parentFolderPath;
        if (isFolder)
        {
            parentFolderPath = folder.getAbsolutePath();
        }
        else
        {
            parentFolderPath = folder.getParentFile().getAbsolutePath();
        }
        File parentFolder = new File(parentFolderPath);
        if (!parentFolder.exists())
        {
            parentFolder.mkdirs();
        }
    }

    public static File createLocalDataFile(String relativePath, boolean overwrite)
    {
        MetaioDebug.log("[createLocalDataFile] File : " + relativePath);
        return createFile(getAbsolutePath(relativePath), overwrite);
    }

    public static File createFile(String absolutePath, boolean overwrite)
    {
        MetaioDebug.log("[createFile] File : " + absolutePath);

        File localFile = new File(absolutePath);
        // delete existing file
        if (localFile.exists())
        {
            if (localFile.isDirectory())
            {
                MetaioDebug.log("[createFile] Folder already exists: " + absolutePath);
                return localFile;
            }
            if (overwrite)
            {
                MetaioDebug.log("[createFile] Delete file : " + absolutePath);
                localFile.delete();
            }
            else
            {
                MetaioDebug.log("[createFile] File already exists: " + absolutePath);
                return localFile;
            }
        }
        // create a new file
        try
        {
            MetaioDebug.log("[createFile] Create new file : " + absolutePath);
            boolean isFolder = (absolutePath.endsWith("/")||absolutePath.endsWith("\\")) ? true : false;
            FileHelpers.createFolderStructure(localFile, isFolder);
            if (!localFile.isDirectory())   // it is already created if it is a dir by the previous command
            {
                localFile.createNewFile();
            }
        }
        catch (IOException exc)
        {
            Log.d("[createFile][ERROR]", exc.getMessage());
            exc.printStackTrace();
        }
        return localFile;
    }

    public static void copyFolderRecursive(File src, File dest)
    {
        if(src.isDirectory())
        {
            if(!dest.exists())
            {
                dest.mkdirs();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files)
            {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolderRecursive(srcFile, destFile);
            }
        }
        else
        {
            overwriteFile(src, dest);
        }
    }

    public static File copyToExternalStorage(Context context, String filename, File srcFile)
    {
        try
        {
            File dstFile = new File(context.getExternalFilesDir(null), filename); //Get file location from external source
            dstFile.createNewFile();
            overwriteFile(srcFile, dstFile);
            return dstFile;
        }
        catch (Exception e)
        {
            MetaioDebug.log("[copyToExternalStorage] Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
