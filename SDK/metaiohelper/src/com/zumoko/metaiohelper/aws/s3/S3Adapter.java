package com.zumoko.metaiohelper.aws.s3;

import android.content.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.metaio.sdk.MetaioDebug;
import com.zumoko.metaiohelper.file.FileHelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by darsta on 30-Mar-16.
 */
public class S3Adapter
{
    private static String TAG = "[S3Adapter]";
    private static String BUCKET_DELIMITER = "/";
    private static String INTERNAL_METADATA_EXTENSION = ".lmd";
    private AmazonS3 mS3;
    private Context mAplicationContext;

    public class S3ObjectsAndPrefixes
    {
        public List<S3ObjectSummary> objects;   // files
        public List<String> prefixes;   // folders

        public List<S3ObjectSummary> getFiles()
        {
            return objects;
        }

        public List<String> getFolders()
        {
            return prefixes;
        }
    }


    public S3Adapter(final Context aplicationContext, final String access_key, final String secret_key)
    {
        mS3 = new AmazonS3Client(new BasicAWSCredentials(access_key, secret_key));
        Region usWest1 = Region.getRegion(Regions.US_WEST_1);
        mS3.setRegion(usWest1);
        mAplicationContext = aplicationContext;
    }

    public S3ObjectsAndPrefixes listObjects(String bucketName, String prefix, String delimiter)
    {
        S3ObjectsAndPrefixes filesAndFolders = new S3ObjectsAndPrefixes();

        ListObjectsRequest listObjectsRequest;
        if (delimiter!=null)
        {
            if (!prefix.endsWith(delimiter))
            {
                prefix += delimiter;
            }
            listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName).withPrefix(prefix)
                    .withDelimiter(delimiter);
        }
        else
        {
            listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(bucketName).withPrefix(prefix);
        }

        ObjectListing objects = null;
        try
        {
            objects = mS3.listObjects(listObjectsRequest);
        }
        catch (Exception exc)
        {
            MetaioDebug.log(TAG + "[listObjects][Exception]");
            exc.printStackTrace();
            return null;
        }

        filesAndFolders.objects = objects.getObjectSummaries();
        filesAndFolders.prefixes = objects.getCommonPrefixes();

        while (objects.isTruncated())
        {
            try
            {
                mS3.listNextBatchOfObjects(objects);
            }
            catch (Exception exc)
            {
                MetaioDebug.log(TAG + "[listObjects][Exception]");
                exc.printStackTrace();
                return null;
            }

            filesAndFolders.objects.addAll(objects.getObjectSummaries());
            filesAndFolders.prefixes.addAll(objects.getCommonPrefixes());
        }

