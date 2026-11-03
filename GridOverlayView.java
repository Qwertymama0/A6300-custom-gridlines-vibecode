package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.EnumSet;

public class GridOverlayView extends View {

    public enum GridType {
        RATIO_16_9,
        RATIO_1_1,
        RULE_OF_THIRDS,
        GOLDEN_RATIO
    }

    private EnumSet<GridType> activeGrids = EnumSet.of(
        GridType.RATIO_1_1, 
        GridType.GOLDEN_RATIO, 
        GridType.RULE_OF_THIRDS
    );

    private Paint linePaint;
    private Paint maskPaint;

    public GridOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(2f);
        linePaint.setStyle(Paint.Style.STROKE);

        maskPaint = new Paint();
        maskPaint.setColor(Color.BLACK);
        maskPaint.setAlpha(140); // Transparante afmaskering voor verhoudingen
        maskPaint.setStyle(Paint.Style.FILL);
    }

    public void toggleGrid(GridType grid) {
        if (activeGrids.contains(grid)) {
            activeGrids.remove(grid);
        } else {
            activeGrids.add(grid);
        }
        invalidate(); // Herteken het canvas
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(Canvas canvas);

        int width = getWidth();
        int height = getHeight();

        // 1. Aspect Ratios (Kaders)
        if (activeGrids.contains(GridType.RATIO_16_9)) {
            drawAspectRatio(canvas, width, height, 16f / 9f);
        }

        if (activeGrids.contains(GridType.RATIO_1_1)) {
            drawAspectRatio(canvas, width, height, 1f / 1f);
        }

        // 2. Compositielijnen (Rule of Thirds)
        if (activeGrids.contains(GridType.RULE_OF_THIRDS)) {
            // Horizontale derden
            canvas.drawLine(0, height / 3f, width, height / 3f, linePaint);
            canvas.drawLine(0, (height * 2f) / 3f, width, (height * 2f) / 3f, linePaint);
            // Verticale derden
            canvas.drawLine(width / 3f, 0, width / 3f, height, linePaint);
            canvas.drawLine((width * 2f) / 3f, 0, (width * 2f) / 3f, height, linePaint);
        }

        // 3. Golden Ratio (Phi = ~0.618)
        if (activeGrids.contains(GridType.GOLDEN_RATIO)) {
            float phi = 0.618f;
            
            // Lijnen op basis van de gulden snede
            float x1 = width * (1f - phi);
            float x2 = width * phi;
            float y1 = height * (1f - phi);
            float y2 = height * phi;

            canvas.drawLine(0, y1, width, y1, linePaint);
            canvas.drawLine(0, y2, width, y2, linePaint);
            canvas.drawLine(x1, 0, x1, height, linePaint);
            canvas.drawLine(x2, 0, x2, height, linePaint);
        }
    }

    private void drawAspectRatio(Canvas canvas, int width, int height, float targetRatio) {
        float currentRatio = (float) width / height;

        if (currentRatio > targetRatio) {
            // Scherm is breder dan gewenste ratio -> maskers links en rechts
            float targetWidth = height * targetRatio;
            float maskWidth = (width - targetWidth) / 2f;

            canvas.drawRect(0, 0, maskWidth, height, maskPaint);
            canvas.drawRect(width - maskWidth, 0, width, height, maskPaint);
            canvas.drawRect(maskWidth, 0, width - maskWidth, height, linePaint);
        } else {
            // Scherm is hoger dan gewenste ratio -> maskers boven en onder
            float targetHeight = width / targetRatio;
            float maskHeight = (height - targetHeight) / 2f;

            canvas.drawRect(0, 0, width, maskHeight, maskPaint);
            canvas.drawRect(0, height - maskHeight, width, height, maskPaint);
            canvas.drawRect(0, maskHeight, width, height - maskHeight, linePaint);
        }
    }
}
