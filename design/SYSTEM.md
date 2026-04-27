# Design System

The visual identity is what makes Dreamloom feel like an heirloom rather than a utility. Get this right and the screenshots sell themselves.

---

## Mood board (one-line)

> Late-night, candlelit, cosmic — half tarot deck, half observatory. Restrained. Reverent. Modern.

---

## Color tokens

Dark theme is canonical. Light theme is supported but secondary.

```kotlin
object DreamColors {
    // Backgrounds
    val Night       = Color(0xFF0A0820)   // base canvas
    val Indigo      = Color(0xFF1A1240)   // surface
    val IndigoSoft  = Color(0xFF231854)   // surface variant
    val Violet      = Color(0xFF2D1B69)   // gradient mid

    // Accents
    val Aurora      = Color(0xFF6B3FC4)   // primary
    val AuroraSoft  = Color(0xFF8B5CF6)   // primary variant
    val Moonglow    = Color(0xFFD4B483)   // gold accent
    val MoonglowSoft= Color(0xFFFBE9C3)

    // Mood colors (5 moods)
    val MoodSerene  = Color(0xFF7DD3FC)   // pale aqua
    val MoodAnxious = Color(0xFFE5B8FC)   // muted lilac
    val MoodJoyful  = Color(0xFFFCD34D)   // warm gold
    val MoodLost    = Color(0xFF6B7280)   // soft gray
    val MoodFierce  = Color(0xFFEF4444)   // ember

    // Text
    val Ink         = Color(0xFFF5F3FF)   // body on dark
    val InkMuted    = Color(0xFFA5A4C7)
    val InkFaint    = Color(0xFF6B6890)

    // System
    val Danger      = Color(0xFFEF4444)
    val Success     = Color(0xFF34D399)
}
```

The signature gradient — used as the app background almost everywhere — is:

```
linear-gradient(180deg, #0F0A2E 0%, #2D1B69 50%, #6B3FC4 100%)
```

Apply it as a `Brush.verticalGradient` on the root `Box`. Beneath it, an animated star-field at 8% opacity (canvas drawing of small dots, slowly drifting).

---

## Typography

Two type families, no more.

| Family | Use |
| --- | --- |
| **Cormorant Garamond** | Display + headlines (titles, big numbers). Adds gravitas. |
| **Inter** | Body, UI, labels. Functional and legible. |

A single small italic moment uses **Cormorant Italic** for the dream's raw text, to feel literary.

```kotlin
val DreamloomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Cormorant,
        fontSize = 56.sp, lineHeight = 64.sp, fontWeight = FontWeight.Medium
    ),
    displayMedium = TextStyle(fontFamily = Cormorant, fontSize = 44.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = Cormorant, fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = Cormorant, fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium= TextStyle(fontFamily = Cormorant, fontSize = 22.sp, lineHeight = 30.sp),

    titleLarge    = TextStyle(fontFamily = Inter, fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleMedium   = TextStyle(fontFamily = Inter, fontSize = 16.sp, fontWeight = FontWeight.Medium),

    bodyLarge     = TextStyle(fontFamily = Inter, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = Inter, fontSize = 12.sp, lineHeight = 16.sp),

    labelLarge    = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                              letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = Inter, fontSize = 11.sp, letterSpacing = 1.sp),
)
```

Section labels (e.g., "INTERPRETATION") use `labelSmall` with `letterSpacing = 1.5.sp` and `Color = InkFaint` for that engraved-in-glass feel.

---

## Spacing

Use a `4dp` baseline:

```kotlin
object DreamSpacing {
    val xxs = 4.dp
    val xs  = 8.dp
    val sm  = 12.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp
    val xxxl= 64.dp
}
```

Default screen horizontal padding: `24.dp`. Cards within a screen: `16.dp` internal padding. Bottom nav inset + WindowInsets handled by Compose `Scaffold`.

---

## Shapes

```kotlin
val DreamShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(20.dp),
    large      = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)
```

Cards use `medium`. The moon button is a perfect circle (`CircleShape`).

