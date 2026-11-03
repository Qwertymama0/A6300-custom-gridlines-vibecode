# Specification: Sony A6300 Custom Gridlines & Composition Studio (Android 2.3.7 / OpenMemories)

A dedicated camera viewfinder companion app engineered specifically for the **Sony Alpha A6300** mirrorless camera running the **Android 2.3.7 (API Level 10)** OpenMemories / PMCA subsystem. It renders simultaneous composition guides and framing aspect ratio masks from a rich catalog, featuring a **Favorite/Starring system**, **independent per-overlay custom color assignment**, and a clean **`[✓]` checkbox multi-selection HUD menu** navigable via the camera's physical dials and buttons.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture Decisions for Sony A6300 (Android 2.3.7 Gingerbread)**
>
> 1. **Favorites / Quick-Access System**:
>    - Users can toggle a star `[★]` on any overlay.
>    - A dedicated **⭐ Favorites** tab appears first in the HUD menu, giving instant 1-click access to the photographer's personal go-to grids on the camera LCD/EVF.
> 2. **Independent Per-Overlay Color Assignment**:
>    - Every single grid/mask can be assigned its own distinct color from a rich pre-selectable palette.
>    - Broad high-contrast palette ensures that even when multiple overlays are active at once, each is visually distinct:
>      - **Pure White** (`#FFFFFF`)
>      - **Vivid Yellow** (`#FFEB3B`)
>      - **Signal Red** (`#F44336`)
>      - **Neon Green** (`#4CAF50`)
>      - **Electric Cyan** (`#00E5FF`)
>      - **Vibrant Orange** (`#FF9800`)
>      - **Hot Magenta** (`#E91E63`)
>      - **Lime Green** (`#AEEA00`)
>      - **Warm Amber/Gold** (`#FFD700`)
>      - **Violet Purple** (`#9C27B0`)
>      - **Sky Blue** (`#2196F3`)
>      - **Crisp Silver** (`#B0BEC5`)
>      - **Deep Black** (`#000000`, with subtle light contour for contrast against dark scenes)
> 3. **Simple Checkbox Selection `[✓]` vs `[ ]`**:
>    - Unambiguous visual indicators: `[✓]` indicates active/on; `[ ]` indicates inactive/off.
>    - D-Pad Center / Enter toggles active state instantly.
> 4. **Hardware Wheel & D-Pad Ergonomics**:
>    - **UP / DOWN**: Navigate through overlay rows.
>    - **LEFT / RIGHT**: Cycle through menu tabs (⭐ *Favorites*, *Grids*, *Classic & Wide*, *Cinema*, *Vertical*).
>    - **CENTER (DPAD_CENTER / ENTER)**: Toggle checkbox `[✓]` ON / `[ ]` OFF.
>    - **RIGHT DIAL / COLOR ACTION**: Cycle overlay color.
>    - **STAR TOGGLE (e.g. C1 or Long-press Center / Star Button)**: Toggle Favorite `[★]` / `[☆]`.
>    - **MENU Button**: Hide / Show the entire HUD for clean live shooting.
>    - **Touch / Click Fallback**: Every row, checkbox, star, and color chip is directly clickable for emulator testing.
> 5. **Runtime Compatibility (API Level 10)**: Strict Android 2.3.7 Java 6/7 bytecode compliance with reflection-based Sony `CameraEx` support and standard `Camera` fallback.

---

## 1. Supported Catalog & Default Colors

### 1.1 Aspect Ratios (Masked Framing Kaders)
* **Square (Vierkant)**:
  - `1:1` (Square / Instagram classic, profile, product)
* **Classic Photography (Klassieke fotografie)**:
  - `5:4` (1.25:1 - Large format)
  - `4:3` (1.33:1 - Micro Four Thirds, classic monitors)
  - `3:2` (1.50:1 - Native 35mm / Sony A6300 sensor baseline)
  - `7:5` (1.40:1 - Standard print)
* **Widescreen (Breedbeeld)**:
  - `16:10` (1.60:1 - Monitors, laptops)
  - `16:9` (1.78:1 - Standard HD / 4K UHD video, YouTube)
  - `17:9` (1.89:1 - DCI 4K Digital Cinema)
  - `18:9` (2.00:1 - Modern smartphone displays)
  - `19.5:9` (2.17:1 - iPhone display ratio)
  - `21:9` (2.33:1 - Ultrawide monitor)
  - `32:9` (3.56:1 - Super ultrawide)
* **Cinema Formats (Bioscoop & Film)**:
  - `1.85:1` (Hollywood Flat standard)
  - `2.00:1` (Univisium Vittorio Storaro standard)
  - `2.20:1` (70mm Panavision film)
  - `2.35:1` (Classic Anamorphic Cinemascope)
  - `2.39:1` (Modern Anamorphic theatrical standard)
  - `2.76:1` (Ultra Panavision 70)
