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

    public void autoFocus() {
        if (standardCamera != null) {
            try {
                standardCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        Log.d(TAG, "autoFocus callback result: " + success);
                    }
                });
                Log.d(TAG, "Triggered camera autoFocus()");
            } catch (Throwable t) {
                Log.w(TAG, "autoFocus failed: " + t.getMessage());
            }
        }
    }

    public void cancelAutoFocus() {
        if (standardCamera != null) {
            try {
                standardCamera.cancelAutoFocus();
                Log.d(TAG, "Triggered camera cancelAutoFocus()");
            } catch (Throwable t) {
                Log.w(TAG, "cancelAutoFocus failed: " + t.getMessage());
            }
        }
    }

    public void takePicture() {
        if (standardCamera != null) {
            try {
                // ma1co CameraActivity pattern for CameraEx / standard camera:
                // Passing null callbacks allows Sony hardware BIONZ DSP to handle capture
                // and write RAW/ARW and JPEG directly to SD card without Dalvik heap pressure.
                standardCamera.takePicture(null, null, null);
                Log.i(TAG, "Triggered camera takePicture(null, null, null)");
            } catch (Throwable t) {
                Log.w(TAG, "takePicture(null, null, null) failed: " + t.getMessage() + ", trying fallback");
                try {
                    standardCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            try {
                                if (camera != null) {
                                    camera.startPreview();
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                } catch (Throwable t2) {
                    Log.e(TAG, "Fallback takePicture failed: " + t2.getMessage());
                }
            }
        }
    }

    public void cancelTakePicture() {
        if (sonyCameraEx != null) {
            try {
                Method cancelMethod = sonyCameraEx.getClass().getMethod("cancelTakePicture");
                cancelMethod.invoke(sonyCameraEx);
                Log.d(TAG, "Invoked CameraEx cancelTakePicture()");
            } catch (Throwable t) {
                Log.w(TAG, "CameraEx cancelTakePicture failed: " + t.getMessage());
            }
        }
        // Ensure preview resumes cleanly
        if (standardCamera != null) {
            try {
                standardCamera.startPreview();
            } catch (Throwable ignored) {
            }
        }
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