        return filesAndFolders;
    }

    /**
     *  @return all files in a 'prefix' folder, including files in subfolders, and including empty folders,
     *          and including the 'prefix' folder itself
     */
    public List<S3ObjectSummary> listAllObjects(String bucketName, String prefix)
    {
        S3ObjectsAndPrefixes op = listObjects(bucketName, prefix, null);
        if (op==null)
        {
            return null;
        }
        return op.objects;
    }

    /**
     *  @return all files in 'prefix' folder, excluding files in subfolders,
     *          and including the 'prefix' folder itself
     */
    public List<S3ObjectSummary> listAllFilesInFolder(String bucketName, String prefix)
    {
        S3ObjectsAndPrefixes op = listObjects(bucketName, prefix, BUCKET_DELIMITER);
        if (op==null)
        {
            return null;
        }
        return op.objects;
    }

    /**
     *  @return all non empty subfolders
     */
    public List<String> listAllSubFolders(String bucketName, String prefix)
    {
        S3ObjectsAndPrefixes op = listObjects(bucketName, prefix, BUCKET_DELIMITER);
        if (op==null)
        {
            return null;
        }
        return op.prefixes;
    }

    private void overwriteInternalMetadataForFile(File internalFile, String content)
    {
        final String path = internalFile.getAbsolutePath() + INTERNAL_METADATA_EXTENSION;
        File mdf = FileHelpers.createFile(path, true);
        FileHelpers.writeToFile(content, mdf);
    }

    private File getInternalMetadataForFile(File internalFile)
    {
        final String path = internalFile.getAbsolutePath() + INTERNAL_METADATA_EXTENSION;
        File mdf = new File(path);
        if (!mdf.exists())
        {
            return null;
        }
        return mdf;
    }

    private boolean fileUpToDate(S3ObjectSummary objectSummary)
    {
        //        check if local file exists
        File internalFile = FileHelpers.getLocalDataFile(objectSummary.getKey());
        if ( internalFile.isDirectory() || (!internalFile.exists()) )
        {
            return false;
        }

        //        check if local file metadata exists
        File mdf = getInternalMetadataForFile(internalFile);
        if (mdf==null)
        {
            return false;
        }

        //        check if local file metadata matches object metadata
        final String localLastModified = FileHelpers.readFromFile(mdf);
        final String awsLastModified = objectSummary.getLastModified().toString();
        if ( (localLastModified!=null) && (localLastModified.compareTo(awsLastModified)!=0) )
        {
            return false;
        }

        return true;
    }

    /**
     *  Tracks cumulative progress for multiple uploads or downloads at once
     */
    public static abstract class ProgressNotifier
    {
        private long mTotalProgress = 0;
        private Map<Integer, Long> mProgressList = new HashMap<>();

        public void postProgress(int id, long currentProgress, long total)
        {
            Long oldProgress = mProgressList.get(id);
            if (oldProgress == null)
            {
                mProgressList.put(id, new Long(0));
                oldProgress = mProgressList.get(id);
            }
            if (currentProgress!=oldProgress)
            {
                MetaioDebug.log(TAG + "[ProgressNotifier][postProgress] progress = " + currentProgress + " / " + total);
                mTotalProgress += currentProgress - oldProgress;
                notifyTotalProgress(mTotalProgress);
                mProgressList.put(id, new Long(currentProgress));
            }
        }
        protected abstract void setTotalData(long total);
        protected abstract void notifyTotalProgress(long addedProgress);
    }

    public boolean syncFilesInInternalMemory(String rootFolderRelativePath, List<S3ObjectSummary> fileList, final ProgressNotifier progressNotifier)
    {
        // Agenda
        //  1. create a new empty list
        //  2. for each file, check if it is up to date, if not add it to a new list
        //  3. for each file in the update list create a local file
        //  4. download file with progress updating

        long totalForDownload = 0;
        List<S3ObjectSummary> updateList = new ArrayList<>();
        List<String> s3Folders = new ArrayList<>();
        s3Folders.add(FileHelpers.setFolderDelimiters(rootFolderRelativePath));
        for (S3ObjectSummary objectSummary : fileList)
        {
            if (!fileUpToDate(objectSummary))
            {
                updateList.add(objectSummary);
                totalForDownload += objectSummary.getSize();
            }
            // track list of local folders that has been updated
            final String key = objectSummary.getKey();
            final int lastDelimiter = key.lastIndexOf("/");
            if (lastDelimiter!=-1)
            {
                String folder = objectSummary.getKey().substring(0, lastDelimiter);
                folder = FileHelpers.setFolderDelimiters(folder);
                if (!s3Folders.contains(folder))
                {
                    s3Folders.add(folder);
                }
            }
        }

        if (totalForDownload==0)
        {
            totalForDownload = 100;
        }
        progressNotifier.setTotalData(totalForDownload);

        // create a list of mutexes
        final int numMutexPermits = updateList.size();
        final Semaphore mutex = new Semaphore(numMutexPermits);
        List<TransferUtility> transfers = new ArrayList<>();
        List<TransferObserver> observers = new ArrayList<>();


        int mutexPermitsBlocked = 0;
        try
        {
            for (final S3ObjectSummary objectSummary : updateList)
            {
                final File localFile = FileHelpers.createLocalDataFile(objectSummary.getKey(), true);
                final String awsLastModified = objectSummary.getLastModified().toString();
                if ( (localFile==null) || (!localFile.exists()) )
                {
                    return false;
                }
                if (localFile.isDirectory())
                {
                    continue;
                }

                // Starts a download
                TransferUtility tu = new TransferUtility(mS3, mAplicationContext);
                MetaioDebug.log(TAG + "[syncFilesInInternalMemory] start download" + objectSummary.getKey());
                TransferObserver observer = tu.download(objectSummary.getBucketName(), objectSummary.getKey(), localFile);

                mutex.acquire();
                mutexPermitsBlocked++;
                observer.setTransferListener(new TransferListener()
                {
                    @Override
                    public void onStateChanged(int id, TransferState newState)
                    {
                        MetaioDebug.log(TAG + "[syncFilesInInternalMemory] id = " + id + "; newState = " + newState.toString());
                        // Do something in the callback.
                        if (newState.equals(TransferState.COMPLETED))
                        {
                            overwriteInternalMetadataForFile(localFile, awsLastModified);
                            mutex.release();
                        }

                        if (newState.equals(TransferState.CANCELED)
                                || newState.equals(TransferState.FAILED)
                                || newState.equals(TransferState.WAITING_FOR_NETWORK))
                        {
                            // immediately cancel all downloads
                            mutex.release(numMutexPermits);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal)
                    {
                        // Do something in the callback.
                        MetaioDebug.log(TAG + "[syncFilesInInternalMemory]" + localFile.getAbsolutePath());
                        progressNotifier.postProgress(id, bytesCurrent, bytesTotal);
                    }

                    @Override
                    public void onError(int id, Exception e)
                    {
                        // Do something in the callback.
                    }
                });
                observers.add(observer);
                transfers.add(tu);
            }

            // now get all mutexes, i.e. wait until download finishes
            mutex.acquire(numMutexPermits);

            // pass through all observers and check for completion
            boolean success = true;
            for (int i=0; i<observers.size(); i++)
            {
                TransferObserver observer = observers.get(i);
                MetaioDebug.log(TAG + "[syncFilesInInternalMemory] final TransferState = " + observer.getState());
                if (!observer.getState().equals(TransferState.COMPLETED))
                {
                    success = false;
                    TransferUtility transfer = transfers.get(i);
                    transfer.cancel(observer.getId());
                }
            }
            if (!success)
            {
                return false;
            }
        }
        catch (InterruptedException exc)
        {
            MetaioDebug.log(TAG + "[syncFilesInInternalMemory] mutex failure");
            exc.printStackTrace();
            return false;
        }

        // Clear all files and folders not existing on AWS S3 any more
        File internalFolder = FileHelpers.getLocalDataFile(rootFolderRelativePath);

        if (s3Folders.size()>0 && internalFolder.exists() && internalFolder.isDirectory())
        {
            List<File> allSubFolders = FileHelpers.listAllSubFolders(internalFolder);
            allSubFolders.add(internalFolder);

            for (File subFolder : allSubFolders)
            {
                String relativePath = FileHelpers.setFolderDelimiters(FileHelpers.getRelativePath(subFolder.getAbsolutePath()));
                if (!s3Folders.contains(relativePath) && subFolder.exists())
                {
                    FileHelpers.deleteRecursive(subFolder);
                }
            }
        }
        return true;
    }

    public boolean uploadInternalFolder(File internalFolder, final String bucketName, final ProgressNotifier progressNotifier)
    {
        // Agenda
        //  1. create a recursive list of files that need to be uploaded
        //  2. compute the size of all files
        //  3. upload files one by one
        //  4. on success, for each of the files, get the object summary and create the .lm file

        long totalForUpload = 0;
        List<File> allFilesList = FileHelpers.listAllFilesAndFoldersRecursive(internalFolder);
        List<File> uploadList = new ArrayList<>();
        List<String> objectKeys = new ArrayList<>();
        for (File file : allFilesList)
        {
            final String key = FileHelpers.getRelativePath(file.getAbsolutePath());
            if (key.endsWith(INTERNAL_METADATA_EXTENSION))
            {
                continue;
            }
            totalForUpload += file.length();
            uploadList.add(file);
            objectKeys.add(key);
        }

        if (totalForUpload==0)
        {
            return false;
        }
        progressNotifier.setTotalData(totalForUpload);

        // create a list of mutexes
        boolean success = true;
        final Semaphore mutex = new Semaphore(uploadList.size());
        List<TransferObserver> observers = new ArrayList<>();
        try
        {
            for (int i=0; i<uploadList.size(); i++)
            {
                final File internalFile = uploadList.get(i);
                final String objectKey = objectKeys.get(i);

                if ( (internalFile==null) || (!internalFile.exists()) )
                {
                    return false;
                }
                if (internalFile.isDirectory())
                {
                    continue;
                }

                // Start an upload
                TransferUtility tu = new TransferUtility(mS3, mAplicationContext);
                MetaioDebug.log(TAG + "[uploadInternalFolder] start upload: " + objectKey);
                TransferObserver observer = tu.upload(bucketName, objectKey, internalFile);

                mutex.acquire();
                observer.setTransferListener(new TransferListener()
                {
                    @Override
                    public void onStateChanged(int id, TransferState newState)
                    {
                        MetaioDebug.log(TAG + "[uploadInternalFolder] id = " + id + "; newState = " + newState.toString());
                        if (newState.equals(TransferState.COMPLETED)
                                || newState.equals(TransferState.CANCELED)
                                || newState.equals(TransferState.FAILED)
                                || newState.equals(TransferState.WAITING_FOR_NETWORK))
                        {
                            mutex.release();
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal)
                    {
                        // Do something in the callback.
                        MetaioDebug.log(TAG + "[uploadInternalFolder]" + internalFile.getAbsolutePath());
                        progressNotifier.postProgress(id, bytesCurrent, bytesTotal);
                    }

                    @Override
                    public void onError(int id, Exception e)
                    {
                        // Do something in the callback.
                    }
                });
                observers.add(observer);
            }

            // now get all mutexes, i.e. wait until download finishes
            mutex.acquire(uploadList.size());

            // pass through all observers and check for completion
            for (TransferObserver observer : observers)
            {
                MetaioDebug.log(TAG + "[uploadInternalFolder] final TransferState = " + observer.getState());
                if (! observer.getState().equals(TransferState.COMPLETED))
                {
                    success = false;
                }
                else
                {
                    // update local metadata
                    final String objectKey = FileHelpers.getRelativePath(observer.getAbsoluteFilePath());
                    final File internalFile = FileHelpers.getLocalDataFile(objectKey);
                    if (internalFile!=null)
                    {
                        final ObjectMetadata metadata = mS3.getObjectMetadata(new GetObjectMetadataRequest(bucketName, objectKey));
                        final String awsLastModified = metadata.getLastModified().toString();
                        overwriteInternalMetadataForFile(internalFile, awsLastModified);
                    }
                }
            }
        }
        catch (InterruptedException exc)
        {
            MetaioDebug.log(TAG + "[uploadInternalFolder] mutex failure");
            exc.printStackTrace();
            success = false;
        }

        if (!success)
        {
            // delete any uploaded AWS "folder" objects, as the scene is incomplete
            final String key = FileHelpers.getRelativePath(internalFolder.getAbsolutePath());
            List<S3ObjectSummary> allObjects = listAllObjects(bucketName, key);

            // delete files
            for (S3ObjectSummary objectSummary : allObjects)
            {
                if (! objectSummary.getKey().endsWith("/"))
                {
                    mS3.deleteObject(bucketName, objectSummary.getKey());
                }
            }
            // delete folders
            for (int i=allObjects.size()-1; i>=0; i--)
            {
                if (allObjects.get(i).getKey().endsWith("/"))
                {
                    mS3.deleteObject(bucketName, allObjects.get(i).getKey());
                }
            }
        }

        return success;
    }
}
