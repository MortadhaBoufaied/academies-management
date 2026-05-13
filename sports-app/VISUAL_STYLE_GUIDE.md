# Modern Design System - Visual Style Guide

**Created**: May 12, 2026  
**Version**: 1.0  
**Status**: Production Ready

---

## 🎨 Color System

### Primary Color Palette
```
TEAL (Primary)
  Base:     #14B8A6  ▪ Colors.teal
  Dark:     #0D9488  ▪ Colors.teal.shade600
  Darker:   #0F766E  ▪ Colors.teal.shade700
  Light:    #20B2A7  ▪ Colors.teal (slightly lighter)
  Pale:     #CCEDE8  ▪ Colors.teal.shade100
  Ultra:    #F0FDFB  ▪ Colors.teal.shade50
```

### Accent Colors
```
GREEN (Success/Training)
  Base:     #22C55E  ▪ Colors.green
  Light:    #86EFAC  ▪ Colors.green.shade100
  Pale:     #DCFCE7  ▪ Colors.green.shade50

BLUE (Info/Statistics)
  Base:     #3B82F6  ▪ Colors.blue
  Light:    #BFDBFE  ▪ Colors.blue.shade100
  Pale:     #EFF6FF  ▪ Colors.blue.shade50

AMBER (Warning/Caution)
  Base:     #F59E0B  ▪ Colors.amber
  Light:    #FCD34D  ▪ Colors.amber.shade100
  Pale:     #FFFBEB  ▪ Colors.amber.shade50

RED (Error/Alert)
  Base:     #EF4444  ▪ Colors.red
  Light:    #FCA5A5  ▪ Colors.red.shade100
  Pale:     #FEE2E2  ▪ Colors.red.shade50
```

### Neutral Colors
```
WHITE (Base)
  Pure:     #FFFFFF  ▪ Colors.white
  Trans 50%: #FFFFFF80 ▪ Colors.white.withOpacity(0.5)
  Trans 60%: #FFFFFFCC ▪ Colors.white.withOpacity(0.6)

GRAY (Text & UI)
  800:      #1F2937  ▪ Colors.grey.shade800 (Primary text)
  700:      #374151  ▪ Colors.grey.shade700 (Secondary text)
  600:      #4B5563  ▪ Colors.grey.shade600 (Tertiary text)
  500:      #6B7280  ▪ Colors.grey.shade500
  100:      #F3F4F6  ▪ Colors.grey.shade100
  50:       #F9FAFB  ▪ Colors.grey.shade50
```

---

## 📐 Spacing & Layout

### Horizontal Spacing
```
Tight:     8px    ▪ const EdgeInsets.symmetric(horizontal: 8)
Standard:  12px   ▪ const EdgeInsets.symmetric(horizontal: 12)
Relaxed:   16px   ▪ const EdgeInsets.symmetric(horizontal: 16)
Large:     20px   ▪ const EdgeInsets.symmetric(horizontal: 20)
```

### Vertical Spacing
```
Minimal:   8px    ▪ const SizedBox(height: 8)
Small:     12px   ▪ const SizedBox(height: 12)
Medium:    16px   ▪ const SizedBox(height: 16)
Large:     20px   ▪ const SizedBox(height: 20)
Section:   24px   ▪ const SizedBox(height: 24)
```

### Card Dimensions
```
Padding:   14px to 16px
Margin:    12px (symmetric)
Radius:    12px to 16px
Min Height: 50px (buttons)
Min Touch:  48px (targets)
```

---

## 🔤 Typography System

### Heading Styles
```
Size:       18px - 24px
Weight:     FontWeight.bold (w700)
Color:      Colors.teal.shade700
Line Height: 1.2
Font:       Roboto
Example:    "Dashboard", "Players Stats"
```

### Label Styles
```
Size:       13px - 15px
Weight:     FontWeight.w600
Color:      Colors.grey.shade800
Line Height: 1.4
Font:       Roboto
Example:    "Total Players", "Action Required"
```

### Body Text Styles
```
Size:       14px - 16px
Weight:     FontWeight.w500 to w600
Color:      Colors.grey.shade700
Line Height: 1.5
Font:       Roboto
Example:    Long descriptions, paragraphs
```

### Caption Styles
```
Size:       12px - 13px
Weight:     FontWeight.w500
Color:      Colors.grey.shade600
Line Height: 1.4
Font:       Roboto
Example:    Timestamps, helper text
```

---

## 🎯 Component Styles

### ModernCard
```
Background:    Colors.white.withOpacity(0.5)
Radius:        12px - 16px
Padding:       14px - 16px
Shadow:        Soft (blurRadius: 6)
Offset:        0, 3
Color:         Colors.black.withOpacity(0.12)
Border:        Optional corner decorations
```

### Corner Border
```
Color:         Colors.teal (or accent color)
Width:         2px
Length:        20px (corner size)
Corners:       All four
Style:         Linear path
```

### Decorative Circle (Background)
```
Size:          180px - 220px
Shape:         Circle
Gradient:      Radial
Colors:        Green or Blue with opacity
Opacity:       0.25 - 0.35
Position:      Absolute, outside viewport
Purpose:       Background decoration
```

