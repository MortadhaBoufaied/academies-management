# Modern Design System - Implementation Guide

## Overview
This guide explains how to implement the modern design system inspired by the HomeSection page throughout the Sports Academy app. The design system provides cohesive, modern UI components with a teal color scheme, gradient overlays, and decorative elements.

## Key Design Principles

### 1. **Color Scheme**
- **Primary**: Teal (`Colors.teal` / `0xFF14B8A6`)
- **Accent Colors**: Green, Blue, Amber, Emerald
- **Background**: Soft white with opacity (`Colors.white.withOpacity(0.5-0.6)`)
- **Text**: Dark gray (`Colors.grey.shade700-800`)

### 2. **Layout Elements**
- Rounded corners: `12-16px` border radius
- Shadow effect: Soft shadows with `blurRadius: 6-8`
- Decorative circles: Gradient radial circles (green and blue)
- Background image: `assets/background-image.png`

### 3. **Typography**
- Headings: `fontWeight: FontWeight.bold`, `fontSize: 18-22`
- Labels: `fontWeight: FontWeight.w600`, `fontSize: 13-15`
- Body text: `fontSize: 15-16`, `height: 1.5`

---

## Core Components

### **1. ModernCard**
A reusable card component with optional corner borders and customizable styling.

```dart
ModernCard(
  withCornerBorder: true,
  cornerBorderColor: Colors.teal,
  margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
  padding: const EdgeInsets.all(14),
  child: Column(
    children: [
      Text('Your content here'),
    ],
  ),
)
```

**Options:**
- `withCornerBorder`: Shows decorative corner borders
- `cornerBorderColor`: Color of the corner borders
- `backgroundColor`: Override the default semi-transparent white
- `borderRadius`: Customize border radius (default: 12)

---

### **2. SectionTitle**
Styled section titles for organizing content.

```dart
SectionTitle(
  title: 'أفضل اللاعبين',  // Arabic text example
  color: Colors.teal,
  fontSize: 18,
)
```

---

### **3. QuickAccessButton**
Circle button with icon for quick navigation.

```dart
QuickAccessButton(
  icon: Icons.sports_soccer,
  label: 'تمارين',
  color: Colors.green,
  onTap: () => Navigator.pushNamed(context, '/plan'),
)
```

---

### **4. ExpandableSection**
Section with "Show More/Less" functionality.

```dart
ExpandableSection(
  title: 'Football Academy Pro',
  description: 'تابع تدريباتك ومبارياتك وارتقِ بأدائك الرياضي...',
  titleIcon: Image.asset("assets/icons/kick-ball.png", width: 50),
  maxLinesCollapsed: 3,
  showCornerBorder: true,
)
```

---

### **5. ContactItem**
Reusable contact information display.

```dart
ContactItem(
  icon: Icons.email,
  text: 'academy@example.com',
  onTap: () { /* Handle tap */ },
)
```

---

### **6. ModernButton**
Styled action button with loading state support.

```dart
ModernButton(
  label: 'Click Me',
  onPressed: () { /* Action */ },
  backgroundColor: Colors.teal,
  textColor: Colors.white,
)
```

---

### **7. InfoBadge**
Small information badges for displaying stats/metrics.

```dart
InfoBadge(
  text: '42 ⚽',
  backgroundColor: Colors.teal,
  icon: Icons.sports_soccer,
)
```

---

### **8. StatusIndicator**
Show status with optional dot indicator.

```dart
StatusIndicator(
  status: 'ACTIVE',
  statusColor: Colors.green,
  withDot: true,
)
```

---

### **9. ModernTextField**
Styled text input with focus effects.

```dart
ModernTextField(
  label: 'Email',
  hint: 'Enter your email',
  accentColor: Colors.teal,
  onChanged: (value) { /* Handle change */ },
)
```

---

### **10. DecorativeCircle**
Reusable decorative background circle.

```dart
DecorativeCircle(
  size: 180,
  top: -40,
  right: -40,
  color: Colors.green.shade100,
  opacity: 0.35,
)
```

---

### **11. ModernBlurContainer**
Container with backdrop blur effect (for overlay cards).

```dart
ModernBlurContainer(
  child: Column(
    children: [/* Your content */],
  ),
  blurSigma: 6,
)
```

---

### **12. EnhancedAppBackground**
Enhanced background with decorative circles and gradient.

```dart
EnhancedAppBackground(
  includeDecorativeCircles: true,
  includeStadiumLines: true,
  child: Scaffold(
    body: /* Your content */,
  ),
)
```

---

## Implementation Patterns

### Pattern 1: Dashboard Layout
```dart
ModernCard(
  withCornerBorder: true,
  child: Column(
    children: [
      SectionTitle(title: 'Dashboard'),
      const SizedBox(height: 12),
      Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          QuickAccessButton(...),
          QuickAccessButton(...),
          QuickAccessButton(...),
        ],
      ),
    ],
  ),
)
```

