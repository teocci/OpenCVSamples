package com.kseek.opencvsamples.views;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;
import java.util.List;

public class MainView extends JavaCameraView implements PictureCallback {

    private static final String TAG = "MainView";
    private String pictureFileName;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public List<String> getEffectList() {
        return camera.getParameters().getSupportedColorEffects();
    }

    public boolean isEffectSupported() {
        return (camera.getParameters().getColorEffect() != null);
    }

    public String getEffect() {
        return camera.getParameters().getColorEffect();
    }

    public void setEffect(String effect) {
        Camera.Parameters params = camera.getParameters();
        params.setColorEffect(effect);
        camera.setParameters(params);
    }

    public List<Size> getResolutionList() {
        return camera.getParameters().getSupportedPreviewSizes();
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

    public Size getResolution() {
        return camera.getParameters().getPreviewSize();
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.pictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
        // Clear up buffers to avoid camera.takePicture to be stuck because of a memory issue
        camera.setPreviewCallback(null);

        // PictureCallback is implemented by the current class
        camera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it again.
        camera.startPreview();
        camera.setPreviewCallback(this);

        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(pictureFileName);

            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }
}
