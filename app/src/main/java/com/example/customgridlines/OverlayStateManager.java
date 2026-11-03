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
    private static final String KEY_ACTIVE_PREFIX = "active_";
    private static final String KEY_FAV_PREFIX = "fav_";
    private static final String KEY_COLOR_PREFIX = "color_";

    public interface OnStateChangeListener {
        void onStateChanged();
    }

    private final SharedPreferences prefs;
    private final Set<OverlayType> activeOverlays = EnumSet.noneOf(OverlayType.class);
    private final Set<OverlayType> favoriteOverlays = EnumSet.noneOf(OverlayType.class);
    private final Map<OverlayType, Integer> overlayColors = new EnumMap<OverlayType, Integer>(OverlayType.class);
    private final List<OnStateChangeListener> listeners = new ArrayList<OnStateChangeListener>();

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

            // Default initial setup if first launch:
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

            boolean isActive = prefs.getBoolean(KEY_ACTIVE_PREFIX + type.id, activeDefault);
            if (isActive) {
                activeOverlays.add(type);
            }

            boolean isFav = prefs.getBoolean(KEY_FAV_PREFIX + type.id, favDefault);
            if (isFav) {
                favoriteOverlays.add(type);
            }

            int colorIdx = prefs.getInt(KEY_COLOR_PREFIX + type.id, type.defaultColorIndex);
            overlayColors.put(type, colorIdx);
        }
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

    public synchronized void setFavorite(OverlayType type, boolean favorite) {
        if (favorite) {
            favoriteOverlays.add(type);
        } else {
            favoriteOverlays.remove(type);
        }
        prefs.edit().putBoolean(KEY_FAV_PREFIX + type.id, favorite).commit();
        notifyChanged();
    }

    public synchronized void toggleFavorite(OverlayType type) {
        setFavorite(type, !isFavorite(type));
    }

    public synchronized int getColorIndex(OverlayType type) {
        Integer idx = overlayColors.get(type);
        if (idx == null) {
            return type.defaultColorIndex;
        }
        return idx;
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

    public synchronized void setColorIndex(OverlayType type, int index) {
        overlayColors.put(type, index);
        prefs.edit().putInt(KEY_COLOR_PREFIX + type.id, index).commit();
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
