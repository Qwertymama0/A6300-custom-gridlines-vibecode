package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

public class HUDMenuOverlayView extends View implements OverlayStateManager.OnStateChangeListener {

    private final OverlayStateManager stateManager;
    private boolean isVisible = true;

    private OverlayType.Category currentCategory = OverlayType.Category.FAVORITES;
    private int selectedRowIndex = 0;
    private int scrollOffset = 0;

    private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint colorPillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF boundsRect = new RectF();
    private final RectF itemRect = new RectF();
    private final RectF tabRect = new RectF();
    private final RectF boxRect = new RectF();
    private final RectF pillRect = new RectF();

    public HUDMenuOverlayView(Context context, OverlayStateManager stateManager) {
        super(context);
        this.stateManager = stateManager;
        this.stateManager.addListener(this);

        setFocusable(true);
        setFocusableInTouchMode(true);

        cardPaint.setColor(0xEE161B22);
        textPaint.setColor(0xFFF0F6FC);
        textPaint.setTextSize(17f);
        boldTextPaint.setColor(0xFFFFFFFF);
        boldTextPaint.setFakeBoldText(true);
        boldTextPaint.setTextSize(19f);
        subtitlePaint.setColor(0xFF8B949E);
        subtitlePaint.setTextSize(12f);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        colorPillPaint.setStyle(Paint.Style.FILL);
    }

    public boolean isMenuVisible() {
        return isVisible;
    }

    public void setMenuVisible(boolean visible) {
        this.isVisible = visible;
        invalidate();
    }

    public void toggleMenuVisible() {
        setMenuVisible(!isVisible);
    }

    @Override
    public void onStateChanged() {
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isVisible) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float panelW = Math.min(width * 0.92f, 740f);
        float panelH = Math.min(height * 0.90f, 450f);
        float panelX = (width - panelW) / 2f;
        float panelY = (height - panelH) / 2f;
        boundsRect.set(panelX, panelY, panelX + panelW, panelY + panelH);

        // Panel background
        cardPaint.setColor(0xEE161B22);
        canvas.drawRoundRect(boundsRect, 12f, 12f, cardPaint);
        borderPaint.setColor(0xFF388BFD);
        borderPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(boundsRect, 12f, 12f, borderPaint);

        // Header Title
        boldTextPaint.setColor(0xFFFFFFFF);
        boldTextPaint.setTextSize(18f);
        canvas.drawText("GRIDLAB | SONY A6300", panelX + 20f, panelY + 28f, boldTextPaint);

        // Quick Settings Info
        subtitlePaint.setColor(0xFF58A6FF);
        subtitlePaint.setTextSize(12f);
        String info = "Dikte: " + stateManager.getStrokeWeightName() + " | Spiraal: " + stateManager.getSpiralCornerName();
        canvas.drawText(info, panelX + panelW - 270f, panelY + 28f, subtitlePaint);

        // Category Tabs
        drawCategoryTabs(canvas, panelX, panelY + 44f, panelW);

        // Item List
        drawItemList(canvas, panelX, panelY + 86f, panelW, panelH - 122f);

