package com.zumoko.metaiohelper.geometry;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector3d;

import java.io.File;

/**
 * Created by darsta on 30-Jan-16.
 */
public interface ARViewHelper
{
    String getAssetPath(String filename);
    File getAssetPathAsFile(String filename);

    boolean loadInitTrackingConfig(String path);

    IGeometry loadModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation);
    IGeometry loadVideoModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation, boolean transparent);
    IGeometry loadImageModel(String path, int cosId, float scale, Rotation rotation, Vector3d translation);

    void runOnGLThreadAfterInterval(final Runnable run, final long delay);

    void playAudio(String path, boolean loop);
    void stopAudio();
}
