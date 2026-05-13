# 🎨 SPORTS APP - CENTRALIZED THEME SYSTEM

## Overview

All colors in the sports academy app are now centralized in a single file: **`lib/theme/app_colors.dart`**

This ensures:
- ✅ Consistency across all screens
- ✅ Easy theme modifications (change color in one place)
- ✅ Semantic color usage (meaningful names)
- ✅ Dark mode support
- ✅ Professional theming standards

---

## 📁 File Structure

```
lib/theme/
├── app_colors.dart          ← MAIN: All color definitions
├── app_theme.dart           ← Material theme configuration
├── THEME_USAGE_GUIDE.dart   ← How to use the theme
└── (this file)              ← Theme documentation
```

---

## 🎯 Color Categories

### 1. PRIMARY PALETTE
Core brand colors used throughout the app.

| Color | Value | Usage |
|-------|-------|-------|
| **Primary Teal** | `#009688` | Main brand color |
| **Primary Teal Dark** | `#00796B` | Hover/active states |
| **Primary Teal Light** | `#4DB6AC` | Light accents |
| **Secondary Orange** | `#FF9800` | Secondary actions |
| **Secondary Violet** | `#7C3AED` | Scouting screens |
| **Secondary Blue** | `#2563EB` | Info/primary actions |

### 2. SEMANTIC COLORS
Status and feedback colors with consistent meaning.

| Color | Purpose | Usage |
|-------|---------|-------|
| **Success Green** | `#22C55E` | Paid, Completed, Success |
| **Warning Orange** | `#F59E0B` | Pending, Caution |
| **Error Red** | `#FF6B6B` | Failed, Error, Cancelled |
| **Info Blue** | `#3B82F6` | Information, Upcoming |
| **Pending Gray** | `#6B7280` | Neutral, Disabled |

### 3. BACKGROUNDS & SURFACES
Light and dark mode backgrounds.

**Light Mode:**
- `bgLight` - Main background
- `bgLightPure` - Pure white
- `bgLightSecondary` - Secondary surface
- `bgLightTertiary` - Tertiary surface

**Dark Mode:**
- `bgDark` - Main dark background
- `bgDarkSecondary` - Dark secondary
- `bgDarkTertiary` - Dark tertiary
- `bgDarkCard` - Dark card background

### 4. TEXT COLORS
Typography hierarchy.

**Light Mode:**
- `textDark` - Primary text
- `textDarkSecondary` - Secondary text
- `textDarkTertiary` - Tertiary text

**Dark Mode:**
- `textLight` - Primary text
- `textLightSecondary` - Secondary text
- `textLightTertiary` - Tertiary text

---

## 📱 Screen-Specific Themes

### HOME / DASHBOARD
- **Primary:** Teal
- **Secondary:** Orange
- **Accent:** Lime
- **Usage:** Main dashboard with quick stats

```dart
// Use these in home screen
AppColors.homePrimary         // Teal
AppColors.homeSecondary       // Orange
AppColors.homeAccent          // Lime
AppColors.homeCardBg          // Light background
```

### PLAYER MANAGEMENT
- **Primary:** Teal
- **Secondary:** Blue
- **Accent:** Green
- **Usage:** Player cards, listings, profiles

```dart
// Use these in player screen
AppColors.playerPrimary       // Teal
AppColors.playerSecondary     // Blue
AppColors.playerAccent        // Green
AppColors.playerCardBg        // Light teal background
```

### SCOUTING REPORTS
- **Primary:** Violet
- **Secondary:** Electric Blue
- **Accent:** Lime
- **Status Colors:** Green (Excellent), Blue (Good), Orange (Average), Red (Poor)
- **Usage:** Talent evaluation, scoring, assessments

```dart
// Use these in scouting screen
AppColors.scoutingPrimary     // Violet
AppColors.scoutingSecondary   // Blue
AppColors.scoutingAccent      // Lime
AppColors.scoutingExcellent   // Green
AppColors.scoutingGood        // Blue
AppColors.scoutingAverage     // Orange
AppColors.scoutingPoor        // Red
```

### TRAINING SESSIONS
- **Primary:** Pitch Green
- **Secondary:** Orange
- **Accent:** Lime
- **Status Colors:** Green (Active), Blue (Scheduled), Teal (Completed), Red (Cancelled)
- **Usage:** Training schedules, activity tracking

