package com.example.customgridlines;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OverlayStateManager {

    private static final String PREFS_NAME = "custom_gridlines_prefs";
    private static final String KEY_ACTIVE_PREFIX = "a_";
    private static final String KEY_FAV_PREFIX = "f_";
    private static final String KEY_COLOR_PREFIX = "c_";
    private static final String KEY_STROKE_WEIGHT = "sw";
    private static final String KEY_SPIRAL_CORNER = "sc";

    public interface OnStateChangeListener {
        void onStateChanged();
    }

    public static final float[] STROKE_WEIGHTS = new float[] { 1.5f, 2.5f, 4.0f };
    public static final String[] STROKE_WEIGHT_NAMES = new String[] { "Fine (1.5px)", "Standard (2.5px)", "Bold (4.0px)" };
    public static final String[] SPIRAL_CORNER_NAMES = new String[] { "Top-Left", "Top-Right", "Bottom-Right", "Bottom-Left" };

    private final SharedPreferences prefs;
    private final Set<OverlayType> activeOverlays = EnumSet.noneOf(OverlayType.class);
    private final Set<OverlayType> favoriteOverlays = EnumSet.noneOf(OverlayType.class);
    private final Map<OverlayType, Integer> overlayColors = new EnumMap<OverlayType, Integer>(OverlayType.class);
    private final List<OnStateChangeListener> listeners = new ArrayList<OnStateChangeListener>();

    private int strokeWeightIndex = 1;
    private int spiralCornerIndex = 0;

    public OverlayStateManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadPreferences();
    }

    private void loadPreferences() {
        OverlayType[] all = OverlayType.values();
        boolean hasSavedState = prefs.contains(KEY_ACTIVE_PREFIX + OverlayType.GRID_RULE_OF_THIRDS.id);

        for (OverlayType type : all) {
            boolean activeDefault = false;
            boolean favDefault = false;

            if (!hasSavedState) {
                if (type == OverlayType.GRID_RULE_OF_THIRDS || type == OverlayType.RATIO_16_9) {
                    activeDefault = true;
                }
                if (type == OverlayType.GRID_RULE_OF_THIRDS ||
                    type == OverlayType.GRID_GOLDEN_RATIO ||
                    type == OverlayType.GRID_GOLDEN_SPIRAL ||
                    type == OverlayType.RATIO_16_9 ||
                    type == OverlayType.RATIO_1_1 ||
                    type == OverlayType.RATIO_2_35) {
                    favDefault = true;
                }
            }

            if (prefs.getBoolean(KEY_ACTIVE_PREFIX + type.id, activeDefault)) {
                activeOverlays.add(type);
            }
            if (prefs.getBoolean(KEY_FAV_PREFIX + type.id, favDefault)) {
                favoriteOverlays.add(type);
            }

            int colorIdx = prefs.getInt(KEY_COLOR_PREFIX + type.id, type.defaultColorIndex);
            overlayColors.put(type, colorIdx);
        }

        strokeWeightIndex = prefs.getInt(KEY_STROKE_WEIGHT, 1);
        spiralCornerIndex = prefs.getInt(KEY_SPIRAL_CORNER, 0);
    }

    public synchronized boolean isActive(OverlayType type) {
        return activeOverlays.contains(type);
    }

    public synchronized void setActive(OverlayType type, boolean active) {
        if (active) {
            activeOverlays.add(type);
        } else {
            activeOverlays.remove(type);
        }
        prefs.edit().putBoolean(KEY_ACTIVE_PREFIX + type.id, active).commit();
        notifyChanged();
    }

    public synchronized void toggleActive(OverlayType type) {
        setActive(type, !isActive(type));
    }

    public synchronized boolean isFavorite(OverlayType type) {
        return favoriteOverlays.contains(type);
    }

    public synchronized void toggleFavorite(OverlayType type) {
        boolean fav = !isFavorite(type);
        if (fav) {
            favoriteOverlays.add(type);
        } else {
            favoriteOverlays.remove(type);
        }
        prefs.edit().putBoolean(KEY_FAV_PREFIX + type.id, fav).commit();
        notifyChanged();
    }

    public synchronized int getColorIndex(OverlayType type) {
        Integer idx = overlayColors.get(type);
        return idx == null ? type.defaultColorIndex : idx;
    }

    public synchronized int getResolvedColor(OverlayType type) {
        return ColorPalette.getColor(getColorIndex(type));
    }

    public synchronized void cycleColor(OverlayType type) {
        int nextIdx = ColorPalette.getNextColorIndex(getColorIndex(type));
        overlayColors.put(type, nextIdx);
        prefs.edit().putInt(KEY_COLOR_PREFIX + type.id, nextIdx).commit();
        notifyChanged();
    }

    public synchronized void cycleColorReverse(OverlayType type) {
        int prevIdx = ColorPalette.getPrevColorIndex(getColorIndex(type));
        overlayColors.put(type, prevIdx);
        prefs.edit().putInt(KEY_COLOR_PREFIX + type.id, prevIdx).commit();
        notifyChanged();
    }

    public synchronized void clearAllActive() {
        activeOverlays.clear();
        SharedPreferences.Editor editor = prefs.edit();
        for (OverlayType type : OverlayType.values()) {
            editor.putBoolean(KEY_ACTIVE_PREFIX + type.id, false);
        }
        editor.commit();
        notifyChanged();
    }

    public synchronized Set<OverlayType> getActiveOverlays() {
        return EnumSet.copyOf(activeOverlays);
    }

    public synchronized List<OverlayType> getItemsForCategory(OverlayType.Category category) {
        List<OverlayType> result = new ArrayList<OverlayType>();
        for (OverlayType type : OverlayType.values()) {
            if (category == OverlayType.Category.FAVORITES) {
                if (isFavorite(type)) {
                    result.add(type);
                }
            } else if (type.category == category) {
                result.add(type);
            }
        }
        return result;
    }

    public synchronized float getStrokeWeightPx() {
        if (strokeWeightIndex < 0 || strokeWeightIndex >= STROKE_WEIGHTS.length) {
            return STROKE_WEIGHTS[1];
        }
        return STROKE_WEIGHTS[strokeWeightIndex];
    }

    public synchronized String getStrokeWeightName() {
        return STROKE_WEIGHT_NAMES[strokeWeightIndex];
    }

    public synchronized void cycleStrokeWeight() {
        strokeWeightIndex = (strokeWeightIndex + 1) % STROKE_WEIGHTS.length;
        prefs.edit().putInt(KEY_STROKE_WEIGHT, strokeWeightIndex).commit();
        notifyChanged();
    }

    public synchronized int getSpiralCornerIndex() {
        return spiralCornerIndex;
    }

    public synchronized String getSpiralCornerName() {
        return SPIRAL_CORNER_NAMES[spiralCornerIndex];
    }

    public synchronized void cycleSpiralCorner() {
        spiralCornerIndex = (spiralCornerIndex + 1) % SPIRAL_CORNER_NAMES.length;
        prefs.edit().putInt(KEY_SPIRAL_CORNER, spiralCornerIndex).commit();
        notifyChanged();
    }

    public void addListener(OnStateChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnStateChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        for (OnStateChangeListener listener : listeners) {
            listener.onStateChanged();
        }
    }
}
