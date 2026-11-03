package com.example.customgridlines;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.lang.reflect.Method;

public class SonyCameraAdapter {

    private static final String TAG = "SonyCameraAdapter";

    private Object sonyCameraEx;
    private Camera standardCamera;
    private boolean isSonyHardware = false;

    public boolean openCamera(SurfaceHolder holder) {
        releaseCamera();

        // 1. Try Sony OpenMemories CameraEx via reflection first
        try {
            Class<?> cameraExClass = Class.forName("com.sony.scalar.hardware.CameraEx");
            Method[] methods = cameraExClass.getMethods();
            Method openMethod = null;
            for (Method m : methods) {
                if ("open".equals(m.getName()) && m.getParameterTypes().length == 2) {
                    openMethod = m;
                    break;
                }
            }

            if (openMethod != null) {
                sonyCameraEx = openMethod.invoke(null, 0, null);
                if (sonyCameraEx != null) {
                    isSonyHardware = true;
                    Method getNormalCam = sonyCameraEx.getClass().getMethod("getNormalCamera");
                    standardCamera = (Camera) getNormalCam.invoke(sonyCameraEx);
                    Log.i(TAG, "Successfully initialized Sony CameraEx extension.");
                }
            }
        } catch (Throwable t) {
            Log.d(TAG, "Sony CameraEx not available on this platform: " + t.getMessage());
            isSonyHardware = false;
            sonyCameraEx = null;
        }

        // 2. Fallback to standard Android Camera API
        if (standardCamera == null) {
            try {
                standardCamera = Camera.open();
                Log.i(TAG, "Initialized standard Android Camera.");
            } catch (Throwable t) {
                Log.w(TAG, "Unable to open standard camera: " + t.getMessage());
                return false;
            }
        }

        // 3. Connect preview to SurfaceHolder
        if (standardCamera != null && holder != null) {
            try {
                standardCamera.setPreviewDisplay(holder);
                standardCamera.startPreview();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        return false;
    }

    public void releaseCamera() {
        if (sonyCameraEx != null) {
            try {
                Method releaseMethod = sonyCameraEx.getClass().getMethod("release");
                releaseMethod.invoke(sonyCameraEx);
            } catch (Throwable ignored) {
            }
            sonyCameraEx = null;
        }

        if (standardCamera != null) {
            try {
                standardCamera.stopPreview();
                standardCamera.release();
            } catch (Throwable ignored) {
            }
            standardCamera = null;
        }

        isSonyHardware = false;
    }

    public boolean isSonyHardware() {
        return isSonyHardware;
    }
}