```dart
// Use these in training screen
AppColors.trainingPrimary     // Pitch Green
AppColors.trainingSecondary   // Orange
AppColors.trainingAccent      // Lime
AppColors.trainingActive      // Green
AppColors.trainingScheduled   // Blue
AppColors.trainingCompleted   // Teal Light
AppColors.trainingCancelled   // Red
```

### MATCHES
- **Primary:** Orange
- **Secondary:** Teal
- **Accent:** Blue
- **Status Colors:** Blue (Upcoming), Red (Live), Green (Completed), Gray (Cancelled)
- **Usage:** Match schedules, results, statistics

```dart
// Use these in match screen
AppColors.matchPrimary        // Orange
AppColors.matchSecondary      // Teal
AppColors.matchAccent         // Blue
AppColors.matchUpcoming       // Blue
AppColors.matchLive           // Red
AppColors.matchCompleted      // Green
AppColors.matchCancelled      // Gray
```

### PAYMENTS
- **Primary:** Green
- **Secondary:** Orange
- **Accent:** Blue
- **Status Colors:** Green (Paid), Orange (Pending), Red (Failed)
- **Usage:** Invoices, transactions, subscriptions

```dart
// Use these in payment screen
AppColors.paymentPrimary      // Green
AppColors.paymentSecondary    // Orange
AppColors.paymentAccent       // Blue
AppColors.paymentPaid         // Green
AppColors.paymentPending      // Orange
AppColors.paymentFailed       // Red
AppColors.paymentDue          // Orange
```

### ADMIN PANEL
- **Primary:** Teal
- **Secondary:** Blue
- **Accent:** Violet
- **Stat Colors:** Teal (Players), Orange (Divisions), Blue (Activities), Green (Payments)
- **Usage:** System administration, analytics

```dart
// Use these in admin screen
AppColors.adminPrimary        // Teal
AppColors.adminSecondary      // Blue
AppColors.adminAccent         // Violet
AppColors.adminStatPlayers    // Teal
AppColors.adminStatDivisions  // Orange
AppColors.adminStatActivities // Blue
AppColors.adminStatPayments   // Green
```

### PROFILE
- **Primary:** Teal
- **Secondary:** Violet
- **Accent:** Orange
- **Usage:** User profiles, settings, personal info

```dart
// Use these in profile screen
AppColors.profilePrimary      // Teal
AppColors.profileSecondary    // Violet
AppColors.profileAccent       // Orange
```

### CHAT / MESSAGING
- **Primary:** Teal
- **Secondary:** Violet
- **Accent:** Blue
- **Message Bubbles:** Teal (sent), Light Gray (received)
- **Usage:** Real-time messaging

```dart
// Use these in chat screen
AppColors.chatPrimary         // Teal
AppColors.chatSecondary       // Violet
AppColors.chatAccent          // Blue
AppColors.chatBubbleSent      // Teal
AppColors.chatBubbleReceived  // Light Gray
```

---

## 🎨 COLOR USAGE EXAMPLES

### ❌ DON'T - Hardcoded colors

```dart
// BAD: Colors hardcoded everywhere
Container(
  color: Color(0xFF009688),  // What is this color?
  child: Text('Player', style: TextStyle(color: Color(0xFF152724))),
)
```

### ✅ DO - Use AppColors

```dart
// GOOD: Clear semantic meaning
Container(
  color: AppColors.playerPrimary,  // Teal for player screen
  child: Text('Player', style: TextStyle(color: AppColors.textDark)),
)
```

### ❌ DON'T - Different colors for same status

```dart
// BAD: Inconsistent status colors
if (status == 'paid') return Colors.green;          // Wrong shade
if (status == 'pending') return Colors.orange;      // Different orange
if (status == 'failed') return Colors.red;          // Different red
```

### ✅ DO - Consistent semantic colors

```dart
// GOOD: Semantic colors used consistently
if (status == 'paid') return AppColors.paymentPaid;
if (status == 'pending') return AppColors.paymentPending;
if (status == 'failed') return AppColors.paymentFailed;
```

---

## 🌓 Dark Mode Support

### Helper Methods

```dart
// Get appropriate background color for current mode
Color bg = AppColors.getBackgroundColor(isDark: isDark);

// Get appropriate text color for current mode
Color text = AppColors.getTextColor(isDark: isDark);

// Get secondary text color
Color secondaryText = AppColors.getSecondaryTextColor(isDark: isDark);

// Get surface color
Color surface = AppColors.getSurfaceColor(isDark: isDark);

// Get border color
Color border = AppColors.getBorderColor(isDark: isDark);
```

