package com.example.customgridlines;

import android.graphics.Color;

public final class ColorPalette {

    public static final class PaletteItem {
        public final int color;
        public final String name;

        public PaletteItem(int color, String name) {
            this.color = color;
            this.name = name;
        }
    }

    public static final PaletteItem[] PALETTE = new PaletteItem[] {
        new PaletteItem(0xFFFFFFFF, "White"),
        new PaletteItem(0xFFFFEB3B, "Yellow"),
        new PaletteItem(0xFFF44336, "Red"),
        new PaletteItem(0xFF4CAF50, "Green"),
        new PaletteItem(0xFF00E5FF, "Cyan"),
        new PaletteItem(0xFFFF9800, "Orange"),
        new PaletteItem(0xFFE91E63, "Magenta"),
        new PaletteItem(0xFFAEEA00, "Lime"),
        new PaletteItem(0xFFFFD700, "Gold"),
        new PaletteItem(0xFF9C27B0, "Violet"),
        new PaletteItem(0xFF2196F3, "Sky Blue"),
        new PaletteItem(0xFFB0BEC5, "Silver"),
        new PaletteItem(0xFF000000, "Black")
    };

    public static int getColorCount() {
        return PALETTE.length;
    }

    public static int getColor(int index) {
        if (index < 0 || index >= PALETTE.length) {
            return PALETTE[0].color;
        }
        return PALETTE[index].color;
    }

    public static String getColorName(int index) {
        if (index < 0 || index >= PALETTE.length) {
            return PALETTE[0].name;
        }
        return PALETTE[index].name;
    }

    public static int getNextColorIndex(int currentIndex) {
        return (currentIndex + 1) % PALETTE.length;
    }

    public static int getPrevColorIndex(int currentIndex) {
        return (currentIndex - 1 + PALETTE.length) % PALETTE.length;
    }

    private ColorPalette() {
    }
}
