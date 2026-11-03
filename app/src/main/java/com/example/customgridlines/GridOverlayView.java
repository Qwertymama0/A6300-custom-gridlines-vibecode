package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Set;

public class GridOverlayView extends View implements OverlayStateManager.OnStateChangeListener {

    private OverlayStateManager stateManager;
    private Paint linePaint;
    private Paint shadowPaint;
    private Paint maskPaint;
    private Paint fillPaint;
    private Path path;
    private RectF arcRect;

    public GridOverlayView(Context context) {
        super(context);
        init();
    }

    public GridOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(4.5f);
        shadowPaint.setColor(0x88000000); // 50% black shadow outline

        maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setColor(0x8A000000); // ~54% opaque black letterbox mask

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        path = new Path();
        arcRect = new RectF();
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

    @Override
    public void onStateChanged() {
        postInvalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (stateManager != null) {
            stateManager.removeListener(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (stateManager == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        Set<OverlayType> activeOverlays = stateManager.getActiveOverlays();

        // 1. Draw Aspect Ratio Letterbox Masks first so grids sit on top
        for (OverlayType type : activeOverlays) {
            if (type.isAspectRatio) {
                int color = stateManager.getResolvedColor(type);
                drawAspectRatio(canvas, width, height, type.ratioValue, color);
            }
        }

        // 2. Draw Composition Grids
        for (OverlayType type : activeOverlays) {
            if (!type.isAspectRatio) {
                int color = stateManager.getResolvedColor(type);
                drawCompositionGrid(canvas, width, height, type, color);
            }
        }
    }

    private void drawAspectRatio(Canvas canvas, int width, int height, float targetRatio, int color) {
        float currentRatio = (float) width / height;

        linePaint.setColor(color);

        if (currentRatio > targetRatio) {
            // Screen is wider than target ratio -> letterbox on left and right
            float targetWidth = height * targetRatio;
            float maskWidth = (width - targetWidth) / 2f;

            canvas.drawRect(0, 0, maskWidth, height, maskPaint);
            canvas.drawRect(width - maskWidth, 0, width, height, maskPaint);

            // Framing border line
            drawHighContrastRect(canvas, maskWidth, 0, width - maskWidth, height, color);
        } else {
            // Screen is taller than target ratio -> letterbox on top and bottom
            float targetHeight = width / targetRatio;
            float maskHeight = (height - targetHeight) / 2f;

            canvas.drawRect(0, 0, width, maskHeight, maskPaint);
            canvas.drawRect(0, height - maskHeight, width, height, maskPaint);

            // Framing border line
            drawHighContrastRect(canvas, 0, maskHeight, width, height - maskHeight, color);
        }
    }

    private void drawHighContrastLine(Canvas canvas, float startX, float startY, float stopX, float stopY, int color) {
        // Draw dark shadow contour
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawLine(startX, startY, stopX, stopY, shadowPaint);

        // Draw main colored line
        linePaint.setColor(color);
        canvas.drawLine(startX, startY, stopX, stopY, linePaint);
    }

    private void drawHighContrastRect(Canvas canvas, float left, float top, float right, float bottom, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawRect(left, top, right, bottom, shadowPaint);

        linePaint.setColor(color);
        canvas.drawRect(left, top, right, bottom, linePaint);
    }

    private void drawHighContrastCircle(Canvas canvas, float cx, float cy, float radius, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawCircle(cx, cy, radius, shadowPaint);

        linePaint.setColor(color);
        canvas.drawCircle(cx, cy, radius, linePaint);
    }

    private void drawHighContrastPath(Canvas canvas, Path p, int color) {
        shadowPaint.setColor(color == 0xFF000000 ? 0x88FFFFFF : 0x88000000);
        canvas.drawPath(p, shadowPaint);

        linePaint.setColor(color);
        canvas.drawPath(p, linePaint);
    }

    private void drawCompositionGrid(Canvas canvas, int width, int height, OverlayType type, int color) {
        float w = (float) width;
        float h = (float) height;

        switch (type) {
            case GRID_RULE_OF_THIRDS: {
                float x1 = w / 3f;
                float x2 = (w * 2f) / 3f;
                float y1 = h / 3f;
                float y2 = (h * 2f) / 3f;

                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);

                // Power points markers (small circles at intersections)
                fillPaint.setColor(color);
                canvas.drawCircle(x1, y1, 5f, fillPaint);
                canvas.drawCircle(x2, y1, 5f, fillPaint);
                canvas.drawCircle(x1, y2, 5f, fillPaint);
                canvas.drawCircle(x2, y2, 5f, fillPaint);
                break;
            }

            case GRID_GOLDEN_RATIO: {
                float phi = 0.618034f;
                float x1 = w * (1f - phi);
                float x2 = w * phi;
                float y1 = h * (1f - phi);
                float y2 = h * phi;

                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);
                break;
            }

            case GRID_GOLDEN_SPIRAL: {
                drawGoldenSpiral(canvas, w, h, color);
                break;
            }

            case GRID_PHI_GRID: {
                float x1 = w * 0.382f;
                float x2 = w * 0.618f;
                float y1 = h * 0.382f;
                float y2 = h * 0.618f;

                drawHighContrastLine(canvas, 0, y1, w, y1, color);
                drawHighContrastLine(canvas, 0, y2, w, y2, color);
                drawHighContrastLine(canvas, x1, 0, x1, h, color);
                drawHighContrastLine(canvas, x2, 0, x2, h, color);
                break;
            }

            case GRID_DYNAMIC_SYMMETRY: {
                // Diagonals
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                drawHighContrastLine(canvas, 0, h, w, 0, color);

                // Perpendicular Reciprocals:
                // Normal to line from (0,h) to (w,0) has slope w/h. Line from (0,0) perpendicular hits (x,y)
                // Let aspect m = h/w
                float m = h / w;
                float xRecip1 = (w * m * m) / (1f + m * m);
                float yRecip1 = (h) / (1f + m * m);
                drawHighContrastLine(canvas, 0, 0, xRecip1, yRecip1, color);
                drawHighContrastLine(canvas, w, h, w - xRecip1, h - yRecip1, color);

                drawHighContrastLine(canvas, 0, h, xRecip1, h - yRecip1, color);
                drawHighContrastLine(canvas, w, 0, w - xRecip1, yRecip1, color);
                break;
            }

            case GRID_DIAGONAL_METHOD: {
                float minDim = Math.min(w, h);
                drawHighContrastLine(canvas, 0, 0, minDim, minDim, color);
                drawHighContrastLine(canvas, w, 0, w - minDim, minDim, color);
                drawHighContrastLine(canvas, 0, h, minDim, h - minDim, color);
                drawHighContrastLine(canvas, w, h, w - minDim, h - minDim, color);

                // Full corner diagonals
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                drawHighContrastLine(canvas, 0, h, w, 0, color);
                break;
            }

            case GRID_BAROQUE_DIAGONAL: {
                // Ascending bottom-left to top-right
                drawHighContrastLine(canvas, 0, h, w, 0, color);
                break;
            }

            case GRID_SINISTER_DIAGONAL: {
                // Descending top-left to bottom-right
                drawHighContrastLine(canvas, 0, 0, w, h, color);
                break;
            }

            case GRID_TRIANGULAR: {
                // Apex at top center
                drawHighContrastLine(canvas, w / 2f, 0, 0, h, color);
                drawHighContrastLine(canvas, w / 2f, 0, w, h, color);
                // Inverse apex at bottom center
                drawHighContrastLine(canvas, w / 2f, h, 0, 0, color);
                drawHighContrastLine(canvas, w / 2f, h, w, 0, color);
                break;
            }

            case GRID_CENTRAL: {
                float cx = w / 2f;
                float cy = h / 2f;

                // Center crosshair with center gap
                drawHighContrastLine(canvas, cx - 40f, cy, cx - 8f, cy, color);
                drawHighContrastLine(canvas, cx + 8f, cy, cx + 40f, cy, color);
                drawHighContrastLine(canvas, cx, cy - 40f, cx, cy - 8f, color);
                drawHighContrastLine(canvas, cx, cy + 8f, cx, cy + 40f, color);

                // Inner and outer concentric target circles
                drawHighContrastCircle(canvas, cx, cy, 25f, color);
                drawHighContrastCircle(canvas, cx, cy, Math.min(w, h) * 0.28f, color);
                break;
            }

            case GRID_RADIAL: {
                float cx = w / 2f;
                float cy = h / 2f;

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

            case GRID_2_COLUMN: {
                drawHighContrastLine(canvas, w / 2f, 0, w / 2f, h, color);
                break;
            }

            case GRID_3_COLUMN: {
                drawHighContrastLine(canvas, w / 3f, 0, w / 3f, h, color);
                drawHighContrastLine(canvas, (w * 2f) / 3f, 0, (w * 2f) / 3f, h, color);
                break;
            }

            case GRID_4_COLUMN: {
                drawHighContrastLine(canvas, w * 0.25f, 0, w * 0.25f, h, color);
                drawHighContrastLine(canvas, w * 0.50f, 0, w * 0.50f, h, color);
                drawHighContrastLine(canvas, w * 0.75f, 0, w * 0.75f, h, color);
                break;
            }

            case GRID_RABATMENT: {
                if (w > h) {
                    // Left square rabatment
                    drawHighContrastLine(canvas, h, 0, h, h, color);
                    drawHighContrastLine(canvas, 0, 0, h, h, color);
                    drawHighContrastLine(canvas, 0, h, h, 0, color);

                    // Right square rabatment
                    float rx = w - h;
                    drawHighContrastLine(canvas, rx, 0, rx, h, color);
                    drawHighContrastLine(canvas, rx, 0, w, h, color);
                    drawHighContrastLine(canvas, rx, h, w, 0, color);
                } else if (h > w) {
                    // Top square
                    drawHighContrastLine(canvas, 0, w, w, w, color);
                    drawHighContrastLine(canvas, 0, 0, w, w, color);
                    drawHighContrastLine(canvas, 0, w, w, 0, color);

                    // Bottom square
                    float by = h - w;
                    drawHighContrastLine(canvas, 0, by, w, by, color);
                    drawHighContrastLine(canvas, 0, by, w, h, color);
                    drawHighContrastLine(canvas, 0, h, w, by, color);
                }
                break;
            }

            default:
                break;
        }
    }

    private void drawGoldenSpiral(Canvas canvas, float w, float h, int color) {
        path.reset();

        // Calculate Fibonacci rectangles bounding coordinates
        float phi = 0.618034f;
        float x = 0;
        float y = 0;
        float curW = w;
        float curH = h;

        // Draw bounding rectangles and connecting spiral quarter arcs
        for (int i = 0; i < 6; i++) {
            switch (i % 4) {
                case 0: { // Cut right section
                    float cut = curW * (1f - phi);
                    float subW = curW - cut;
                    drawHighContrastRect(canvas, x, y, x + subW, y + curH, color);

                    arcRect.set(x, y - curH, x + 2 * subW, y + curH);
                    path.arcTo(arcRect, 90f, 90f);

                    x += subW;
                    curW = cut;
                    break;
                }
                case 1: { // Cut bottom section
                    float cut = curH * (1f - phi);
                    float subH = curH - cut;
                    drawHighContrastRect(canvas, x, y, x + curW, y + subH, color);

                    arcRect.set(x, y, x + 2 * curW, y + 2 * subH);
                    path.arcTo(arcRect, 180f, 90f);

                    y += subH;
                    curH = cut;
                    break;
                }
                case 2: { // Cut left section
                    float cut = curW * (1f - phi);
                    float subW = curW - cut;
                    drawHighContrastRect(canvas, x + cut, y, x + curW, y + curH, color);

                    arcRect.set(x - cut, y, x + curW + cut, y + 2 * curH);
                    path.arcTo(arcRect, 270f, 90f);

                    curW = cut;
                    break;
                }
                case 3: { // Cut top section
                    float cut = curH * (1f - phi);
                    float subH = curH - cut;
                    drawHighContrastRect(canvas, x, y + cut, x + curW, y + curH, color);

                    arcRect.set(x - curW, y - cut, x + curW, y + curH);
                    path.arcTo(arcRect, 0f, 90f);

                    curH = cut;
                    break;
                }
            }
        }

        drawHighContrastPath(canvas, path, color);
    }
}