* **Vertical & Social (Verticaal & Social Media)**:
  - `9:16` (0.56:1 - TikTok, IG Reels, YouTube Shorts)
  - `4:5` (0.80:1 - Instagram Portrait feed)
  - `2:3` (0.67:1 - Vertical portrait photography)
  - `3:4` (0.75:1 - Mobile portrait capture)
  - `1.91:1` (LinkedIn & Facebook landscape link preview)

### 1.2 Composition Grids & Aesthetic Systems
* **Rule of Thirds**: 3×3 grid dividing frame at 33.3% and 66.6% with power points.
* **Golden Ratio**: Phi lines at 38.2% and 61.8% ($1 : 1.618$).
* **Golden Spiral (Fibonacci)**: Logarithmic curve through successive Fibonacci proportional rectangles.
* **Phi Grid**: Harmonic photography grid applying phi spacing ($0.382 : 0.236 : 0.382$).
* **Dynamic Symmetry**: Harmonic armature with diagonals and reciprocal perpendiculars.
* **Diagonal Method**: 45° corner diagonals guiding dynamic visual flow.
* **Baroque & Sinister Diagonals**: Ascending and descending guide vectors.
* **Triangular Composition**: Symmetrical and apex-oriented triangles for portraits.
* **Central Composition**: Precision center crosshair and bullseye rings.
* **Radial Composition**: Radiating lines converging at single focal vanishing point.
* **Column Grids**: 2-column (50/50 split), 3-column, and 4-column layout guides.
* **Rabatment of the Rectangle**: Visual squares formed by folding the short dimension of the frame onto both sides.

---

## 2. User Experience & Visual Design

### 2.1 Live Viewfinder & HUD Layout

```
┌────────────────────────────────────────────────────────┐
│ [LIVE VIEW STREAM]                         [BAT 98%]   │
│ ┌────────────────── HUD SELECTION MENU ──────────────┐ │
│ │ [⭐ Favorites]  Grids  Classic  Cinema  Vertical    │ │
│ │ ────────────────────────────────────────────────── │ │
│ │ ▸ [✓] Rule of Thirds        [● Yellow]  [★ Starred]│ │
│ │   [✓] 16:9 Widescope        [● Cyan]    [★ Starred]│ │
│ │   [ ] Golden Spiral         [● Red]     [☆]        │ │
│ │   [✓] Center Crosshair      [● Green]   [★ Starred]│ │
│ │   [ ] 1:1 Square            [● White]   [☆]        │ │
│ │ ────────────────────────────────────────────────── │ │
│ │ [OK] Toggle  [DIAL] Color  [C1] Star  [MENU] Close │ │
│ └────────────────────────────────────────────────────┘ │
│ ───┼──────────────────────────────────────────────┼─── │
│    │                  + Center                    │    │
│ ───┼──────────────────────────────────────────────┼─── │
│    │                                              │    │
└────────────────────────────────────────────────────────┘
```

### 2.2 Navigation & Control Mapping (Sony A6300 Hardware Keys)
- **MENU Button**: Toggles the entire HUD menu ON / OFF (hides it completely for shooting).
- **UP / DOWN**: Navigates active row cursor up and down.
- **LEFT / RIGHT**: Switches category tabs (⭐ *Favorites*, *Grids*, *Classic*, *Cinema*, *Vertical*).
- **CENTER / ENTER**: Toggles active state: `[ ]` $\leftrightarrow$ `[✓]`.
- **C1 Button / Long-Press Center**: Toggles favorite star: `[☆]` $\leftrightarrow$ `[★]`.
- **C2 Button / Keycode Right Arrow on item**: Cycles item color forward through the palette.
- **TRASH Button**: Clears all active overlays.
- **Touch / Mouse Support**: In the browser emulator, clicking the checkbox `[✓]`, the color pill `[●]`, or the star `[★]` directly toggles them.

---

## 3. Technical Architecture & Component Structure

### 3.1 Architectural Diagram
```
┌───────────────────────────────────────────────────────────────────┐
│                           MainActivity                            │
│  - Android 2.3.7 Activity lifecycle & window management           │
│  - Key dispatch (D-Pad, Rotary Wheel, Menu, C1, C2)               │
│  - SurfaceHolder orchestration                                    │
└───────────────┬───────────────────────────────────┬───────────────┘
                │                                   │
                ▼                                   ▼
┌───────────────────────────────┐   ┌───────────────────────────────┐
│     SonyCameraAdapter         │   │       GridOverlayView         │
│  - Reflection: CameraEx       │   │  - Aspect ratio mask renderer │
│  - Fallback: Camera.open()    │   │  - Composition curve/grid math│
│  - Surface preview hook       │   │  - Per-overlay Paint instance │
└───────────────────────────────┘   └───────────────┬───────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │      HUDMenuOverlayView       │
                                    │  - Tabbed category manager    │
                                    │  - Checkbox & Star renderer   │
                                    │  - Per-item color selector    │
                                    │  - D-Pad & touch dispatcher   │
                                    └───────────────────────────────┘
                                                    │
                                                    ▼
                                    ┌───────────────────────────────┐
                                    │      OverlayStateManager      │
                                    │  - Set<OverlayId> activeItems │
                                    │  - Set<OverlayId> favorites   │
                                    │  - Map<OverlayId, Integer>    │
                                    │    itemColors                 │
                                    │  - SharedPreferences persist  │
                                    └───────────────────────────────┘
```

