package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.util.Set;

public class GridOverlayView extends View implements OverlayStateManager.OnStateChangeListener {

    private OverlayStateManager stateManager;
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint maskPaint = new Paint();
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF arcRect = new RectF();

    // Exit prompt overlay
    private long exitPromptUntil = 0;
    private final Paint exitBannerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint exitTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint exitBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF exitBannerRect = new RectF();

    private float displayScale = 1.0f;

    public GridOverlayView(Context context) {
        super(context);
        linePaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStyle(Paint.Style.STROKE);
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(0x8A000000); // 54% dark letterbox mask
        fillPaint.setStyle(Paint.Style.FILL);

        exitBannerPaint.setStyle(Paint.Style.FILL);
        exitBannerPaint.setColor(0xEE161B22);
        exitBorderPaint.setStyle(Paint.Style.STROKE);
        exitBorderPaint.setStrokeWidth(2f);
        exitBorderPaint.setColor(0xFFF85149); // Warning red accent border
        exitTextPaint.setColor(0xFFFFFFFF);
        exitTextPaint.setFakeBoldText(true);
        exitTextPaint.setTextSize(17f);
    }

    public void setStateManager(OverlayStateManager manager) {
        if (this.stateManager != null) {
            this.stateManager.removeListener(this);
        }
        this.stateManager = manager;
        if (this.stateManager != null) {
            this.stateManager.addListener(this);
        }
        invalidate();
    }

    public void showExitPrompt(long durationMs) {
        this.exitPromptUntil = System.currentTimeMillis() + durationMs;
        postInvalidate();
    }

    @Override
    public void onStateChanged() {
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        displayScale = (w >= 1000 || h >= 700) ? 1.35f : 1.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stateManager == null) return;

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float baseStroke = stateManager.getStrokeWeightPx() * displayScale;
        linePaint.setStrokeWidth(baseStroke);
        shadowPaint.setStrokeWidth(baseStroke + 2.0f);

        Set<OverlayType> active = stateManager.getActiveOverlays();

        // 1. Draw Aspect Ratio Letterbox Masks
        for (OverlayType type : active) {
            if (type.isAspectRatio) {
                drawAspectRatio(canvas, width, height, type.ratioValue, stateManager.getResolvedColor(type));
            }
        }

        // 2. Draw Composition Grids
        for (OverlayType type : active) {
            if (!type.isAspectRatio) {
                drawCompositionGrid(canvas, width, height, type, stateManager.getResolvedColor(type));
            }
        }

