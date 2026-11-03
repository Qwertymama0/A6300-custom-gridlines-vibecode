package com.example.customgridlines;

public enum OverlayType {

    // === COMPOSITION GRIDS ===
    GRID_RULE_OF_THIRDS("grid_rule_of_thirds", Category.GRIDS, "Rule of Thirds", "3x3 raster (33.3% / 66.6%)", false, 0f, 0), // White
    GRID_GOLDEN_RATIO("grid_golden_ratio", Category.GRIDS, "Golden Ratio (Phi)", "Phi snijlijnen (1 : 1.618)", false, 0f, 1), // Yellow
    GRID_GOLDEN_SPIRAL("grid_golden_spiral", Category.GRIDS, "Golden Spiral", "Fibonacci spiraalcurve", false, 0f, 4), // Cyan
    GRID_PHI_GRID("grid_phi_grid", Category.GRIDS, "Phi Grid", "38.2% | 23.6% | 38.2% raster", false, 0f, 8), // Gold
    GRID_DYNAMIC_SYMMETRY("grid_dynamic_symmetry", Category.GRIDS, "Dynamic Symmetry", "Harmonische armature & reciprocals", false, 0f, 6), // Magenta
    GRID_DIAGONAL_METHOD("grid_diagonal_method", Category.GRIDS, "Diagonal Method", "45° hoekdiagonalen", false, 0f, 5), // Orange
    GRID_BAROQUE_DIAGONAL("grid_baroque_diagonal", Category.GRIDS, "Baroque Diagonal", "Linksonder naar rechtsboven", false, 0f, 10), // Sky Blue
    GRID_SINISTER_DIAGONAL("grid_sinister_diagonal", Category.GRIDS, "Sinister Diagonal", "Linksboven naar rechtsonder", false, 0f, 2), // Red
    GRID_TRIANGULAR("grid_triangular", Category.GRIDS, "Triangular", "Driehoekscompositie", false, 0f, 7), // Lime
    GRID_CENTRAL("grid_central", Category.GRIDS, "Central Crosshair", "Middenkruis & focuscirkels", false, 0f, 3), // Green
    GRID_RADIAL("grid_radial", Category.GRIDS, "Radial (Vanishing)", "Verdwijnpunt straallijnen", false, 0f, 9), // Violet
    GRID_2_COLUMN("grid_2_column", Category.GRIDS, "2-Column (50/50)", "Centrale verticale scheiding", false, 0f, 11), // Silver
    GRID_3_COLUMN("grid_3_column", Category.GRIDS, "3-Column", "Driekoloms verticaal raster", false, 0f, 11),
    GRID_4_COLUMN("grid_4_column", Category.GRIDS, "4-Column", "Vierkoloms raster (25% stappen)", false, 0f, 11),
    GRID_RABATMENT("grid_rabatment", Category.GRIDS, "Rabatment", "Vierkanten vanuit korte zijde", false, 0f, 8),