### QuickAccessButton
```
Icon Size:     28px
Icon Color:    Solid color (green/blue/amber)
BG Color:      Color.withOpacity(0.15)
BG Shape:      Circle
Size:          50px diameter
Text Size:     13px
Text Weight:   w600
Spacing:       8px below icon
```

### StatusIndicator
```
Dot Size:      8px
Dot Color:     Status color
Text Size:     13px
Text Weight:   w600
Spacing:       6px between dot and text
Colors:        Green (active), Amber (pending), Red (inactive)
```

### ModernButton
```
Height:        48px - 56px
Padding:       12px x 20px
Radius:        12px
Background:    Solid teal or accent
Text Color:    White
Text Size:     16px
Text Weight:   w600
Shadow:        Soft elevation
Hover:         Slight opacity change
Disabled:      Reduced opacity
```

### ModernTextField
```
Height:        48px
Radius:        12px
Padding:       12px x 16px
Background:    Grey.shade100.withOpacity(0.6)
Border:        Grey.shade300 (enabled)
Border Focus:  Teal, 2px (focused)
Icon Color:    Teal (optional)
Icon Size:     20px
Label Size:    14px
Label Weight:  w600
Hint Color:    Grey.shade600
```

---

## 🌓 Light & Dark Mode

### Light Mode
```
Background:    Colors.white / Grey.shade50
Card BG:       Colors.white.withOpacity(0.5-0.6)
Text Primary:  Colors.grey.shade800
Text Secondary: Colors.grey.shade700
Borders:       Grey.shade300
Shadows:       Colors.black.withOpacity(0.06-0.12)
```

### Dark Mode
```
Background:    Dark grey / Near black
Card BG:       Slightly lighter dark with 0.5-0.6 opacity
Text Primary:  Colors.white.withOpacity(0.92)
Text Secondary: Colors.white.withOpacity(0.72)
Borders:       Colors.white.withOpacity(0.14-0.28)
Shadows:       Colors.black.withOpacity(0.25)
Accents:       Same (teal/green/blue unchanged)
```

---

## 🔄 Interactive States

### Buttons
```
Default:    Full opacity, soft shadow
Hover:      Slight opacity change, shadow grows
Active:     Darker shade, pressed appearance
Disabled:   0.5 opacity, no interactions
Loading:    Spinning indicator, disabled state
Focus:      Ring outline (accessibility)
```

### Input Fields
```
Idle:       Grey border, light background
Focused:    Teal border (2px), same background
Error:      Red border, error message shown
Disabled:   Reduced opacity, no interactions
Success:    Teal border with checkmark
```

### Navigation Items
```
Idle:       Grey text, grey icon
Active:     Teal text, teal icon, underline
Hover:      Slight highlight
Pressed:    Darker shade
Focus:      Ring outline
```

---

## 📐 Responsive Breakpoints

### Phone (Small)
```
Width:         < 600px
Card Margins:  12px
Text Size:     Reduced 10-15%
Spacing:       12px standard
Layout:        Single column
```

### Tablet (Medium)
```
Width:         600px - 900px
Card Margins:  16px
Text Size:     Standard
Spacing:       16px standard
Layout:        2 column where possible
```

### Desktop (Large)
```
Width:         > 900px
Card Margins:  20px
Text Size:     Standard
Spacing:       20px standard
Layout:        3+ column layouts
Max Width:     1200px
```

---

## 🎭 Decorative Elements

### Gradient Circles (Background)
```
Top-right Green Circle:
  Size:        180px
  Position:    -40, -40 (outside viewport)
  Color:       Colors.green.shade100
  Opacity:     0.35
  Gradient:    Radial

Bottom-left Blue Circle:
  Size:        220px
  Position:    -60, bottom 60
  Color:       Colors.blue.shade100
  Opacity:     0.25
  Gradient:    Radial

Purpose: Subtle background decoration
Effect:  Adds visual interest without clutter
```

### Shadow Effects
```
Soft Shadow:
  Color:       Colors.black.withOpacity(0.12)
  Blur:        6px
  Offset:      0, 3px
  Purpose:     Card elevation

Medium Shadow:
  Color:       Colors.black.withOpacity(0.1)
  Blur:        12px
  Offset:      0, 6px
  Purpose:     Deeper elements

Strong Shadow:
  Color:       Colors.black.withOpacity(0.15)
  Blur:        18px
  Offset:      0, 10px
  Purpose:     Floating elements
```

### Backdrop Blur
```
Blur Amount:   6px (sigmaX & sigmaY)
Use Cases:     Overlay dialogs, loading states
Effect:        Softly blurs content behind
Combined:      White.withOpacity(0.5) on top
```

---

## 🔤 Text Hierarchy

### Level 1 - Page Title
```
Size:    22px - 24px
Weight:  Bold (w700)
Color:   Colors.teal.shade700
Space:   Below content
Example: "Admin Dashboard", "Player Profile"
```

### Level 2 - Section Title
```
Size:    18px
Weight:  Bold (w700)
Color:   Colors.teal.shade700
Space:   12px below
Example: "Quick Access", "Contact Us"
```