### Pattern 2: User Profile Section
```dart
ModernCard(
  withCornerBorder: true,
  cornerBorderColor: Colors.teal,
  child: Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Row(
        children: [
          Avatar(...),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('User Name'),
              StatusIndicator(status: 'VERIFIED'),
            ],
          ),
        ],
      ),
    ],
  ),
)
```

### Pattern 3: Settings Links
```dart
Column(
  children: [
    ContactItem(icon: Icons.email, text: 'Email...'),
    const SizedBox(height: 12),
    ContactItem(icon: Icons.phone, text: 'Phone...'),
  ],
)
```

---

## RTL (Right-to-Left) Support

The app supports Arabic text. Always wrap RTL layouts properly:

```dart
Directionality(
  textDirection: TextDirection.rtl,
  child: ModernCard(
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('نص عربي'),
      ],
    ),
  ),
)
```

---

## Color Palette

| Purpose | Color | Hex |
|---------|-------|-----|
| Primary (Teal) | `Colors.teal` | `#14B8A6` |
| Green Accent | `Colors.green.shade100` | `#86EFAC` |
| Blue Accent | `Colors.blue.shade100` | `#BFDBFE` |
| Amber Accent | `Colors.amber` | `#F59E0B` |
| Background Light | `Colors.white.withOpacity(0.5)` | `#FFFFFF80` |
| Text Primary | `Colors.grey.shade800` | `#1F2937` |
| Text Secondary | `Colors.grey.shade700` | `#374151` |

---

## Spacing Guidelines

- **Card Margins**: `12px` or `EdgeInsets.symmetric(horizontal: 12)`
- **Card Padding**: `14-16px`
- **Section Spacing**: `10-12px`
- **Element Spacing**: `8px` (small), `12px` (medium), `16px` (large)

---

## Shadow Guidelines

```dart
// Soft shadow
BoxShadow(
  color: Colors.black12,
  blurRadius: 6,
  offset: const Offset(0, 3),
)

// Medium shadow
BoxShadow(
  color: Colors.black.withOpacity(0.1),
  blurRadius: 12,
  offset: const Offset(0, 6),
)
```

---

## Dos and Don'ts

### ✅ Do:
- Use `ModernCard` for all card layouts
- Maintain consistent teal color scheme
- Use semi-transparent white backgrounds
- Include decorative circles in full-page layouts
- Add corner borders to important sections
- Use `SectionTitle` for headings

### ❌ Don't:
- Use harsh colors or strong opacity
- Mix different card styles in the same screen
- Add shadows to every element
- Use solid backgrounds instead of semi-transparent
- Ignore RTL text direction
- Create custom cards when `ModernCard` exists

---

## Migration Checklist

When updating a screen to use the modern design system:

- [ ] Add imports: `import 'path/to/modern_design_system.dart';`
- [ ] Replace all `Card` widgets with `ModernCard`
- [ ] Update color scheme to use teal
- [ ] Replace section titles with `SectionTitle`
- [ ] Update backgrounds to use `AppBackground` or `EnhancedAppBackground`
- [ ] Apply corner borders to key sections
- [ ] Add decorative circles if it's a main page
- [ ] Update button styles to use `ModernButton`
- [ ] Test RTL layout
- [ ] Verify all shadow effects are subtle

---

## Examples

### Complete Dashboard Screen

```dart
class DashboardScreen extends StatefulWidget {
  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(title: const Text('Dashboard')),
        body: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Quick Access Section
              ModernCard(
                withCornerBorder: true,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    QuickAccessButton(
                      icon: Icons.sports_soccer,
                      label: 'تمارين',
                      color: Colors.green,
                      onTap: () => Navigator.pushNamed(context, '/plan'),
                    ),
                    QuickAccessButton(
                      icon: Icons.bar_chart,
                      label: 'إحصائيات',
                      color: Colors.blue,
                      onTap: () => Navigator.pushNamed(context, '/stats'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              
              // Info Section
              ExpandableSection(
                title: 'Welcome',
                description: 'Your application description...',
                showCornerBorder: true,
              ),
              const SizedBox(height: 16),
              
              // Contact Section
              ModernCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const SectionTitle(title: 'تواصل معنا'),
                    const SizedBox(height: 12),
                    ContactItem(icon: Icons.email, text: 'email@example.com'),
                    const SizedBox(height: 12),
                    ContactItem(icon: Icons.phone, text: '+1 234 567 8900'),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
```

---

## Performance Tips

1. **Lazy Loading**: Use `ListView.builder` for large lists of cards
2. **Hero Animations**: Use `Hero` widget for smooth transitions between screens
3. **Decorative Circles**: Consider disabling on low-end devices:
   ```dart
   EnhancedAppBackground(
     includeDecorativeCircles: Device.isLowEnd ? false : true,
   )
   ```
4. **Image Caching**: Use `CachedNetworkImage` for background images
5. **State Management**: Minimize rebuilds by using keys and separating state

---

## Support

For issues or questions about the design system:
1. Check this guide first
2. Review the `modern_design_system.dart` file
3. Look at existing implementations for examples
4. Reference the HomeSection widget for complex layouts

---

**Version**: 1.0  
**Last Updated**: May 2026  
**Maintained By**: Development Team