        // Footer Navigation Guide
        drawFooter(canvas, panelX, panelY + panelH - 28f, panelW);
    }

    private void drawCategoryTabs(Canvas canvas, float x, float y, float w) {
        OverlayType.Category[] categories = OverlayType.Category.values();
        float tabW = (w - 24f) / categories.length;
        float tabH = 32f;

        for (int i = 0; i < categories.length; i++) {
            OverlayType.Category cat = categories[i];
            float tx = x + 12f + (i * tabW);
            tabRect.set(tx, y, tx + tabW - 4f, y + tabH);

            boolean isCurrent = (cat == currentCategory);
            cardPaint.setColor(isCurrent ? 0xFF1F6FEB : 0xFF21262D);
            canvas.drawRoundRect(tabRect, 6f, 6f, cardPaint);

            textPaint.setColor(isCurrent ? 0xFFFFFFFF : 0xFFC9D1D9);
            textPaint.setTextSize(12f);
            textPaint.setFakeBoldText(isCurrent);

            String title = cat.title;
            float tw = textPaint.measureText(title);
            canvas.drawText(title, tx + (tabW - 4f - tw) / 2f, y + 21f, textPaint);
        }
    }

    private void drawItemList(Canvas canvas, float x, float y, float w, float h) {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);

        if (items.isEmpty()) {
            textPaint.setColor(0xFF8B949E);
            textPaint.setTextSize(14f);
            String emptyMsg = currentCategory == OverlayType.Category.FAVORITES
                ? "Geen favorieten. Druk op [C1] om rasters als favoriet te markeren."
                : "Geen items in deze categorie.";
            canvas.drawText(emptyMsg, x + 30f, y + 40f, textPaint);
            return;
        }

        if (selectedRowIndex >= items.size()) {
            selectedRowIndex = Math.max(0, items.size() - 1);
        }

        float rowH = 44f;
        int visibleRows = (int) (h / rowH);

        if (selectedRowIndex < scrollOffset) {
            scrollOffset = selectedRowIndex;
        } else if (selectedRowIndex >= scrollOffset + visibleRows) {
            scrollOffset = selectedRowIndex - visibleRows + 1;
        }

        int maxRows = Math.min(items.size(), scrollOffset + visibleRows);

        for (int i = scrollOffset; i < maxRows; i++) {
            OverlayType type = items.get(i);
            float rowY = y + ((i - scrollOffset) * rowH);
            itemRect.set(x + 12f, rowY, x + w - 12f, rowY + rowH - 4f);

            boolean isFocused = (i == selectedRowIndex);
            boolean isActive = stateManager.isActive(type);
            boolean isFav = stateManager.isFavorite(type);
            int resolvedColor = stateManager.getResolvedColor(type);
            String colorName = ColorPalette.getColorName(stateManager.getColorIndex(type));

            cardPaint.setColor(isFocused ? 0xFF21262D : 0xFF161B22);
            canvas.drawRoundRect(itemRect, 6f, 6f, cardPaint);

            if (isFocused) {
                borderPaint.setColor(0xFF388BFD);
                borderPaint.setStrokeWidth(1.5f);
                canvas.drawRoundRect(itemRect, 6f, 6f, borderPaint);

                textPaint.setColor(0xFF58A6FF);
                textPaint.setTextSize(15f);
                canvas.drawText("▸", x + 18f, rowY + 26f, textPaint);
            }

            // Checkbox [✓] or [ ]
            boxRect.set(x + 36f, rowY + 10f, x + 58f, rowY + 32f);
            cardPaint.setColor(isActive ? 0xFF1F6FEB : 0xFF0D1117);
            canvas.drawRoundRect(boxRect, 4f, 4f, cardPaint);
            borderPaint.setColor(isActive ? 0xFF58A6FF : 0xFF484F58);
            borderPaint.setStrokeWidth(1.5f);
            canvas.drawRoundRect(boxRect, 4f, 4f, borderPaint);

            if (isActive) {
                textPaint.setColor(0xFFFFFFFF);
                textPaint.setTextSize(14f);
                textPaint.setFakeBoldText(true);
                canvas.drawText("✓", x + 41f, rowY + 26f, textPaint);
            }

            // Title & Subtitle
            boldTextPaint.setColor(isActive ? 0xFFFFFFFF : 0xFFC9D1D9);
            boldTextPaint.setTextSize(14f);
            canvas.drawText(type.displayName, x + 70f, rowY + 21f, boldTextPaint);

            subtitlePaint.setColor(0xFF8B949E);
            subtitlePaint.setTextSize(11f);
            canvas.drawText(type.subtitle, x + 70f, rowY + 35f, subtitlePaint);

            // Color Pill
            pillRect.set(x + w - 210f, rowY + 9f, x + w - 85f, rowY + 31f);
            cardPaint.setColor(0xFF21262D);
            canvas.drawRoundRect(pillRect, 11f, 11f, cardPaint);
            borderPaint.setColor(0xFF30363D);
            borderPaint.setStrokeWidth(1f);
            canvas.drawRoundRect(pillRect, 11f, 11f, borderPaint);

            colorPillPaint.setColor(resolvedColor);
            canvas.drawCircle(x + w - 196f, rowY + 20f, 5.5f, colorPillPaint);

            textPaint.setColor(0xFFE6EDF3);
            textPaint.setTextSize(11f);
            textPaint.setFakeBoldText(false);
            canvas.drawText(colorName, x + w - 184f, rowY + 24f, textPaint);

            // Star
            textPaint.setTextSize(18f);
            textPaint.setColor(isFav ? 0xFFFFD700 : 0xFF484F58);
            canvas.drawText(isFav ? "★" : "☆", x + w - 55f, rowY + 27f, textPaint);
        }
    }

    private void drawFooter(Canvas canvas, float x, float y, float w) {
        borderPaint.setColor(0xFF30363D);
        borderPaint.setStrokeWidth(1f);
        canvas.drawLine(x, y - 6f, x + w, y - 6f, borderPaint);

        textPaint.setColor(0xFF58A6FF);
        textPaint.setTextSize(11f);
        textPaint.setFakeBoldText(true);
        canvas.drawText("[ENTER]", x + 16f, y + 14f, textPaint);

        subtitlePaint.setColor(0xFF8B949E);
        subtitlePaint.setTextSize(11f);
        canvas.drawText("Toggle  ", x + 62f, y + 14f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[C2/DIAL]", x + 115f, y + 14f, textPaint);
        canvas.drawText("Kleur  ", x + 172f, y + 14f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[C1]", x + 218f, y + 14f, textPaint);
        canvas.drawText("Favoriet  ", x + 244f, y + 14f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[3]", x + 300f, y + 14f, textPaint);
        canvas.drawText("Dikte  ", x + 316f, y + 14f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[TRASH]", x + 360f, y + 14f, textPaint);
        canvas.drawText("Wissen  ", x + 412f, y + 14f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[MENU]", x + 465f, y + 14f, textPaint);
        canvas.drawText("Verberg", x + 512f, y + 14f, subtitlePaint);
    }

    // === 100% SONY A6300 HARDWARE KEY DISPATCH ===
    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleMenuVisible();
            return true;
        }

        if (!isVisible) {
            return false;
        }

        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!items.isEmpty()) {
                    selectedRowIndex = (selectedRowIndex - 1 + items.size()) % items.size();
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (!items.isEmpty()) {
                    selectedRowIndex = (selectedRowIndex + 1) % items.size();
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT: {
                OverlayType.Category[] categories = OverlayType.Category.values();
                int nextIdx = (currentCategory.ordinal() - 1 + categories.length) % categories.length;
                currentCategory = categories[nextIdx];
                selectedRowIndex = 0;
                scrollOffset = 0;
                invalidate();
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                OverlayType.Category[] categories = OverlayType.Category.values();
                int nextIdx = (currentCategory.ordinal() + 1) % categories.length;
                currentCategory = categories[nextIdx];
                selectedRowIndex = 0;
                scrollOffset = 0;
                invalidate();
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    stateManager.toggleActive(items.get(selectedRowIndex));
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_1: // C1 Custom button: Toggle Favorite
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    stateManager.toggleFavorite(items.get(selectedRowIndex));
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_2: // C2 Custom button: Cycle Color or Spiral Corner
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    OverlayType item = items.get(selectedRowIndex);
                    if (item == OverlayType.GRID_GOLDEN_SPIRAL) {
                        stateManager.cycleSpiralCorner();
                    } else {
                        stateManager.cycleColor(item);
                    }
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_3: // Cycle line stroke weight
                stateManager.cycleStrokeWeight();
                invalidate();
                return true;

            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_CLEAR: // Sony Trash key
                stateManager.clearAllActive();
                invalidate();
                return true;

            case KeyEvent.KEYCODE_BACK:
                setMenuVisible(false);
                return true;
        }

        return false;
    }
}