### Level 3 - Card Title
```
Size:    16px
Weight:  Semi-bold (w600)
Color:   Colors.grey.shade800
Space:   8px below
Example: "Player Stats", "Upcoming Events"
```

### Level 4 - Label/Field Name
```
Size:    14px
Weight:  Semi-bold (w600)
Color:   Colors.grey.shade700
Space:   4-8px below
Example: "Email Address", "Date of Birth"
```

### Level 5 - Body Text
```
Size:    14px - 16px
Weight:  Regular (w500)
Color:   Colors.grey.shade700
Line:    1.5
Example: Descriptions, long content
```

---

## 🎨 Component Variations

### Card Variations
```
1. Standard Card
   - Background: White.withOpacity(0.5)
   - No special decoration
   - Use for: Regular content

2. Corner Border Card
   - Background: White.withOpacity(0.5)
   - Corner borders: Teal, 20px
   - Use for: Important sections

3. Blur Card
   - Background: White.withOpacity(0.5)
   - Backdrop blur: 6px
   - Use for: Overlays, modals

4. Colored Card
   - Background: Custom color + opacity
   - Use for: Status displays, metrics
```

### Button Variations
```
1. Primary Button (Teal)
   - BG: Colors.teal
   - Text: Colors.white
   - Use for: Main actions

2. Secondary Button (Accent)
   - BG: Colors.blue / green / amber
   - Text: Colors.white
   - Use for: Alternative actions

3. Outlined Button
   - BG: Transparent
   - Border: Teal
   - Text: Colors.teal
   - Use for: Secondary options

4. Ghost Button
   - BG: Transparent
   - Text: Teal
   - No border
   - Use for: Tertiary actions
```

---

## 🌍 RTL Considerations

### Text Direction
```
Default:  TextDirection.ltr (left-to-right)
Arabic:   TextDirection.rtl (right-to-left)
Always:   Wrap main content in Directionality widget
```

### Alignment
```
Start:    Aligns to left (LTR) or right (RTL)
End:      Aligns to right (LTR) or left (RTL)
Avoid:    Left/Right (use Start/End instead)
Icons:    Mirror horizontally in RTL
```

### Spacing
```
PaddingStart:   Left margin in LTR, right in RTL
PaddingEnd:     Right margin in LTR, left in RTL
Standard:       12px - 16px for all languages
```

---

## ✨ Best Practices

### Color Usage
- ✅ Use teal for primary actions
- ✅ Use accents for different sections (green, blue, amber)
- ✅ Ensure sufficient contrast (WCAG AA minimum)
- ✅ Limit to 5-6 main colors max
- ❌ Don't use pure black (#000000)
- ❌ Don't use clashing color combinations

### Spacing
- ✅ Use consistent 12px base unit
- ✅ Scale spacing by multiples (8, 12, 16, 20, 24)
- ✅ Use more space on main sections
- ✅ Balance white space
- ❌ Don't mix arbitrary spacing values
- ❌ Don't cram too much into small spaces

### Typography
- ✅ Use Roboto font consistently
- ✅ Maintain 1.4-1.5 line height
- ✅ Use weight variation (w500-w700)
- ✅ Limit to 3 font sizes per section
- ❌ Don't use decorative fonts
- ❌ Don't mix multiple font families

### Shadows
- ✅ Use subtle soft shadows
- ✅ Increase shadow for elevation
- ✅ Apply consistently across similar elements
- ❌ Don't use harsh/dark shadows
- ❌ Don't shadow every element

---

## 📦 Export Guidelines

### For Designers
```
- Use these exact color hex codes
- Follow spacing in multiples of 4px
- Use Roboto font exclusively
- Apply shadows as specified
- Maintain 1:1 aspect ratio for icons
```

### For Developers
```
- Use Colors.teal instead of hex values
- Use standard EdgeInsets (12, 16, 20)
- Use FontWeight.w500, w600, w700
- Copy shadow definitions from guide
- Use Icons from Flutter Material
```

---

## 🎯 Quick Verification

Use this checklist to verify any new design matches the system:

- [ ] Colors from approved palette
- [ ] Spacing in 4px/8px/12px/16px/20px/24px
- [ ] Fonts using Roboto only
- [ ] Text hierarchy clear
- [ ] Shadows soft and subtle
- [ ] Radius 12px-16px for cards
- [ ] Components using defined styles
- [ ] Buttons at least 48px tall
- [ ] Touch targets at least 48x48px
- [ ] Contrast ratio >= 4.5:1 (text)

---

## 📋 File References

For implementation:
- Main System: `lib/components/modern_design_system.dart`
- Background: `lib/components/enhanced_app_background.dart`
- Example: `lib/screens/user/profile_screen.dart`

For guides:
- Full Documentation: `MODERN_DESIGN_SYSTEM_GUIDE.md`
- Examples: `IMPLEMENTATION_EXAMPLES.md`
- Quick Reference: `QUICK_REFERENCE.md`

---

**Version**: 1.0  
**Last Updated**: May 12, 2026  
**Status**: Approved for Use ✅  
**Maintained By**: Design & Development Team
