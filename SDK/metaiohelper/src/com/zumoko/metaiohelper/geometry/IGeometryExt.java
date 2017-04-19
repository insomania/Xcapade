package com.zumoko.metaiohelper.geometry;

import com.metaio.sdk.jni.BoundingBox;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector3d;

/**
 * Created by darsta on 02-Jun-15.
 */
public class IGeometryExt
{
    private IGeometry mModel;
    private Vector3d mOriginalScale;
    private Vector3d mOriginalTranslation;
    private Rotation mOriginalRotation;
    private boolean mZeroRotationSet;

    private static final double OBJ_SCALE_LIMIT = 3.f;  // exp(OBJ_SCALE_LIMIT)
    private static final double OBJ_SCALE_STEPS = 16.f;   // total steps between +/- OBJ_SCALE_LIMIT
    private double              mObjScaleStepCurr = OBJ_SCALE_STEPS/2.0;  // always start from the half, because half = 0

    public IGeometryExt(IGeometry modelIn)
    {
        mModel = modelIn;
        mOriginalScale = modelIn.getScale();
        mOriginalTranslation = modelIn.getTranslation();
        mOriginalRotation = modelIn.getRotation();
        mZeroRotationSet = false;
    }

    public IGeometry model()
    {
        return mModel;
    }

    public void incModelsScale()
    {
        if (mObjScaleStepCurr < OBJ_SCALE_STEPS)
        {
            mObjScaleStepCurr += 1.0;
            reScale(computeScaleFactor(mObjScaleStepCurr), true);
        }
    }
    public void decModelsScale()
    {
        if (mObjScaleStepCurr > 0)
        {
            mObjScaleStepCurr -= 1.0;
            reScale(computeScaleFactor(mObjScaleStepCurr), true);
        }
    }

    private static float computeScaleFactor(final double step)
    {
        return (float) Math.exp( ((step /OBJ_SCALE_STEPS) * 2.0 * OBJ_SCALE_LIMIT) - OBJ_SCALE_LIMIT );
    }

    // rescale by considering original scale = 1
    private void reScale(float scaleFactor, boolean rescaleTranslation)
    {
        final Vector3d newScale = mOriginalScale.multiply(scaleFactor);
        mModel.setScale(newScale);
        if (rescaleTranslation)
        {
            Vector3d newTrans = mOriginalTranslation.multiply(scaleFactor);
            mModel.setTranslation(newTrans);
        }
    }

    public Vector3d getOriginalScale()
    {
        return mOriginalScale;
    }
    public Vector3d getOriginalTranslation()
    {
        return mOriginalTranslation;
    }
    public void applyOriginalRotation()
    {
        mZeroRotationSet = false;
        final Rotation currentRotation = mModel.getRotation();
        final Rotation originalRotation = mOriginalRotation.multiply(currentRotation);
        mModel.setRotation(originalRotation);
    }
    public void applyInverseOriginalRotation()
    {
        mZeroRotationSet = true;
        final Rotation currentRotation = mModel.getRotation();
        final Rotation originalRotation = mOriginalRotation.inverse().multiply(currentRotation);
        mModel.setRotation(originalRotation);
    }
    public boolean isInverseOriginalRotationSet()
    {
        return mZeroRotationSet;
    }
    public final Rotation getOriginalRotation()
    {
        return mOriginalRotation;
    }

    public static void enforce2DGeometryResolution(IGeometry geom, float forceX, float forceY)
    {
        final BoundingBox bb = geom.getBoundingBox(true);
        final Vector3d diff = bb.getMax().subtract(bb.getMin());

        final float scalerX = forceX / diff.getX();
        final float scalerY = forceY / diff.getY();
        final float scaler = (scalerX < scalerY) ? scalerX : scalerY;
        geom.setScale(geom.getScale().multiply(scaler));
    }
}
