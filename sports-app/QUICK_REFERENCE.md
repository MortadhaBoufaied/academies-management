# Modern Design System - Quick Reference Card

## 🚀 Getting Started

```dart
// 1. Add import
import '../../components/modern_design_system.dart';
import '../../components/app_background.dart';

// 2. Wrap with background
AppBackground(
  child: Scaffold(
    backgroundColor: Colors.transparent,
    // Your content
  ),
)
```

---

## 📦 Component Quick Reference

### ModernCard
Reusable card container with optional corner borders.
```dart
ModernCard(
  withCornerBorder: true,        // Toggle corner borders
  cornerBorderColor: Colors.teal, // Border color
  margin: EdgeInsets.all(12),    // Outer spacing
  padding: EdgeInsets.all(14),   // Inner spacing
  child: Text('Card content'),
)
```

### QuickAccessButton
Circle button for quick navigation.
```dart
QuickAccessButton(
  icon: Icons.sports_soccer,
  label: 'Training',
  color: Colors.green,
  onTap: () => Navigator.pushNamed(context, '/training'),
)
```

### SectionTitle
Styled section heading.
```dart
SectionTitle(
  title: 'Dashboard',
  color: Colors.teal,        // Optional
  fontSize: 18,              // Optional
)
```

### ExpandableSection
Section with "Show More/Less".
```dart
ExpandableSection(
  title: 'About',
  description: 'Long text here...',
  showCornerBorder: true,    // Optional
)
```

### ModernButton
Styled action button with loading support.
```dart
ModernButton(
  label: 'Submit',
  onPressed: _handleSubmit,
  backgroundColor: Colors.teal,
  isLoading: _isLoading,     // Optional
)
```

### ModernTextField
Styled text input.
```dart
ModernTextField(
  label: 'Email',
  hint: 'your@email.com',
  icon: Icons.email,         // Optional
  accentColor: Colors.teal,  // Optional
  onChanged: (value) { },    // Optional
)
```

### StatusIndicator
Status display with dot.
```dart
StatusIndicator(
  status: 'ACTIVE',
  statusColor: Colors.green,
  withDot: true,            // Optional
)
```

### ContactItem
Contact information display.
```dart
ContactItem(
  icon: Icons.email,
  text: 'contact@academy.com',
  onTap: () { },            // Optional
)
```

### InfoBadge
Small stat badge.
```dart
InfoBadge(
  text: '42 Goals',
  backgroundColor: Colors.teal,
  icon: Icons.sports_soccer,
)
```

### ModernBlurContainer
Container with backdrop blur.
```dart
ModernBlurContainer(
  blurSigma: 6,             // Optional
  backgroundColor: Colors.white,
  child: Column(
    children: [/* content */],
  ),
)
```

### DecorativeCircle
Gradient circle for backgrounds.
```dart
DecorativeCircle(
  size: 180,
  top: -40,
  right: -40,
  color: Colors.green.shade100,
  opacity: 0.35,            // Optional
)
```

---

## 🎨 Color Quick Reference

```dart
// Primary
Colors.teal                    // Main color
Colors.teal.shade700          // Darker teal

// Accents
Colors.green                   // Success
Colors.blue                    // Info
Colors.amber                   // Warning
Colors.red                     // Error

// Backgrounds
Colors.white.withOpacity(0.5)  // Cards
Colors.white.withOpacity(0.6)  // Light areas
Colors.grey.shade100.withOpacity(0.6)

// Text
Colors.grey.shade800           // Primary text
Colors.grey.shade700           // Secondary text
Colors.grey.shade600           // Tertiary text
```

---

## 📐 Spacing Cheat Sheet

```dart
// Margins (between sections)
const EdgeInsets.symmetric(horizontal: 12)           // Horizontal
const EdgeInsets.symmetric(horizontal: 12, vertical: 10) // All around

// Padding (inside cards)
const EdgeInsets.all(12)       // Tight
const EdgeInsets.all(14)       // Standard
const EdgeInsets.all(16)       // Relaxed

// SizedBox spacing
const SizedBox(height: 8)       // Minimal
const SizedBox(height: 12)      // Small
const SizedBox(height: 16)      // Medium
const SizedBox(width: 12)       // Horizontal spacing
```

---

## 🔄 Common Patterns

### Dashboard with Quick Access
```dart
ModernCard(
  withCornerBorder: true,
  child: Row(
    mainAxisAlignment: MainAxisAlignment.spaceAround,
    children: [
      QuickAccessButton(...),
      QuickAccessButton(...),
      QuickAccessButton(...),
    ],
  ),
)
```

### Profile Header
```dart
ModernCard(
  withCornerBorder: true,
  cornerBorderColor: Colors.teal,
  child: Row(
    children: [
      Avatar(...),
      Expanded(
        child: Column(
          children: [
            Text('Name', style: TextStyle(color: Colors.teal.shade700)),
            StatusIndicator(status: 'ACTIVE', statusColor: Colors.teal),
          ],
        ),
      ),
    ],
  ),
)
```

