# Specification: Sony A6300 Custom Gridlines & Composition Studio (Android 2.3.7 / OpenMemories)

A dedicated camera viewfinder companion app engineered specifically for the **Sony Alpha A6300** mirrorless camera running the **Android 2.3.7 (API Level 10)** OpenMemories / PMCA subsystem. It renders simultaneous composition guides and framing aspect ratio masks from a rich catalog, featuring a **Favorite/Starring system**, **independent per-overlay custom color assignment**, and a clean **`[✓]` checkbox multi-selection HUD menu** navigable via the camera's physical dials and buttons.

---

## 1. Hardware Shutter & Autofocus Pipeline (Sony OpenMemories Integration)

> [!IMPORTANT]
> **Why Shutter & Autofocus Previously Failed & The Real Fix:**
>
> On Sony Alpha cameras running custom Android applications via the OpenMemories / PMCA subsystem, the background camera firmware is suspended while an Android Activity is active. The Android app owns the hardware lifecycle.
>
> In earlier builds, the app passed `KEYCODE_FOCUS` and `KEYCODE_CAMERA` to `super.onKeyDown()`, which in Android OS does nothing.
>
> **The Real Implementation:**
> 1. **Half-Press Shutter (S1) & Back-Button AF**:
>    - Intercepts `KeyEvent.KEYCODE_FOCUS`, Sony scan code `516` (S1 half-press), scan code `532` (AEL button assigned to back-button AF), and scan codes `519` / `521` (AF/MF lever).
>    - On key down: Automatically dismisses the HUD menu (if open) and invokes `camera.getNormalCamera().autoFocus(null)`.
>    - On key up: Invokes `camera.getNormalCamera().cancelAutoFocus()`.
> 2. **Full Shutter Press (S2)**:
>    - Intercepts `KeyEvent.KEYCODE_CAMERA` and Sony scan code `518` (S2 full press).
>    - On key down: Automatically dismisses the HUD menu (if open) and calls `camera.getNormalCamera().takePicture(null, null, null)`.
>      - Passing null callbacks allows the Sony BIONZ X image processor to capture full sensor RAW (ARW) and JPEG images directly to the camera's SD card (`DCIM/`) without consuming limited Android Dalvik heap memory.
>    - On key up: Calls `sonyCameraEx.cancelTakePicture()` and resumes the live viewfinder preview.

---

## 2. D-Pad Column Navigation: Color Selection & Starring System

> [!IMPORTANT]
> **Why Color & Star Were Unreachable & The New D-Pad Focus Engine:**
>
> Previously, cycling colors and starring items were mapped to PC keyboard keys (`1` and `2`), which physically do not exist on the Sony Alpha camera body. Additionally, Left and Right arrows immediately switched category tabs, leaving the user with no way to navigate horizontally to the Color pill or the Star icon.
>
> **The Column Navigation Solution:**
> 1. **3 Interactive Columns Per Row**:
>    - **Column 0 (`[✓]` Checkbox & Title)**: Pressing `ENTER` / `CENTER` toggles the active overlay `[✓]` / `[ ]`.
>    - **Column 1 (`[ ● Color ]` Color Pill)**: When focused, a bright golden outline (`#FFD700`) and arrows `◀ [ ● Color ] ▶` illuminate. Pressing `ENTER` / `CENTER` (or turning the control wheel) cycles colors!
>    - **Column 2 (`[★]` Star Icon)**: When focused, a golden focus box highlights the star. Pressing `ENTER` / `CENTER` toggles favorite status `[★]` / `[☆]`!
> 2. **D-Pad Directional Controls**:
>    - **`LEFT` / `RIGHT`**: Moves the focus cursor horizontally across columns:
>      `[✓ Checkbox]` $\longleftrightarrow$ `[● Color]` $\longleftrightarrow$ `[★ Star]`!
>      - Pressing `LEFT` while on Checkbox switches to the previous category tab.
>      - Pressing `RIGHT` while on Star switches to the next category tab.
>    - **`UP` / `DOWN`**: Navigates between rows in the list.
>      - Pressing `UP` from the top row jumps focus up to the **Category Tabs Header**!
> 3. **Category Tabs Header**:
>    - When focused on the Tab bar: `LEFT` and `RIGHT` switch between categories (`⭐ Favorieten`, `Compositie`, `Klassiek`, `Cinema`, `Verticaal`).
>    - Pressing `DOWN` or `ENTER` moves focus down into the item list.
> 4. **Hardware Dial & Shortcut Support**:
>    - **Top Control Dial (scan codes `525` / `526`)**: Switches category tabs immediately from anywhere!
>    - **Control Wheel Rotary (scan codes `522` / `523`)**:
>      - If on Color column: Cycles color forward / reverse.
>      - If on Checkbox or Star column: Scrolls rows up / down.
>    - **C1 Button (scan code `524`)**: Instant shortcut to toggle favorite `[★]`.
>    - **C2 / Trash Button (scan code `595`, `513`, `KEYCODE_CLEAR`)**: Instant shortcut to cycle color.
>    - **Fn Button (scan code `520`)**: Instant shortcut to cycle line stroke weight (Fine / Standard / Bold).
>    - **MENU Button (scan code `514`, `KEYCODE_MENU`)**: Toggles HUD menu visibility.
>    - **BACK Button (`KEYCODE_BACK`)**: Closes the HUD menu; double-press within 2.5s cleanly exits the app.

---

## 3. Supported Catalog & Palette

- **23 Aspect Ratios**: Square (`1:1`), Classic (`3:2`, `4:3`, `5:4`, `7:5`), Widescreen (`16:9`, `16:10`, `17:9`, `18:9`, `19.5:9`, `21:9`, `32:9`), Cinema (`1.85:1`, `2.00:1`, `2.20:1`, `2.35:1`, `2.39:1`, `2.76:1`), Vertical/Social (`9:16`, `4:5`, `2:3`, `3:4`, `1.91:1`).
- **15 Composition Guides**: Rule of Thirds, Golden Ratio, Golden Spiral (with 4-corner orientation flip), Phi Grid, Dynamic Symmetry, Diagonal Method, Baroque & Sinister Diagonals, Triangular, Center Crosshair, Radial, 2/3/4-Column Grids, Rabatment of Rectangle, and Virtual Horizon Level.
- **13 High-Contrast Colors**: White, Yellow, Red, Green, Cyan, Orange, Magenta, Lime, Gold, Violet, Sky Blue, Silver, Black.
- **Stroke Weights**: Fine (1.5px), Standard (2.5px), Bold (4.0px) with contrast shadow strokes for high visibility across any shooting scene.
- **Persistence**: All selections, colors, favorites, and settings persist automatically via `SharedPreferences`.
