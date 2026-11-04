package com.example.customgridlines;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.View;

import java.util.List;

public class HUDMenuOverlayView extends View implements OverlayStateManager.OnStateChangeListener {

    public enum FocusArea {
        TABS,
        ITEMS
    }

    public static final int COL_CHECKBOX = 0;
    public static final int COL_COLOR = 1;
    public static final int COL_STAR = 2;

    private final OverlayStateManager stateManager;
    private boolean isVisible = true;

    private FocusArea focusArea = FocusArea.ITEMS;
    private int focusColumn = COL_CHECKBOX;
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
    private final RectF starBox = new RectF();

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

        float panelW = Math.min(width * 0.94f, 750f);
        float panelH = Math.min(height * 0.92f, 455f);
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
        boldTextPaint.setTextSize(17f);
        canvas.drawText("GRIDLAB | SONY A6300", panelX + 20f, panelY + 26f, boldTextPaint);

        // Quick Settings Info
        subtitlePaint.setColor(0xFF58A6FF);
        subtitlePaint.setTextSize(12f);
        String info = "Dikte: " + stateManager.getStrokeWeightName() + " | Spiraal: " + stateManager.getSpiralCornerName();
        canvas.drawText(info, panelX + panelW - 270f, panelY + 26f, subtitlePaint);

        // Category Tabs
        drawCategoryTabs(canvas, panelX, panelY + 38f, panelW);

        // Item List
        drawItemList(canvas, panelX, panelY + 76f, panelW, panelH - 128f);