### Example Implementation

```dart
Widget buildCard(bool isDark) {
  return Card(
    color: AppColors.getSurfaceColor(isDark: isDark),
    child: Text(
      'Content',
      style: TextStyle(
        color: AppColors.getTextColor(isDark: isDark),
      ),
    ),
  );
}
```

---

## 📝 How to Update Theme

### Change a Single Color

1. Open `lib/theme/app_colors.dart`
2. Find the color definition
3. Update the hex value

```dart
// BEFORE
static const Color primaryTeal = Color(0xFF009688);

// AFTER
static const Color primaryTeal = Color(0xFF009999);  // New shade
```

### Change an Entire Screen's Theme

1. Open `lib/theme/app_colors.dart`
2. Find the screen-specific color section
3. Update all colors for that screen

```dart
// Example: Update scouting colors
static const Color scoutingPrimary = Color(0xFF7C3AED);    // Violet
static const Color scoutingSecondary = Color(0xFF2563EB);  // Blue
static const Color scoutingAccent = Color(0xFF63E6A6);     // Lime
```

### Add a New Color Category

1. Open `lib/theme/app_colors.dart`
2. Add new section with clear comments
3. Add to appropriate category

```dart
// New category example
/// ─────────────────────────────────────────────────────────────
/// MY NEW SCREEN
/// Primary: X | Secondary: Y | Accent: Z
/// ─────────────────────────────────────────────────────────────
static const Color myScreenPrimary = Color(0xFF...);
static const Color myScreenSecondary = Color(0xFF...);
```

---

## 🔍 Color Reference Chart

### All Primary Colors
```
Teal          #009688  ■
Teal Dark     #00796B  ■
Teal Light    #4DB6AC  ■
Orange        #FF9800  ■
Violet        #7C3AED  ■
Blue          #2563EB  ■
Pitch Green   #0E7C66  ■
Lime          #63E6A6  ■
```

### All Semantic Colors
```
Success Green #22C55E  ■
Warning Amber #F59E0B  ■
Error Red     #FF6B6B  ■
Info Blue     #3B82F6  ■
Pending Gray  #6B7280  ■
```

---

## 📊 Chart Colors

For data visualizations and charts, use:

```dart
AppColors.chartColors  // Pre-defined color palette
// Contains: [Teal, Orange, Blue, Green, Violet, Orange]

AppColors.chartGradientStart  // Gradient start
AppColors.chartGradientEnd    // Gradient end
```

---

## 🔧 Import Template for New Screens

Use this template when creating new screens:

```dart
import 'package:flutter/material.dart';
import 'package:your_app/theme/app_colors.dart';

class YourNewScreen extends StatelessWidget {
  const YourNewScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    
    return Scaffold(
      backgroundColor: AppColors.getBackgroundColor(isDark: isDark),
      // Use AppColors.yourScreenPrimary, etc.
    );
  }
}
```

---

## ✅ Checklist for Using the Theme

- [ ] Import `app_colors.dart` in your screen file
- [ ] Use `AppColors.screenNamePrimary` for primary colors
- [ ] Use semantic colors (`AppColors.success*`, `AppColors.warning*`) for status
- [ ] Use helper methods for dark mode support
- [ ] Comment which theme your screen uses at the top
- [ ] No hardcoded color values like `Color(0xFF...)` in widgets
- [ ] Test both light and dark modes

---

## 🎓 Best Practices

1. **Always import AppColors** - Don't hardcode colors
2. **Use semantic names** - `AppColors.paymentPaid` not `Color(0xFF22C55E)`
3. **Screen comments** - Comment which colors your screen uses
4. **Consistent status colors** - Use same color for same status across app
5. **Dark mode support** - Use helper methods for mode-aware colors
6. **Avoid custom colors** - Add to AppColors if needed
7. **Team coordination** - Agree on colors before development

---

## 🚀 Next Steps

1. Update all existing screens to use `AppColors`
2. Remove all hardcoded color values
3. Add comments to each screen showing its theme
4. Test all screens in both light and dark modes
5. Create screenshot guide of theme system

---

## 📞 Questions?

Refer to:
- `THEME_USAGE_GUIDE.dart` - Code examples
- `app_colors.dart` - All color definitions
- `app_theme.dart` - Theme configuration
