---
name: Apex Vitality
colors:
  surface: '#131315'
  surface-dim: '#131315'
  surface-bright: '#39393b'
  surface-container-lowest: '#0e0e10'
  surface-container-low: '#1c1b1d'
  surface-container: '#201f22'
  surface-container-high: '#2a2a2c'
  surface-container-highest: '#353437'
  on-surface: '#e5e1e4'
  on-surface-variant: '#c5c9ac'
  inverse-surface: '#e5e1e4'
  inverse-on-surface: '#313032'
  outline: '#8f9378'
  outline-variant: '#444932'
  surface-tint: '#b0d500'
  primary: '#ffffff'
  on-primary: '#2a3400'
  primary-container: '#caf300'
  on-primary-container: '#596c00'
  inverse-primary: '#536600'
  secondary: '#c8c6c9'
  on-secondary: '#303033'
  secondary-container: '#47464a'
  on-secondary-container: '#b6b4b8'
  tertiary: '#ffffff'
  on-tertiary: '#303037'
  tertiary-container: '#e3e1ea'
  on-tertiary-container: '#64646b'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#caf300'
  primary-fixed-dim: '#b0d500'
  on-primary-fixed: '#171e00'
  on-primary-fixed-variant: '#3e4c00'
  secondary-fixed: '#e4e1e5'
  secondary-fixed-dim: '#c8c6c9'
  on-secondary-fixed: '#1b1b1e'
  on-secondary-fixed-variant: '#47464a'
  tertiary-fixed: '#e3e1ea'
  tertiary-fixed-dim: '#c7c5ce'
  on-tertiary-fixed: '#1b1b21'
  on-tertiary-fixed-variant: '#46464d'
  background: '#131315'
  on-background: '#e5e1e4'
  surface-variant: '#353437'
typography:
  display-lg:
    fontFamily: Archivo Narrow
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  display-sm:
    fontFamily: Archivo Narrow
    fontSize: 36px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Archivo Narrow
    fontSize: 24px
    fontWeight: '600'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Archivo Narrow
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.2'
  body-lg:
    fontFamily: Geist
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  body-md:
    fontFamily: Geist
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1'
    letterSpacing: 0.05em
  display-lg-mobile:
    fontFamily: Archivo Narrow
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.1'
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  unit-1: 4px
  unit-2: 8px
  unit-4: 16px
  unit-6: 24px
  unit-8: 32px
  container-margin: 20px
  gutter: 12px
---

## Brand & Style

The design system is engineered for peak human performance. It embodies a technical, high-performance aesthetic that prioritizes data density, biometric precision, and athletic urgency. The brand personality is disciplined, objective, and authoritative, moving away from soft wellness tropes toward a "human-machine" optimization philosophy.

The visual style is a fusion of **Modern Minimalism** and **Technical Athleticism**. It utilizes high-contrast interfaces, structured layouts, and a total absence of decorative ornamentation. Every element serves a functional purpose, reflecting the mindset of an elite athlete or a data-driven professional. The emotional response is one of focus, clarity, and readiness.

## Colors

The palette is anchored in a high-performance **Dark Mode** configuration to maximize contrast and minimize eye strain during intense sessions. 

- **Primary (Electric Lime):** Reserved exclusively for critical actions, progress milestones, and active state indicators. It should be used sparingly but with high impact.
- **Surface Palette:** Employs a range of deep slate grays (Charcoal and Zinc) to create structural hierarchy without relying on shadows.
- **Typography:** Pure white (#FFFFFF) is used for primary data points and headers, while muted grays are used for secondary metadata to maintain a clear information architecture.
- **Functional Colors:** Error states use a high-saturation red, while success states are subsumed by the primary Electric Lime to maintain a cohesive "active" language.

## Typography

Typography is used as a structural element. **Archivo Narrow** provides a condensed, technical feel for headlines and large-scale biometric data, allowing for high information density in horizontal layouts. **Geist** handles body copy and labels with its neutral, monospaced-adjacent precision, ensuring that numerical data remains legible and aligned.

Headlines should utilize uppercase styling to reinforce the disciplined, athletic tone. Numerical data within cards should prioritize font weight and size to ensure immediate glanceability during physical activity.

## Layout & Spacing

This design system utilizes a **Fixed-Fluid Hybrid Grid** based on an 8px rhythmic scale, optimized for data-heavy dashboards. 

- **Desktop:** 12-column grid with a maximum content width of 1280px.
- **Mobile:** 4-column fluid grid with 20px outside margins.
- **Philosophy:** Spacing is intentionally tight (8px-16px between related elements) to allow for more data "above the fold." Use consistent vertical rhythms to separate distinct workout phases or metric categories. Horizontal scrolling is permitted for chart timelines and secondary metric carousels.

## Elevation & Depth

Depth is achieved through **Tonal Layering** rather than traditional shadows. This maintains a "flat" but structured engineered look.

- **Level 0 (Background):** Deepest black/neutral (#09090B).
- **Level 1 (Cards/Surfaces):** Dark Zinc (#18181B).
- **Level 2 (In-app Overlays/Modals):** Lighter Zinc (#27272A) with a 1px solid border (#3F3F46).

Borders are the primary tool for separation. Use crisp, low-opacity outlines (1px) in neutral tones to define component boundaries. No blurs, no gradients, and no ambient shadows are allowed; the interface must feel physically solid and precisely cut.

## Shapes

The shape language is **Soft (Level 1)**. Elements utilize a 4px base radius (`rounded`) and up to 8px for large containers (`rounded-lg`). 

This subtle rounding provides just enough modern refinement to prevent the UI from feeling dated or overly "brutalist," while maintaining the aggressive, sharp-edged precision associated with high-performance automotive and aerospace instrumentation. Interactive elements like buttons should remain consistent with this 4px-8px logic—never use fully pill-shaped or circular buttons for primary actions.

## Components

- **Buttons:** Rectangular with 4px corners. Primary buttons use the Electric Lime background with black text for maximum legibility. Secondary buttons use a ghost style with a 1px Zinc border.
- **Data Cards:** High-density layouts. Place the metric label (Geist, 12px, Uppercase) at the top left and the primary value (Archivo Narrow, Bold) as the hero element.
- **Progress Rings:** High-visibility strokes (2px-4px) using the Primary color. Background tracks should be dark gray, never transparent, to show the goal's "container."
- **Input Fields:** Monospace-inspired text entry. 1px borders that change to Electric Lime on focus. Label text should sit above the field, never as a placeholder.
- **Chips/Tags:** Compact, square-edged, used for workout categories (e.g., "HIIT", "RECOVERY"). Dark gray background with white text.
- **Lists:** Clean, border-bottom separated rows. High-contrast titles with muted metadata. Use chevron-right icons only when the entire row is interactive.