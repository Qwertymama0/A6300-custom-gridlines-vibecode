package com.example.customgridlines;

import android.app.Activity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraAdapter = new SonyCameraAdapter();
        stateManager = new OverlayStateManager(this);

        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(0xFF0D1117);

        // 1. Live camera preview
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

        // 3. Sony hardware D-Pad HUD menu
        hudOverlay = new HUDMenuOverlayView(this, stateManager);
        rootLayout.addView(hudOverlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(rootLayout);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        cameraAdapter.openCamera(holder);
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

    // === SONY A6300 HARDWARE KEY DISPATCH ===
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Native shooting pass-through: NEVER intercept focus or shutter keys
        if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return super.onKeyDown(keyCode, event);
        }

        // Delegate navigation and toggling strictly to HUD menu
        if (hudOverlay != null && hudOverlay.handleKeyDown(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
