# Implementation Plan - Refine Image Export to Match Viewport 100%

## User Goal
The user wants the exported image (`renderStillImage`) to match 100% and look identical to what is displayed in the live animation viewport, while adhering precisely to the user's chosen export resolution.

## Technical Analysis
1. **Current Export Logic (`EngineHtmlBuilder.kt`)**:
   - Locates the largest canvas in the iframe (`__HARNESS__`).
   - Forces `canvas.width = TARGET_W` and `canvas.height = TARGET_H`.
   - Calls `__HARNESS__.step(0)` and exports via `canvas.toDataURL()`.
2. **Discrepancy Cause**:
   - If user code creates a canvas with hardcoded dimensions (e.g. 800x600) or CSS scaling, forcing `canvas.width = TARGET_W` directly without scaling drawing context or preserving aspect ratio/transform can distort graphics or miss what was rendered in the viewport canvas context.
   - To ensure 100% exact visual parity with the viewport while outputting at `TARGET_W` x `TARGET_H`:
     - We should ensure the render engine captures the exact drawing commands / context scale or copies the source canvas buffer scaled to the target resolution correctly maintaining aspect ratio / letterboxing / background fill, or scaling the context appropriately.
     - Alternatively, if the canvas in the viewport has its own internal width/height, we draw/scale it into an offscreen canvas of `TARGET_W x TARGET_H` or resize the canvas context properly before rendering.

## Proposed Changes
1. **Update `EngineHtmlBuilder.kt` (`renderStillImage`)**:
   - Inspect the source canvas from the live frame.
   - If the source canvas has width/height different from `TARGET_W`/`TARGET_H`, create a high-resolution offscreen staging canvas with dimensions `TARGET_W` and `TARGET_H`.
   - Scale the 2D context (`ctx.scale(TARGET_W / sourceCanvas.width, TARGET_H / sourceCanvas.height)`) or draw the source canvas scaled to `TARGET_W` x `TARGET_H` with smoothing enabled (`imageSmoothingEnabled = true`, `imageSmoothingQuality = 'high'`), ensuring 100% visual fidelity matching the viewport representation scaled up to the 4MP+ export resolution.
   - Ensure background color handling matches user code (respecting transparent PNG vs solid backgrounds).

## Verification Plan
- Compile applet with `compile_applet`.
- Verify no syntax errors in HTML builder generation.