    // === KLASSIEK & BREEDBEELD ===
    RATIO_1_1("ratio_1_1", Category.CLASSIC_WIDE, "1:1 Vierkant", "Instagram klassiek, profiel & product", true, 1.0f, 0),
    RATIO_5_4("ratio_5_4", Category.CLASSIC_WIDE, "5:4 (1.25:1)", "Grootformaat camera's", true, 1.25f, 1),
    RATIO_4_3("ratio_4_3", Category.CLASSIC_WIDE, "4:3 (1.33:1)", "Micro Four Thirds & standaard", true, 4f / 3f, 4),
    RATIO_3_2("ratio_3_2", Category.CLASSIC_WIDE, "3:2 (1.50:1)", "Native 35mm / A6300 sensor", true, 1.50f, 3),
    RATIO_7_5("ratio_7_5", Category.CLASSIC_WIDE, "7:5 (1.40:1)", "Klassieke printverhouding", true, 1.40f, 5),
    RATIO_16_10("ratio_16_10", Category.CLASSIC_WIDE, "16:10 (1.60:1)", "Monitoren & laptops", true, 1.60f, 10),
    RATIO_16_9("ratio_16_9", Category.CLASSIC_WIDE, "16:9 (1.78:1)", "YouTube, HD & 4K video", true, 16f / 9f, 1),
    RATIO_17_9("ratio_17_9", Category.CLASSIC_WIDE, "17:9 (1.89:1)", "DCI 4K Digital Cinema", true, 17f / 9f, 6),
    RATIO_18_9("ratio_18_9", Category.CLASSIC_WIDE, "18:9 (2.00:1)", "Moderne smartphones", true, 2.00f, 7),
    RATIO_19_5_9("ratio_19_5_9", Category.CLASSIC_WIDE, "19.5:9 (2.17:1)", "iPhone schermen", true, 19.5f / 9f, 9),
    RATIO_21_9("ratio_21_9", Category.CLASSIC_WIDE, "21:9 (2.33:1)", "Ultrawide monitoren", true, 21f / 9f, 4),
    RATIO_32_9("ratio_32_9", Category.CLASSIC_WIDE, "32:9 (3.56:1)", "Super ultrawide monitoren", true, 32f / 9f, 8),

    // === CINEMA FORMATS ===
    RATIO_1_85("ratio_1_85", Category.CINEMA, "1.85:1 Hollywood", "Hollywood bioscoopstandaard", true, 1.85f, 1),
    RATIO_2_00("ratio_2_00", Category.CINEMA, "2.00:1 Univisium", "Vittorio Storaro Univisium", true, 2.00f, 5),
    RATIO_2_20("ratio_2_20", Category.CINEMA, "2.20:1 70mm", "70mm filmformaat", true, 2.20f, 4),
    RATIO_2_35("ratio_2_35", Category.CINEMA, "2.35:1 Scope", "Klassieke anamorphic cinema", true, 2.35f, 2),
    RATIO_2_39("ratio_2_39", Category.CINEMA, "2.39:1 Bioscoop", "Moderne bioscoopfilms", true, 2.39f, 6),
    RATIO_2_76("ratio_2_76", Category.CINEMA, "2.76:1 Ultra Panavision", "Ultra Panavision 70", true, 2.76f, 8),

    // === VERTICAAL & SOCIAL ===
    RATIO_9_16("ratio_9_16", Category.VERTICAL_SOCIAL, "9:16 Verticaal", "TikTok, Reels, Shorts", true, 9f / 16f, 4),
    RATIO_4_5("ratio_4_5", Category.VERTICAL_SOCIAL, "4:5 Portrait", "Instagram portrait posts", true, 4f / 5f, 6),
    RATIO_2_3("ratio_2_3", Category.VERTICAL_SOCIAL, "2:3 Portret", "Klassieke staande foto", true, 2f / 3f, 1),
    RATIO_3_4("ratio_3_4", Category.VERTICAL_SOCIAL, "3:4 Mobiel", "Mobiele portretfotografie", true, 3f / 4f, 7),
    RATIO_1_91("ratio_1_91", Category.VERTICAL_SOCIAL, "1.91:1 Social Link", "LinkedIn & Facebook covers", true, 1.91f, 10);

    public enum Category {
        FAVORITES("⭐ Favorieten"),
        GRIDS("Compositie Grids"),
        CLASSIC_WIDE("Klassiek & Wide"),
        CINEMA("Cinema"),
        VERTICAL_SOCIAL("Verticaal & Social");

        public final String title;
        Category(String title) {
            this.title = title;
        }
    }

    public final String id;
    public final Category category;
    public final String displayName;
    public final String subtitle;
    public final boolean isAspectRatio;
    public final float ratioValue;
    public final int defaultColorIndex;

    OverlayType(String id, Category category, String displayName, String subtitle, boolean isAspectRatio, float ratioValue, int defaultColorIndex) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.subtitle = subtitle;
        this.isAspectRatio = isAspectRatio;
        this.ratioValue = ratioValue;
        this.defaultColorIndex = defaultColorIndex;
    }
}