        // Footer Navigation Guide
        drawFooter(canvas, panelX, panelY + panelH - 38f, panelW);
    }

    private void drawCategoryTabs(Canvas canvas, float x, float y, float w) {
        OverlayType.Category[] categories = OverlayType.Category.values();
        float tabW = (w - 24f) / categories.length;
        float tabH = 30f;

        for (int i = 0; i < categories.length; i++) {
            OverlayType.Category cat = categories[i];
            float tx = x + 12f + (i * tabW);
            tabRect.set(tx, y, tx + tabW - 4f, y + tabH);

            boolean isCurrent = (cat == currentCategory);
            boolean isTabFocused = (focusArea == FocusArea.TABS && isCurrent);

            cardPaint.setColor(isCurrent ? 0xFF1F6FEB : 0xFF21262D);
            canvas.drawRoundRect(tabRect, 6f, 6f, cardPaint);

            if (isTabFocused) {
                borderPaint.setColor(0xFFFFD700); // Bright Gold focus outline
                borderPaint.setStrokeWidth(2.5f);
                canvas.drawRoundRect(tabRect, 6f, 6f, borderPaint);
            } else if (isCurrent) {
                borderPaint.setColor(0xFF58A6FF);
                borderPaint.setStrokeWidth(1.2f);
                canvas.drawRoundRect(tabRect, 6f, 6f, borderPaint);
            }

            textPaint.setColor(isCurrent ? 0xFFFFFFFF : 0xFFC9D1D9);
            textPaint.setTextSize(12f);
            textPaint.setFakeBoldText(isCurrent);

            String title = isTabFocused ? ("◀ " + cat.title + " ▶") : cat.title;
            float tw = textPaint.measureText(title);
            canvas.drawText(title, tx + (tabW - 4f - tw) / 2f, y + 20f, textPaint);
        }
    }

    private void drawItemList(Canvas canvas, float x, float y, float w, float h) {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);

        if (items.isEmpty()) {
            if (focusArea == FocusArea.ITEMS) {
                focusArea = FocusArea.TABS;
            }
            textPaint.setColor(0xFFC9D1D9);
            textPaint.setTextSize(14f);
            String emptyMsg = (currentCategory == OverlayType.Category.FAVORITES)
                ? "Geen favorieten opgeslagen. Druk op [C1] of navigeer naar [★] om rasters toe te voegen!"
                : "Geen items in deze categorie.";
            canvas.drawText(emptyMsg, x + 30f, y + 40f, textPaint);

            subtitlePaint.setColor(0xFF58A6FF);
            subtitlePaint.setTextSize(12f);
            canvas.drawText("Gebruik [◀ / ▶] of het bovenste instelwiel om een andere categorie te openen.", x + 30f, y + 66f, subtitlePaint);
            return;
        }

        if (selectedRowIndex >= items.size()) {
            selectedRowIndex = Math.max(0, items.size() - 1);
        }

        float rowH = 43f;
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

            boolean isRowFocused = (focusArea == FocusArea.ITEMS && i == selectedRowIndex);
            boolean isActive = stateManager.isActive(type);
            boolean isFav = stateManager.isFavorite(type);
            int resolvedColor = stateManager.getResolvedColor(type);
            String colorName = ColorPalette.getColorName(stateManager.getColorIndex(type));

            cardPaint.setColor(isRowFocused ? 0xFF21262D : 0xFF161B22);
            canvas.drawRoundRect(itemRect, 6f, 6f, cardPaint);

            if (isRowFocused) {
                borderPaint.setColor(0xFF388BFD);
                borderPaint.setStrokeWidth(1.2f);
                canvas.drawRoundRect(itemRect, 6f, 6f, borderPaint);
            }

            // --- COLUMN 0: Checkbox [✓] or [ ] & Titles ---
            boxRect.set(x + 36f, rowY + 9f, x + 58f, rowY + 31f);
            cardPaint.setColor(isActive ? 0xFF1F6FEB : 0xFF0D1117);
            canvas.drawRoundRect(boxRect, 4f, 4f, cardPaint);

            if (isRowFocused && focusColumn == COL_CHECKBOX) {
                // Bright Gold indicator and focus outline
                borderPaint.setColor(0xFFFFD700);
                borderPaint.setStrokeWidth(2.5f);
                canvas.drawRoundRect(boxRect, 4f, 4f, borderPaint);

                textPaint.setColor(0xFFFFD700);
                textPaint.setTextSize(15f);
                canvas.drawText("▸", x + 18f, rowY + 25f, textPaint);
            } else {
                borderPaint.setColor(isActive ? 0xFF58A6FF : 0xFF484F58);
                borderPaint.setStrokeWidth(1.5f);
                canvas.drawRoundRect(boxRect, 4f, 4f, borderPaint);
            }

            if (isActive) {
                textPaint.setColor(0xFFFFFFFF);
                textPaint.setTextSize(14f);
                textPaint.setFakeBoldText(true);
                canvas.drawText("✓", x + 41f, rowY + 25f, textPaint);
            }

            boldTextPaint.setColor(isActive ? 0xFFFFFFFF : 0xFFC9D1D9);
            boldTextPaint.setTextSize(14f);
            canvas.drawText(type.displayName, x + 70f, rowY + 20f, boldTextPaint);

            subtitlePaint.setColor(0xFF8B949E);
            subtitlePaint.setTextSize(11f);
            canvas.drawText(type.subtitle, x + 70f, rowY + 34f, subtitlePaint);

            // --- COLUMN 1: Color Pill [ ● Color ] ---
            pillRect.set(x + w - 215f, rowY + 9f, x + w - 85f, rowY + 31f);
            boolean isColorColFocused = (isRowFocused && focusColumn == COL_COLOR);
            cardPaint.setColor(isColorColFocused ? 0xFF2D333B : 0xFF21262D);
            canvas.drawRoundRect(pillRect, 11f, 11f, cardPaint);

            if (isColorColFocused) {
                borderPaint.setColor(0xFFFFD700); // Bright Gold focus ring
                borderPaint.setStrokeWidth(2.5f);
                canvas.drawRoundRect(pillRect, 11f, 11f, borderPaint);

                textPaint.setColor(0xFFFFD700);
                textPaint.setTextSize(13f);
                canvas.drawText("◀", x + w - 228f, rowY + 24f, textPaint);
                canvas.drawText("▶", x + w - 81f, rowY + 24f, textPaint);
            } else {
                borderPaint.setColor(0xFF30363D);
                borderPaint.setStrokeWidth(1f);
                canvas.drawRoundRect(pillRect, 11f, 11f, borderPaint);
            }

            colorPillPaint.setColor(resolvedColor);
            canvas.drawCircle(x + w - 200f, rowY + 20f, 5.5f, colorPillPaint);

            textPaint.setColor(isColorColFocused ? 0xFFFFFFFF : 0xFFE6EDF3);
            textPaint.setTextSize(11f);
            textPaint.setFakeBoldText(isColorColFocused);
            canvas.drawText(colorName, x + w - 188f, rowY + 24f, textPaint);

            // --- COLUMN 2: Star [★] or [☆] ---
            starBox.set(x + w - 65f, rowY + 8f, x + w - 30f, rowY + 32f);
            boolean isStarColFocused = (isRowFocused && focusColumn == COL_STAR);

            if (isStarColFocused) {
                cardPaint.setColor(0xFF2D333B);
                canvas.drawRoundRect(starBox, 6f, 6f, cardPaint);
                borderPaint.setColor(0xFFFFD700); // Bright Gold focus ring
                borderPaint.setStrokeWidth(2.5f);
                canvas.drawRoundRect(starBox, 6f, 6f, borderPaint);
            }

            textPaint.setTextSize(18f);
            textPaint.setColor(isFav ? 0xFFFFD700 : (isStarColFocused ? 0xFFC9D1D9 : 0xFF484F58));
            canvas.drawText(isFav ? "★" : "☆", x + w - 53f, rowY + 26f, textPaint);
        }
    }

    private void drawFooter(Canvas canvas, float x, float y, float w) {
        borderPaint.setColor(0xFF30363D);
        borderPaint.setStrokeWidth(1f);
        canvas.drawLine(x, y - 4f, x + w, y - 4f, borderPaint);

        // Line 1: D-Pad & Control navigation
        textPaint.setTextSize(12f);
        textPaint.setColor(0xFFFFD700);
        canvas.drawText("[◀/▶]", x + 12f, y + 13f, textPaint);
        subtitlePaint.setColor(0xFFE6EDF3);
        canvas.drawText("Veld/Tab", x + 44f, y + 13f, subtitlePaint);

        textPaint.setColor(0xFFFFD700);
        canvas.drawText("[ENTER]", x + 104f, y + 13f, textPaint);
        subtitlePaint.setColor(0xFFE6EDF3);
        canvas.drawText("Wijzig/Toggle", x + 152f, y + 13f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[▲/▼]", x + 242f, y + 13f, textPaint);
        subtitlePaint.setColor(0xFF8B949E);
        canvas.drawText("Rij/Tabs", x + 272f, y + 13f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[Wiel]", x + 330f, y + 13f, textPaint);
        subtitlePaint.setColor(0xFF8B949E);
        canvas.drawText("Scroll/Kleur", x + 368f, y + 13f, subtitlePaint);

        // Line 2: Camera shortcut buttons
        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[C1]", x + 12f, y + 29f, textPaint);
        subtitlePaint.setColor(0xFF8B949E);
        canvas.drawText("Favoriet", x + 36f, y + 29f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[C2]", x + 96f, y + 29f, textPaint);
        subtitlePaint.setColor(0xFF8B949E);
        canvas.drawText("Kleur", x + 122f, y + 29f, subtitlePaint);

        textPaint.setColor(0xFF58A6FF);
        canvas.drawText("[MENU]", x + 168f, y + 29f, textPaint);
        subtitlePaint.setColor(0xFF8B949E);
        canvas.drawText("Verberg", x + 212f, y + 29f, subtitlePaint);

        textPaint.setColor(0xFFF85149);
        canvas.drawText("[BACK]", x + 272f, y + 29f, textPaint);
        subtitlePaint.setColor(0xFFE6EDF3);
        canvas.drawText("Sluit Menu / Afsluiten", x + 316f, y + 29f, subtitlePaint);
    }

    private void switchCategory(int direction) {
        OverlayType.Category[] categories = OverlayType.Category.values();
        int nextIdx = (currentCategory.ordinal() + direction + categories.length) % categories.length;
        currentCategory = categories[nextIdx];
        selectedRowIndex = 0;
        scrollOffset = 0;
        focusColumn = COL_CHECKBOX;
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);
        if (items.isEmpty()) {
            focusArea = FocusArea.TABS;
        }
        invalidate();
    }

    private void toggleCurrentFavorite() {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);
        if (!items.isEmpty() && selectedRowIndex < items.size()) {
            OverlayType item = items.get(selectedRowIndex);
            stateManager.toggleFavorite(item);
            List<OverlayType> updated = stateManager.getItemsForCategory(currentCategory);
            if (selectedRowIndex >= updated.size()) {
                selectedRowIndex = Math.max(0, updated.size() - 1);
            }
            if (updated.isEmpty()) {
                focusArea = FocusArea.TABS;
            }
            invalidate();
        }
    }

    private void cycleCurrentColor(boolean forward) {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);
        if (!items.isEmpty() && selectedRowIndex < items.size()) {
            OverlayType item = items.get(selectedRowIndex);
            if (item == OverlayType.GRID_GOLDEN_SPIRAL) {
                stateManager.cycleSpiralCorner();
            } else {
                if (forward) {
                    stateManager.cycleColor(item);
                } else {
                    stateManager.cycleColorReverse(item);
                }
            }
            invalidate();
        }
    }

    private void moveRow(int delta) {
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);
        if (!items.isEmpty()) {
            selectedRowIndex = Math.max(0, Math.min(items.size() - 1, selectedRowIndex + delta));
            invalidate();
        }
    }

    // === SONY A6300 HARDWARE KEY & ROTARY DIAL DISPATCH ===
    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        int scanCode = (event != null) ? event.getScanCode() : 0;

        // 1. Menu Toggle (Sony MENU key is scanCode 514 or KEYCODE_MENU)
        if (keyCode == KeyEvent.KEYCODE_MENU || scanCode == 514) {
            toggleMenuVisible();
            return true;
        }

        if (!isVisible) {
            return false;
        }

        // 2. Sony Top Dial rotation (scanCode 525 CW / 526 CCW): Switches category tabs
        if (scanCode == 525) {
            switchCategory(1);
            return true;
        }
        if (scanCode == 526) {
            switchCategory(-1);
            return true;
        }

        // 3. Sony Control Wheel rotary turn (scanCode 522 CW / 523 CCW):
        if (scanCode == 522) { // Clockwise
            if (focusArea == FocusArea.TABS) {
                switchCategory(1);
            } else {
                if (focusColumn == COL_COLOR) {
                    cycleCurrentColor(true);
                } else {
                    moveRow(1);
                }
            }
            return true;
        }
        if (scanCode == 523) { // Counter-Clockwise
            if (focusArea == FocusArea.TABS) {
                switchCategory(-1);
            } else {
                if (focusColumn == COL_COLOR) {
                    cycleCurrentColor(false);
                } else {
                    moveRow(-1);
                }
            }
            return true;
        }

        // 4. Sony Hardware Button Shortcuts:
        // C1 button (scanCode 524 or KEYCODE_1): Toggle Favorite
        if (scanCode == 524 || keyCode == KeyEvent.KEYCODE_1) {
            toggleCurrentFavorite();
            return true;
        }

        // C2 / Trash button (scanCode 595, 513, KEYCODE_CLEAR, KEYCODE_DEL, KEYCODE_2): Cycle Color
        if (scanCode == 595 || scanCode == 513 || keyCode == KeyEvent.KEYCODE_CLEAR || keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_2) {
            cycleCurrentColor(true);
            return true;
        }

        // Fn button (scanCode 520 or KEYCODE_3): Cycle Stroke Weight
        if (scanCode == 520 || keyCode == KeyEvent.KEYCODE_3) {
            stateManager.cycleStrokeWeight();
            invalidate();
            return true;
        }

        // 5. Standard Sony D-Pad Navigation:
        List<OverlayType> items = stateManager.getItemsForCategory(currentCategory);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (focusArea == FocusArea.ITEMS) {
                    if (selectedRowIndex > 0) {
                        selectedRowIndex--;
                    } else {
                        // Move UP past row 0: focus Category Tabs!
                        focusArea = FocusArea.TABS;
                    }
                    invalidate();
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (focusArea == FocusArea.TABS) {
                    if (!items.isEmpty()) {
                        focusArea = FocusArea.ITEMS;
                        selectedRowIndex = 0;
                        invalidate();
                    }
                } else {
                    if (selectedRowIndex < items.size() - 1) {
                        selectedRowIndex++;
                        invalidate();
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (focusArea == FocusArea.TABS) {
                    switchCategory(-1);
                } else {
                    if (focusColumn > 0) {
                        // Move between columns: Star (2) -> Color (1) -> Checkbox (0)
                        focusColumn--;
                        invalidate();
                    } else {
                        // From Checkbox column, move to previous category tab
                        switchCategory(-1);
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (focusArea == FocusArea.TABS) {
                    switchCategory(1);
                } else {
                    if (focusColumn < 2) {
                        // Move between columns: Checkbox (0) -> Color (1) -> Star (2)
                        focusColumn++;
                        invalidate();
                    } else {
                        // From Star column, move to next category tab
                        switchCategory(1);
                    }
                }
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (focusArea == FocusArea.TABS) {
                    if (!items.isEmpty()) {
                        focusArea = FocusArea.ITEMS;
                        selectedRowIndex = 0;
                        focusColumn = COL_CHECKBOX;
                        invalidate();
                    }
                } else {
                    if (!items.isEmpty() && selectedRowIndex < items.size()) {
                        OverlayType item = items.get(selectedRowIndex);
                        if (focusColumn == COL_CHECKBOX) {
                            stateManager.toggleActive(item);
                        } else if (focusColumn == COL_COLOR) {
                            cycleCurrentColor(true);
                        } else if (focusColumn == COL_STAR) {
                            toggleCurrentFavorite();
                        }
                        invalidate();
                    }
                }
                return true;
        }

        return false;
    }
}