### Contact Section
```dart
ModernCard(
  child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      SectionTitle(title: 'Contact Us'),
      SizedBox(height: 12),
      ContactItem(icon: Icons.email, text: 'email@...'),
      SizedBox(height: 12),
      ContactItem(icon: Icons.phone, text: '+1 234...'),
    ],
  ),
)
```

### Form Section
```dart
ModernCard(
  child: Column(
    children: [
      SectionTitle(title: 'Update Profile'),
      SizedBox(height: 16),
      ModernTextField(label: 'Email', accentColor: Colors.teal),
      SizedBox(height: 12),
      ModernTextField(label: 'Phone', accentColor: Colors.teal),
      SizedBox(height: 20),
      ModernButton(label: 'Save', onPressed: _save),
    ],
  ),
)
```

### Metric Grid
```dart
GridView.count(
  crossAxisCount: 2,
  shrinkWrap: true,
  physics: const NeverScrollableScrollPhysics(),
  mainAxisSpacing: 12,
  crossAxisSpacing: 12,
  children: [
    _buildMetricCard('Players', '150', Icons.people, Colors.teal),
    _buildMetricCard('Divisions', '8', Icons.category, Colors.blue),
    _buildMetricCard('Activities', '42', Icons.event, Colors.amber),
    _buildMetricCard('Revenue', '\$5.2K', Icons.attach_money, Colors.green),
  ],
)
```

---

## ✅ Dos and Don'ts

### ✅ DO
- Use `ModernCard` for all card layouts
- Use teal as primary color
- Keep shadows subtle
- Use semi-transparent white backgrounds
- Add corner borders to important sections
- Use `SectionTitle` for headings
- Test light/dark mode
- Support RTL layout

### ❌ DON'T
- Don't use solid color backgrounds
- Don't mix different shadow styles
- Don't use harsh colors
- Don't create custom cards (use `ModernCard`)
- Don't ignore corner borders on main sections
- Don't use different button styles
- Don't forget to make AppBar transparent
- Don't ignore RTL text direction

---

## 🎯 Implementation Checklist

- [ ] Added imports
- [ ] Wrapped with AppBackground
- [ ] Made AppBar transparent
- [ ] Replaced Card with ModernCard
- [ ] Applied teal color scheme
- [ ] Updated typography (SectionTitle)
- [ ] Added corner borders to key sections
- [ ] Used semi-transparent backgrounds
- [ ] Tested on multiple devices
- [ ] Tested light/dark mode
- [ ] Tested RTL layout (if applicable)

---

## 🐛 Common Issues & Fixes

**Issue**: Colors look different on different devices
- **Fix**: Use standard Color values (Colors.teal, not hex)

**Issue**: Shadows appear too strong
- **Fix**: Use the predefined `BoxShadow` from examples

**Issue**: Text overflows
- **Fix**: Use `Expanded` or `SizedBox` with width constraint

**Issue**: Corner borders not showing
- **Fix**: Ensure `withCornerBorder: true` is set

**Issue**: Layout breaks in RTL
- **Fix**: Use `TextDirection` and test with Arabic text

---

## 📚 Full Documentation

For complete details, see:
- **MODERN_DESIGN_SYSTEM_GUIDE.md** - Full component docs
- **IMPLEMENTATION_EXAMPLES.md** - Real before/after examples
- **DESIGN_SYSTEM_SUMMARY.md** - Project overview

---

## 💡 Pro Tips

1. **Batch Updates**: Update multiple screens at once using same patterns
2. **Reuse Components**: Create helper methods for repeated patterns
3. **Test Early**: Check light/dark mode compatibility immediately
4. **Keep It Consistent**: Stick to spacing and color guidelines
5. **Reference Code**: Look at already-updated profile_screen.dart for examples
6. **Document Changes**: Update team docs when creating custom variants
7. **Performance**: Use `ListView.builder` for long lists of cards

---

## 🔗 File Locations

```
lib/
├── components/
│   ├── modern_design_system.dart    ← Main system
│   └── enhanced_app_background.dart ← Alternative background
├── screens/
│   ├── user/profile_screen.dart     ← Refactored example
│   ├── admin/
│   ├── MainPage/
│   └── ...

Root:
├── MODERN_DESIGN_SYSTEM_GUIDE.md    ← Full docs
├── IMPLEMENTATION_EXAMPLES.md       ← Examples
├── DESIGN_SYSTEM_SUMMARY.md         ← Overview
└── QUICK_REFERENCE.md               ← This file
```

---

**Quick Reference Card v1.0**  
**For use by development team**  
**Last updated: May 2026**

Print this and keep it handy! 📋