---

## Components — sketch specs

### MoonButton (Home)

```
size              ~280.dp on phone, scales with min(width, height) * 0.42
fill              radial gradient — center: #D4B483 at 0.9 alpha → edge: transparent
stroke            1dp inner ring at #D4B48340
inner glow        soft shadow color = #D4B483, radius = 24dp, alpha = 0.4
animation         scale 1.0 → 1.04 → 1.0 over 2s, ease-in-out, infinite
press             scale 0.96 over 120ms with haptic FEEDBACK_TICK
```

Use `Modifier.shadow(elevation = 0.dp)` and draw the glow manually with `drawBehind { drawCircle(...) }`.

### DreamCard (Atlas)

```
height: wrap content
padding: 16.dp
background: DreamColors.Indigo with 70% opacity over canvas
border: 1.dp DreamColors.Aurora with 25% opacity
corner: 20.dp
elevation: 0 (use border + alpha instead — looks more elegant on dark)
```

Layout:
```
[date row: "Apr 12" gold,  • mood-color dot]
[title: 22sp Cormorant, max 2 lines]
[preview: 14sp Inter Muted, 2 lines, ellipsis]
[symbol chips: small, 1.5dp border, no fill, gold text]
```

### SymbolChip

```
height: 24.dp
horizontal padding: 10.dp
background: transparent
border: 1.dp DreamColors.Moonglow at 50% opacity
text: 12sp Inter labelSmall, color: Moonglow
shape: full pill
```

### Section label

A small all-caps label with bracketing horizontal lines:

```
─── SYMBOLS ───
```

Implemented as a `Row` with two `Spacer(Modifier.weight(1f).height(1.dp).background(InkFaint.copy(alpha=0.3f)))` flanking a `Text` of `labelSmall`.

### Bottom nav

Material 3 `NavigationBar` with custom theme: surface = `Color.Transparent`, tonal elevation = 0, item content color via `NavigationBarItemDefaults.colors(...)`. Add a subtle top divider at 1.dp `InkFaint at 15%`.

Icons: line icons, not filled. Use `Icons.Outlined.*` from material-icons-extended.

### Streaming "loom" animation

A canvas drawing of 12 horizontal lines, each 1.dp tall, drifting horizontally at slightly different speeds (1.2x–2.4x), each at gold-on-indigo with 30–60% opacity. Lasts the duration of the stream. When the stream completes, lines settle into a horizontal calm.

```kotlin
@Composable
fun LoomAnimation(modifier: Modifier = Modifier) { /* ... */ }
```

---

## Iconography

Custom set, monoline, 24dp grid:

- 🌙 Moon (crescent waxing/waning, used as primary brand symbol)
- 📖 Book (Atlas)
- 🧭 Compass (Insight)
- ✦ Spark (rewarded ad CTAs — distinctive enough to read instantly)
- ⚙ Gear (Settings)

Get them from Phosphor Icons (free, MIT) or Tabler Icons. Pick one library and stick to it.

---

## Motion

- All transitions: **250ms** ease-out, 50ms slide-up of 16.dp.
- Haptic feedback on: moon tap (FEEDBACK_TICK), save (FEEDBACK_CONFIRM), wipe-everything confirmation (FEEDBACK_REJECT).
- No bouncy / "fun" animations. We are reverent, not playful.

---

## Light theme

Same tokens, inverted backgrounds:

```
Night → #FAF7FF
Indigo → #FFFFFF surface
Aurora gradient still appears in the moon button + the Insight chart (bright accent)
Ink → #1A1240
```

Most users will never use this. It's there for accessibility and battery preference. Don't over-invest.

---

## Accessibility

- Min text size scaling honored (no hard-coded `dp` for text).
- Color contrast AA on all text vs background. Verify with `Contrast Analyser`.
- Content descriptions on every interactive element. The moon button: `"Record a new dream"`.
- Reduced motion: when `Settings.Global.TRANSITION_ANIMATION_SCALE == 0`, skip the loom animation and show static "Weaving…" text.