        // 3. Draw Exit Confirmation Banner if active
        if (System.currentTimeMillis() < exitPromptUntil) {
            drawExitPromptBanner(canvas, width, height);
            postInvalidateDelayed(100);
        }
    }

    private void drawExitPromptBanner(Canvas canvas, int width, int height) {
        String msg = "✕ Druk nogmaals op [BACK] om GridLab af te sluiten";
        exitTextPaint.setTextSize(16f * displayScale);
        float tw = exitTextPaint.measureText(msg);

        float padH = 24f * displayScale;
        float padV = 14f * displayScale;
        float bannerW = tw + (padH * 2f);
        float bannerH = 46f * displayScale;

        float cx = width / 2f;
        float cy = height * 0.78f; // Near bottom-center so view stays visible
        exitBannerRect.set(cx - bannerW / 2f, cy - bannerH / 2f, cx + bannerW / 2f, cy + bannerH / 2f);

        canvas.drawRoundRect(exitBannerRect, 8f, 8f, exitBannerPaint);
        canvas.drawRoundRect(exitBannerRect, 8f, 8f, exitBorderPaint);
        canvas.drawText(msg, cx - tw / 2f, cy + 6f * displayScale, exitTextPaint);
    }

    private void drawAspectRatio(Canvas canvas, int width, int height, float targetRatio, int color) {
        float currentRatio = (float) width / height;

        if (currentRatio > targetRatio) {
            float targetWidth = height * targetRatio;
            float maskWidth = (width - targetWidth) / 2f;
            canvas.drawRect(0, 0, maskWidth, height, maskPaint);
            canvas.drawRect(width - maskWidth, 0, width, height, maskPaint);
            drawHighContrastRect(canvas, maskWidth, 0, width - maskWidth, height, color);
        } else {
            float targetHeight = width / targetRatio;
            float maskHeight = (height - targetHeight) / 2f;
            canvas.drawRect(0, 0, width, maskHeight, maskPaint);
            canvas.drawRect(0, height - maskHeight, width, height, maskPaint);
            drawHighContrastRect(canvas, 0, maskHeight, width, height - maskHeight, color);
        }
    }

    private void drawHighContrastLine(Canvas canvas, float x1, float y1, float x2, float y2, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawLine(x1, y1, x2, y2, shadowPaint);
        linePaint.setColor(color);
        canvas.drawLine(x1, y1, x2, y2, linePaint);
    }

    private void drawHighContrastRect(Canvas canvas, float l, float t, float r, float b, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawRect(l, t, r, b, shadowPaint);
        linePaint.setColor(color);
        canvas.drawRect(l, t, r, b, linePaint);
    }

    private void drawHighContrastCircle(Canvas canvas, float cx, float cy, float radius, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawCircle(cx, cy, radius, shadowPaint);
        linePaint.setColor(color);
        canvas.drawCircle(cx, cy, radius, linePaint);
    }

    private void drawCompositionGrid(Canvas canvas, int width, int height, OverlayType type, int color) {
        float w = (float) width;
        float h = (float) height;

        switch (type) {
            case GRID_RULE_OF_THIRDS: {
                float x1 = w / 3f, x2 = (w * 2f) / 3f;
                float y1 = h / 3f, y2 = (h * 2f) / 3f;
                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);

                fillPaint.setColor(color);
                float dotR = 4.5f * displayScale;
                canvas.drawCircle(x1, y1, dotR, fillPaint);
                canvas.drawCircle(x2, y1, dotR, fillPaint);
                canvas.drawCircle(x1, y2, dotR, fillPaint);
                canvas.drawCircle(x2, y2, dotR, fillPaint);
                break;
            }

            case GRID_GOLDEN_RATIO: {
                float phi = 0.618034f;
                float x1 = w * (1f - phi), x2 = w * phi;
                float y1 = h * (1f - phi), y2 = h * phi;
                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);
                break;
            }

            case GRID_GOLDEN_SPIRAL:
                drawOrientedGoldenSpiral(canvas, w, h, color, stateManager.getSpiralCornerIndex());
                break;

            case GRID_PHI_GRID: {
                float x1 = w * 0.382f, x2 = w * 0.618f;
                float y1 = h * 0.382f, y2 = h * 0.618f;
                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);
                break;
            }

            case GRID_DYNAMIC_SYMMETRY: {
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                drawHighContrastLine(canvas, 0, h, w, 0, color);
                float m = h / w;
                float xRecip = (w * m * m) / (1f + m * m);
                float yRecip = h / (1f + m * m);
                drawHighContrastLine(canvas, 0, 0, xRecip, yRecip, color);
                drawHighContrastLine(canvas, w, h, w - xRecip, h - yRecip, color);
                drawHighContrastLine(canvas, 0, h, xRecip, h - yRecip, color);
                drawHighContrastLine(canvas, w, 0, w - xRecip, yRecip, color);
                break;
            }

            case GRID_DIAGONAL_METHOD: {
                float minDim = Math.min(w, h);
                drawHighContrastLine(canvas, 0, 0, minDim, minDim, color);
                drawHighContrastLine(canvas, w, 0, w - minDim, minDim, color);
                drawHighContrastLine(canvas, 0, h, minDim, h - minDim, color);
                drawHighContrastLine(canvas, w, h, w - minDim, h - minDim, color);
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                drawHighContrastLine(canvas, 0, h, w, 0, color);
                break;
            }

            case GRID_BAROQUE_DIAGONAL:
                drawHighContrastLine(canvas, 0, h, w, 0, color);
                break;

            case GRID_SINISTER_DIAGONAL:
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                break;

            case GRID_TRIANGULAR:
                drawHighContrastLine(canvas, w / 2f, 0, 0, h, color);
                drawHighContrastLine(canvas, w / 2f, 0, w, h, color);
                drawHighContrastLine(canvas, w / 2f, h, 0, 0, color);
                drawHighContrastLine(canvas, w / 2f, h, w, 0, color);
                break;

            case GRID_CENTRAL: {
                float cx = w / 2f, cy = h / 2f;
                drawHighContrastLine(canvas, cx - 40f, cy, cx - 8f, cy, color);
                drawHighContrastLine(canvas, cx + 8f, cy, cx + 40f, cy, color);
                drawHighContrastLine(canvas, cx, cy - 40f, cx, cy - 8f, color);
                drawHighContrastLine(canvas, cx, cy + 8f, cx, cy + 40f, color);
                drawHighContrastCircle(canvas, cx, cy, 25f * displayScale, color);
                drawHighContrastCircle(canvas, cx, cy, Math.min(w, h) * 0.28f, color);
                break;
            }

            case GRID_HORIZON_LEVEL: {
                float cx = w / 2f, cy = h / 2f;
                float wingLen = Math.min(w, h) * 0.40f;
                drawHighContrastLine(canvas, cx - wingLen, cy, cx - 35f, cy, color);
                drawHighContrastLine(canvas, cx + 35f, cy, cx + wingLen, cy, color);
                drawHighContrastLine(canvas, cx - 15f, cy - 8f, cx + 15f, cy - 8f, color);
                drawHighContrastCircle(canvas, cx, cy, 3f * displayScale, color);
                break;
            }

            case GRID_RADIAL: {
                float cx = w / 2f, cy = h / 2f;
                drawHighContrastLine(canvas, 0, 0, cx, cy, color);
                drawHighContrastLine(canvas, w, 0, cx, cy, color);
                drawHighContrastLine(canvas, 0, h, cx, cy, color);
                drawHighContrastLine(canvas, w, h, cx, cy, color);
                drawHighContrastLine(canvas, cx, 0, cx, cy, color);
                drawHighContrastLine(canvas, cx, h, cx, cy, color);
                drawHighContrastLine(canvas, 0, cy, cx, cy, color);
                drawHighContrastLine(canvas, w, cy, cx, cy, color);
                break;
            }

            case GRID_2_COLUMN:
                drawHighContrastLine(canvas, w / 2f, 0, w / 2f, h, color);
                break;

            case GRID_3_COLUMN:
                drawHighContrastLine(canvas, w / 3f, 0, w / 3f, h, color);
                drawHighContrastLine(canvas, (w * 2f) / 3f, 0, (w * 2f) / 3f, h, color);
                break;

            case GRID_4_COLUMN:
                drawHighContrastLine(canvas, w * 0.25f, 0, w * 0.25f, h, color);
                drawHighContrastLine(canvas, w * 0.50f, 0, w * 0.50f, h, color);
                drawHighContrastLine(canvas, w * 0.75f, 0, w * 0.75f, h, color);
                break;

            case GRID_RABATMENT: {
                if (w > h) {
                    drawHighContrastLine(canvas, h, 0, h, h, color);
                    drawHighContrastLine(canvas, 0, 0, h, h, color);
                    drawHighContrastLine(canvas, 0, h, h, 0, color);
                    float rx = w - h;
                    drawHighContrastLine(canvas, rx, 0, rx, h, color);
                    drawHighContrastLine(canvas, rx, 0, w, h, color);
                    drawHighContrastLine(canvas, rx, h, w, 0, color);
                } else if (h > w) {
                    drawHighContrastLine(canvas, 0, w, w, w, color);
                    drawHighContrastLine(canvas, 0, 0, w, w, color);
                    drawHighContrastLine(canvas, 0, w, w, 0, color);
                    float by = h - w;
                    drawHighContrastLine(canvas, 0, by, w, by, color);
                    drawHighContrastLine(canvas, 0, by, w, h, color);
                    drawHighContrastLine(canvas, 0, h, w, by, color);
                }
                break;
            }
        }
    }

    private void drawOrientedGoldenSpiral(Canvas canvas, float w, float h, int color, int corner) {
        canvas.save();
        if (corner == 1) canvas.scale(-1f, 1f, w / 2f, h / 2f);
        else if (corner == 2) canvas.scale(-1f, -1f, w / 2f, h / 2f);
        else if (corner == 3) canvas.scale(1f, -1f, w / 2f, h / 2f);

        path.reset();
        float phi = 0.618034f, x = 0, y = 0, curW = w, curH = h;

        for (int i = 0; i < 6; i++) {
            switch (i % 4) {
                case 0: {
                    float cut = curW * (1f - phi), subW = curW - cut;
                    drawHighContrastRect(canvas, x, y, x + subW, y + curH, color);
                    arcRect.set(x, y - curH, x + 2 * subW, y + curH);
                    path.arcTo(arcRect, 90f, 90f);
                    x += subW; curW = cut;
                    break;
                }
                case 1: {
                    float cut = curH * (1f - phi), subH = curH - cut;
                    drawHighContrastRect(canvas, x, y, x + curW, y + subH, color);
                    arcRect.set(x, y, x + 2 * curW, y + 2 * subH);
                    path.arcTo(arcRect, 180f, 90f);
                    y += subH; curH = cut;
                    break;
                }
                case 2: {
                    float cut = curW * (1f - phi), subW = curW - cut;
                    drawHighContrastRect(canvas, x + cut, y, x + curW, y + curH, color);
                    arcRect.set(x - cut, y, x + curW + cut, y + 2 * curH);
                    path.arcTo(arcRect, 270f, 90f);
                    curW = cut;
                    break;
                }
                case 3: {
                    float cut = curH * (1f - phi), subH = curH - cut;
                    drawHighContrastRect(canvas, x, y + cut, x + curW, y + curH, color);
                    arcRect.set(x - curW, y - cut, x + curW, y + curH);
                    path.arcTo(arcRect, 0f, 90f);
                    curH = cut;
                    break;
                }
            }
        }
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawPath(path, shadowPaint);
        linePaint.setColor(color);
        canvas.drawPath(path, linePaint);
        canvas.restore();
    }
}
