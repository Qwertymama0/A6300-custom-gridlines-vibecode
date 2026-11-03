package com.example.customgridlines;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements SurfaceHolder.Callback {

    private SonyCameraAdapter cameraAdapter;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private GridOverlayView gridOverlay;
    private HUDMenuOverlayView hudOverlay;
    private OverlayStateManager stateManager;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on while shooting
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraAdapter = new SonyCameraAdapter();
        stateManager = new OverlayStateManager(this);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF0D1117);

        // 1. Live camera surface preview
        surfaceView = new SurfaceView(this);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        rootLayout.addView(surfaceView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 2. Custom gridlines and aspect ratio overlay
        gridOverlay = new GridOverlayView(this);
        gridOverlay.setStateManager(stateManager);
        rootLayout.addView(gridOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 3. Hardware-navigable & touch HUD selection menu
        hudOverlay = new HUDMenuOverlayView(this, stateManager);
        rootLayout.addView(hudOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(rootLayout);

        // Android 6.0+ runtime permission check (guarded to avoid NoSuchMethodError on Android 2.3.7)
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestCameraPermission();
        }
    }

    private void checkAndRequestCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (surfaceHolder != null) {
                    cameraAdapter.openCamera(surfaceHolder);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        if (Build.VERSION.SDK_INT < 23) {
            // Android 2.3.7 (Sony A6300): Permission granted at install time
            cameraAdapter.openCamera(holder);
        } else {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                cameraAdapter.openCamera(holder);
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        cameraAdapter.releaseCamera();
        this.surfaceHolder = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (surfaceHolder != null) {
            cameraAdapter.openCamera(surfaceHolder);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraAdapter.releaseCamera();
    }

    // === HARDWARE KEYBOARD & SONY A6300 DIAL DISPATCH ===
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Shutter half-press autofocus support
        if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            cameraAdapter.triggerAutofocus();
            return true;
        }

        // Delegate navigation and toggling to HUD menu
        if (hudOverlay != null && hudOverlay.handleKeyDown(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