### 3.2 State Persistence (`OverlayStateManager`)
- Uses standard Android `SharedPreferences` (fully supported in Android 2.3.7) to remember:
  - Which overlays are favorited `[★]`.
  - Which overlays are currently active `[✓]`.
  - Assigned color for each overlay.
- Settings persist when opening and closing the app on the camera!

---

## 4. Agile Checkpoints & Milestone Gates

### Milestone 1: Core Geometry & Dynamic Color Rendering Engine (`GridOverlayView`)
- **Scope**:
  - Implement full mathematical drawing routines for all aspect ratios and composition geometries.
  - Per-overlay color support using independent `Paint` instances with high-contrast dual-stroke (drop contour for black/dark colors and bright colors against light backgrounds).
  - Multi-overlay compositing without alpha or clipping interference.
- **Verification Gate**: Standalone render test verifying that multiple overlays with different colors render crisply together.

### Milestone 2: Multi-Select HUD with Checkboxes, Color Selector & Favorites (`HUDMenuOverlayView`)
- **Scope**:
  - Implement tabbed HUD selector:
    - Tabs: `⭐ Favorieten`, `Compositie Grids`, `Klassiek & Wide`, `Cinema`, `Verticaal & Social`.
  - Clear `[✓]` / `[ ]` checkboxes for each overlay.
  - Color picker pill `[●]` displaying current color and cycling through the 13 distinct colors.
  - Favorite star toggle `[★]` / `[☆]`.
  - Full hardware D-Pad and emulator touch interaction.
  - Persistence via `SharedPreferences`.
- **Verification Gate**: Interactive navigation test verifying checkbox toggling, color changing, favorite starring, and persistence.

### Milestone 3: Sony CameraEx & OpenMemories Integration (`SonyCameraAdapter`)
- **Scope**:
  - Implement reflection-safe binding to `com.sony.scalar.hardware.CameraEx`.
  - Graceful fallback to `android.hardware.Camera` when run in emulators or standard Android environments.
  - Hardware button hooks for Sony A6300 shutter half-press, menu, and custom buttons.
- **Verification Gate**: Safe execution test ensuring zero crashes on devices without Sony CameraEx APIs.

### Milestone 4: Android 2.3.7 Build & Bytecode Compliance
- **Scope**:
  - Validate Gradle build with Android 2.3.7 (API Level 10) bytecode rules (Java 6/7 bytecode, zero modern AndroidX).
  - Produce valid `app-debug.apk` signed with debug keystore.
- **Verification Gate**: `compile_applet` build passes with APK generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 5. Verifiers & Evaluation Criteria (Before Code Changes)

| Evaluation Area | Pass Criteria | Verification Method |
|---|---|---|
| **Android 2.3.7 Compliance** | Zero AndroidX imports, no Java 8 lambdas without desugaring, API level 10 compatible. | Static code analysis & javac bytecode check. |
| **Favorites System** | Starred items appear in ⭐ Favorites tab; state persists across app launches. | SharedPreferences state check & tab filter. |
| **Color Customization** | Every overlay can independently select from the 13 distinct colors; renders with exact color on canvas. | Canvas paint verification & color cycling. |
| **Simple Checkbox Selection** | Each overlay clearly shows `[✓]` when active and `[ ]` when inactive; multiple can be active at once. | UI state toggle test. |
| **Physical Button Support** | Full navigation (up/down/left/right), color cycling, and starring achievable strictly through hardware keys. | Key event dispatch test. |
| **Touch Fallback** | All controls are interactive via mouse/touch in the browser emulator. | MotionEvent listener verification. |
| **Build Stability** | Build completes cleanly with `compile_applet`. | `compile_applet` execution. |

---

## 6. Recommendations for Knowledge Base, Skills & Guardrails

1. **Sony Keycode Reference**: Add a documented table of Sony OpenMemories keycodes:
   - `KEYCODE_DPAD_CENTER` (105 / 23)
   - `KEYCODE_MENU` (82)
   - `KEYCODE_BACK` (4)
   - `KEYCODE_CAMERA` (27)
   - Custom keys C1/C2 (mapped to Sony scalar scancodes).
2. **Bytecode Guardrail**: Rule ensuring any new classes avoid `java.util.function.*`, `java.util.stream.*`, or lambdas that generate `invokedynamic` instructions on Dalvik API 10.
