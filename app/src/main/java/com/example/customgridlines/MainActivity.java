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

    private long lastBackPressTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2500; // 2.5 seconds

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
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

    // === SONY A6300 HARDWARE FOCUS & SHUTTER DETECTION ===
    private boolean isFocusKey(int keyCode, KeyEvent event) {
        int scanCode = (event != null) ? event.getScanCode() : 0;
        return keyCode == KeyEvent.KEYCODE_FOCUS
                || scanCode == 516 // S1 half-press
                || scanCode == 532 // AEL button (often assigned to Back-Button AF)
                || scanCode == 519 // AF/MF button/switch position
                || scanCode == 521;
    }

    private boolean isShutterKey(int keyCode, KeyEvent event) {
        int scanCode = (event != null) ? event.getScanCode() : 0;
        return keyCode == KeyEvent.KEYCODE_CAMERA
                || scanCode == 518; // S2 full shutter press
    }

    // === SONY A6300 HARDWARE KEY DISPATCH ===
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 1. Shutter half-press or Back-Button AF: Engage hardware autofocus
        if (isFocusKey(keyCode, event)) {
            if (hudOverlay != null && hudOverlay.isMenuVisible()) {
                hudOverlay.setMenuVisible(false);
            }
            cameraAdapter.autoFocus();
            return true;
        }

        // 2. Shutter full-press: Engage hardware photo capture
        if (isShutterKey(keyCode, event)) {
            if (hudOverlay != null && hudOverlay.isMenuVisible()) {
                hudOverlay.setMenuVisible(false);
            }
            cameraAdapter.takePicture();
            return true;
        }

        // 3. Dedicated BACK key handling for clear, safe app exit
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long now = System.currentTimeMillis();

            // If HUD menu is open, close menu and prompt
            if (hudOverlay != null && hudOverlay.isMenuVisible()) {
                hudOverlay.setMenuVisible(false);
                lastBackPressTime = now;
                if (gridOverlay != null) {
                    gridOverlay.showExitPrompt(BACK_PRESS_INTERVAL);
                }
                return true;
            }

            // In live view mode, double-press BACK within 2.5s cleanly exits app
            if (now - lastBackPressTime < BACK_PRESS_INTERVAL) {
                finish();
                return true;
            } else {
                lastBackPressTime = now;
                if (gridOverlay != null) {
                    gridOverlay.showExitPrompt(BACK_PRESS_INTERVAL);
                }
                return true;
            }
        }

        // 4. Delegate HUD menu navigation, column selection, colors, and stars
        if (hudOverlay != null && hudOverlay.handleKeyDown(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // 1. Releasing focus half-press / Back-Button AF
        if (isFocusKey(keyCode, event)) {
            cameraAdapter.cancelAutoFocus();
            return true;
        }

        // 2. Releasing shutter full-press
        if (isShutterKey(keyCode, event)) {
            cameraAdapter.cancelTakePicture();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }
}
