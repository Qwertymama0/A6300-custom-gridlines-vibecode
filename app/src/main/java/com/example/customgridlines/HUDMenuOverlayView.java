package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

public class HUDMenuOverlayView extends View implements OverlayStateManager.OnStateChangeListener {

    private OverlayStateManager stateManager;
    private boolean isVisible = true;

    private OverlayType.Category currentCategory = OverlayType.Category.FAVORITES;
    private int selectedRowIndex = 0;
    private int scrollOffset = 0;

    private Paint bgPaint;
    private Paint cardPaint;
    private Paint textPaint;
    private Paint boldTextPaint;
    private Paint subtitlePaint;
    private Paint borderPaint;
    private Paint highlightPaint;
    private Paint colorPillPaint;

    private RectF boundsRect;
    private RectF itemRect;
    private RectF toggleButtonRect;
    private RectF[] tabRects;

    public HUDMenuOverlayView(Context context, OverlayStateManager stateManager) {
        super(context);
        this.stateManager = stateManager;
        this.stateManager.addListener(this);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);

        bgPaint = new Paint();
        bgPaint.setColor(0xCC0D1117); // ~80% dark translucent scrim

        cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardPaint.setColor(0xEE161B22);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFF0F6FC);
        textPaint.setTextSize(18f);

        boldTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boldTextPaint.setColor(0xFFFFFFFF);
        boldTextPaint.setFakeBoldText(true);
        boldTextPaint.setTextSize(20f);

        subtitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtitlePaint.setColor(0xFF8B949E);
        subtitlePaint.setTextSize(13f);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0xFF30363D);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(0x33238636); // Subtle accent highlight

        colorPillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorPillPaint.setStyle(Paint.Style.FILL);

        boundsRect = new RectF();
        itemRect = new RectF();
        toggleButtonRect = new RectF();

        OverlayType.Category[] categories = OverlayType.Category.values();
        tabRects = new RectF[categories.length];
        for (int i = 0; i < tabRects.length; i++) {
            tabRects[i] = new RectF();
        }
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

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        // Always draw top-right quick toggle button
        drawQuickToggleButton(canvas, width);

        if (!isVisible) {
            return;
        }

        // Calculate responsive dialog dimensions centered on screen
        float panelW = Math.min(width * 0.90f, 720f);
        float panelH = Math.min(height * 0.88f, 440f);
        float panelX = (width - panelW) / 2f;
        float panelY = (height - panelH) / 2f;
        boundsRect.set(panelX, panelY, panelX + panelW, panelY + panelH);

        // Backdrop panel
        canvas.drawRoundRect(boundsRect, 14f, 14f, cardPaint);
        borderPaint.setColor(0xFF388BFD);
        borderPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(boundsRect, 14f, 14f, borderPaint);

        // Header Title
        boldTextPaint.setColor(0xFFFFFFFF);
        boldTextPaint.setTextSize(20f);
        canvas.drawText("SONY A6300 CUSTOM GRIDLINES", panelX + 20f, panelY + 32f, boldTextPaint);

        subtitlePaint.setColor(0xFF8B949E);
        subtitlePaint.setTextSize(13f);
        canvas.drawText("Android 2.3.7 / OpenMemories", panelX + panelW - 200f, panelY + 32f, subtitlePaint);

        // Divider
        borderPaint.setColor(0xFF30363D);
        borderPaint.setStrokeWidth(1.5f);
        canvas.drawLine(panelX, panelY + 46f, panelX + panelW, panelY + 46f, borderPaint);

        // Category Tabs
        drawCategoryTabs(canvas, panelX, panelY + 54f, panelW);

        // Item List
        drawItemList(canvas, panelX, panelY + 104f, panelW, panelH - 148f);

        // Footer Help Guide
        drawFooter(canvas, panelX, panelY + panelH - 34f, panelW);
    }

    private void drawQuickToggleButton(Canvas canvas, int width) {
        float btnW = 110f;
        float btnH = 36f;
        float btnX = width - btnW - 16f;
        float btnY = 16f;
        toggleButtonRect.set(btnX, btnY, btnX + btnW, btnY + btnH);

        cardPaint.setColor(isVisible ? 0xDD21262D : 0xEE161B22);
        canvas.drawRoundRect(toggleButtonRect, 8f, 8f, cardPaint);

        borderPaint.setColor(isVisible ? 0xFF8B949E : 0xFF388BFD);
        borderPaint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(toggleButtonRect, 8f, 8f, borderPaint);

        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(14f);
        String label = isVisible ? "✕ SLUITEN" : "☰ GUIDES";
        float tw = textPaint.measureText(label);
        canvas.drawText(label, btnX + (btnW - tw) / 2f, btnY + 23f, textPaint);
    }

    private void drawCategoryTabs(Canvas canvas, float x, float y, float w) {
        OverlayType.Category[] categories = OverlayType.Category.values();
        float tabW = (w - 24f) / categories.length;
        float tabH = 34f;

        for (int i = 0; i < categories.length; i++) {
            OverlayType.Category cat = categories[i];
            float tx = x + 12f + (i * tabW);
            tabRects[i].set(tx, y, tx + tabW - 4f, y + tabH);

            boolean isCurrent = (cat == currentCategory);
            cardPaint.setColor(isCurrent ? 0xFF1F6FEB : 0xFF21262D);
            canvas.drawRoundRect(tabRects[i], 6f, 6f, cardPaint);

            textPaint.setColor(isCurrent ? 0xFFFFFFFF : 0xFFC9D1D9);
            textPaint.setTextSize(13f);
            textPaint.setFakeBoldText(isCurrent);

            String title = cat.title;
            float tw = textPaint.measureText(title);
            canvas.drawText(title, tx + (tabW - 4f - tw) / 2f, y + 22f, textPaint);
        }
    }

    private void drawItemList(Canvas canvas, float x, float y, float w, float h) {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);

        if (items.isEmpty()) {
            textPaint.setColor(0xFF8B949E);
            textPaint.setTextSize(15f);
            String emptyMsg = currentCategory == OverlayType.Category.FAVORITES
                ? "Geen favorieten gemarkeerd. Druk op [C1] of klik op [☆] om rasters toe te voegen."
                : "Geen items in deze categorie.";
            canvas.drawText(emptyMsg, x + 30f, y + 50f, textPaint);
            return;
        }

        if (selectedRowIndex >= items.size()) {
            selectedRowIndex = Math.max(0, items.size() - 1);
        }

        float rowH = 46f;
        int visibleRows = (int) (h / rowH);

        // Adjust scroll offset to keep selected row in view
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
            int colorIdx = stateManager.getColorIndex(type);
            int resolvedColor = stateManager.getResolvedColor(type);
            String colorName = ColorPalette.getColorName(colorIdx);

            // Row background
            cardPaint.setColor(isFocused ? 0xFF21262D : 0xFF161B22);
            canvas.drawRoundRect(itemRect, 6f, 6f, cardPaint);

            if (isFocused) {
                borderPaint.setColor(0xFF388BFD);
                borderPaint.setStrokeWidth(1.5f);
                canvas.drawRoundRect(itemRect, 6f, 6f, borderPaint);
            }

            // 1. Focus Cursor Indicator
            if (isFocused) {
                textPaint.setColor(0xFF58A6FF);
                textPaint.setTextSize(16f);
                canvas.drawText("▸", x + 18f, rowY + 27f, textPaint);
            }

            // 2. Simple Checkbox: [✓] or [ ]
            drawCheckbox(canvas, x + 36f, rowY + 11f, isActive, resolvedColor);

            // 3. Title & Subtitle
            boldTextPaint.setColor(isActive ? 0xFFFFFFFF : 0xFFC9D1D9);
            boldTextPaint.setTextSize(15f);
            canvas.drawText(type.displayName, x + 72f, rowY + 22f, boldTextPaint);

            subtitlePaint.setColor(0xFF8B949E);
            subtitlePaint.setTextSize(11f);
            canvas.drawText(type.subtitle, x + 72f, rowY + 36f, subtitlePaint);

            // 4. Color Selection Pill [● ColorName]
            drawColorPill(canvas, x + w - 210f, rowY + 9f, resolvedColor, colorName);

            // 5. Favorite Star [★] / [☆]
            drawFavoriteStar(canvas, x + w - 60f, rowY + 11f, isFav);
        }
    }

    private void drawCheckbox(Canvas canvas, float cx, float cy, boolean checked, int checkColor) {
        RectF box = new RectF(cx, cy, cx + 22f, cy + 22f);
        cardPaint.setColor(checked ? 0xFF1F6FEB : 0xFF0D1117);
        canvas.drawRoundRect(box, 4f, 4f, cardPaint);

        borderPaint.setColor(checked ? 0xFF58A6FF : 0xFF484F58);
        borderPaint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(box, 4f, 4f, borderPaint);

        if (checked) {
            textPaint.setColor(0xFFFFFFFF);
            textPaint.setTextSize(14f);
            textPaint.setFakeBoldText(true);
            canvas.drawText("✓", cx + 5f, cy + 17f, textPaint);
        }
    }

    private void drawColorPill(Canvas canvas, float x, float y, int color, String name) {
        float pillW = 125f;
        float pillH = 24f;
        RectF pillRect = new RectF(x, y, x + pillW, y + pillH);

        cardPaint.setColor(0xFF21262D);
        canvas.drawRoundRect(pillRect, 12f, 12f, cardPaint);
        borderPaint.setColor(0xFF30363D);
        borderPaint.setStrokeWidth(1f);
        canvas.drawRoundRect(pillRect, 12f, 12f, borderPaint);

        // Color dot
        colorPillPaint.setColor(color);
        canvas.drawCircle(x + 14f, y + 12f, 6f, colorPillPaint);

        // Name
        textPaint.setColor(0xFFE6EDF3);
        textPaint.setTextSize(12f);
        textPaint.setFakeBoldText(false);
        canvas.drawText(name, x + 26f, y + 16f, textPaint);
    }

    private void drawFavoriteStar(Canvas canvas, float x, float y, boolean favorited) {
        textPaint.setTextSize(19f);
        textPaint.setColor(favorited ? 0xFFFFD700 : 0xFF484F58);
        canvas.drawText(favorited ? "★" : "☆", x, y + 17f, textPaint);
    }

    private void drawFooter(Canvas canvas, float x, float y, float w) {
        borderPaint.setColor(0xFF30363D);
        borderPaint.setStrokeWidth(1f);
        canvas.drawLine(x, y - 6f, x + w, y - 6f, borderPaint);

        textPaint.setColor(0xFF58A6FF);
        textPaint.setTextSize(12f);
        textPaint.setFakeBoldText(true);
        canvas.drawText("[ENTER]", x + 16f, y + 16f, textPaint);

        subtitlePaint.setColor(0xFF8B949E);
        subtitlePaint.setTextSize(12f);
        canvas.drawText("Toggle  ", x + 66f, y + 16f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[DIAL/RIGHT]", x + 125f, y + 16f, textPaint);
        canvas.drawText("Kleur  ", x + 208f, y + 16f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[C1]", x + 260f, y + 16f, textPaint);
        canvas.drawText("Favoriet  ", x + 290f, y + 16f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[TRASH]", x + 360f, y + 16f, textPaint);
        canvas.drawText("Wissen  ", x + 418f, y + 16f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[MENU]", x + 480f, y + 16f, textPaint);
        canvas.drawText("Verberg", x + 534f, y + 16f, subtitlePaint);
    }

    // === HARDWARE KEY DISPATCH (SONY A6300 DIAL & BUTTONS) ===
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
                // Switch to previous category
                OverlayType.Category[] categories = OverlayType.Category.values();
                int catIdx = currentCategory.ordinal();
                int nextIdx = (catIdx - 1 + categories.length) % categories.length;
                currentCategory = categories[nextIdx];
                selectedRowIndex = 0;
                scrollOffset = 0;
                invalidate();
                return true;
            }

            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                // If on an item, cycle color; or switch category
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    stateManager.cycleColor(items.get(selectedRowIndex));
                } else {
                    OverlayType.Category[] categories = OverlayType.Category.values();
                    int catIdx = currentCategory.ordinal();
                    int nextIdx = (catIdx + 1) % categories.length;
                    currentCategory = categories[nextIdx];
                    selectedRowIndex = 0;
                    scrollOffset = 0;
                }
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

            case KeyEvent.KEYCODE_1: // C1 Custom button
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    stateManager.toggleFavorite(items.get(selectedRowIndex));
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_2: // C2 Custom button
                if (!items.isEmpty() && selectedRowIndex < items.size()) {
                    stateManager.cycleColor(items.get(selectedRowIndex));
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_CLEAR:
                stateManager.clearAllActive();
                invalidate();
                return true;

            case KeyEvent.KEYCODE_BACK:
                setMenuVisible(false);
                return true;
        }

        return false;
    }

    // === TOUCH / MOUSE DISPATCH (STREAMING EMULATOR SUPPORT) ===
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }

        float tx = event.getX();
        float ty = event.getY();

        // 1. Check quick toggle button
        if (toggleButtonRect.contains(tx, ty)) {
            toggleMenuVisible();
            return true;
        }

        if (!isVisible) {
            return false;
        }

        // 2. Check Category Tabs
        OverlayType.Category[] categories = OverlayType.Category.values();
        for (int i = 0; i < tabRects.length; i++) {
            if (tabRects[i] != null && tabRects[i].contains(tx, ty)) {
                currentCategory = categories[i];
                selectedRowIndex = 0;
                scrollOffset = 0;
                invalidate();
                return true;
            }
        }

        // 3. Check Items in List
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);
        int width = getWidth();
        int height = getHeight();
        float panelW = Math.min(width * 0.90f, 720f);
        float panelH = Math.min(height * 0.88f, 440f);
        float panelX = (width - panelW) / 2f;
        float panelY = (height - panelH) / 2f;
        float listY = panelY + 104f;
        float listH = panelH - 148f;
        float rowH = 46f;
        int visibleRows = (int) (listH / rowH);

        for (int i = scrollOffset; i < Math.min(items.size(), scrollOffset + visibleRows); i++) {
            float rowTop = listY + ((i - scrollOffset) * rowH);
            float rowBottom = rowTop + rowH;

            if (ty >= rowTop && ty <= rowBottom && tx >= panelX && tx <= panelX + panelW) {
                selectedRowIndex = i;
                OverlayType type = items.get(i);

                // Check click targets:
                // Star target (right edge)
                if (tx >= panelX + panelW - 70f) {
                    stateManager.toggleFavorite(type);
                }
                // Color pill target
                else if (tx >= panelX + panelW - 220f && tx <= panelX + panelW - 80f) {
                    stateManager.cycleColor(type);
                }
                // Checkbox / main row target
                else {
                    stateManager.toggleActive(type);
                }
                invalidate();
                return true;
            }
        }

        return true;
    }
}
