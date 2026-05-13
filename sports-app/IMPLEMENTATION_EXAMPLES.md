# Modern Design System - Real Implementation Examples

## Quick Reference: Before & After

This document shows how to refactor screens to use the modern design system.

---

## Example 1: Profile Screen Refactor

### Before (Old Implementation)
```dart
import 'package:flutter/material.dart';
import '../../components/app_background.dart';
import '../../components/ui_kit.dart';

class ProfileScreen extends StatefulWidget {
  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(title: Text('Profile')),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          children: [
            SoftCard(
              margin: EdgeInsets.zero,
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Avatar(...),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('User Name'),
                        Text('Role'),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            SoftCard(
              margin: const EdgeInsets.only(top: 10),
              padding: EdgeInsets.zero,
              child: ListTile(
                leading: CircleAvatar(child: Icon(Icons.email)),
                title: Text('Email'),
                trailing: Icon(Icons.chevron_right),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
```

### After (Modern Design System)
```dart
import 'package:flutter/material.dart';
import '../../components/app_background.dart';
import '../../components/ui_kit.dart';
import '../../components/modern_design_system.dart'; // ← Added

class ProfileScreen extends StatefulWidget {
  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          title: Text('Profile'),
          backgroundColor: Colors.transparent, // ← Transparent
          elevation: 0, // ← No shadow
        ),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          children: [
            // Modern header with corner borders
            ModernCard(
              withCornerBorder: true, // ← Decorative borders
              cornerBorderColor: Colors.teal,
              margin: EdgeInsets.zero,
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Avatar(...),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'User Name',
                          style: TextStyle(
                            fontWeight: FontWeight.w900,
                            color: Colors.teal.shade700, // ← Teal color
                          ),
                        ),
                        StatusIndicator( // ← Modern component
                          status: 'VERIFIED',
                          statusColor: Colors.teal,
                          withDot: true,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            // Modern navigation links
            GestureDetector(
              onTap: () => Navigator.pushNamed(context, '/my-payments'),
              child: ModernCard(
                margin: const EdgeInsets.only(top: 10),
                padding: const EdgeInsets.all(12),
                backgroundColor: Colors.white.withOpacity(0.6),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.teal.withOpacity(0.15),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(Icons.payments, color: Colors.teal, size: 20),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        'Payments',
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 15,
                          color: Colors.black87,
                        ),
                      ),
                    ),
                    Icon(Icons.chevron_right, color: Colors.teal.shade300),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
```

**Key Changes:**
- ✅ Added corner borders to profile header
- ✅ Used `StatusIndicator` instead of plain text for role
- ✅ Replaced `SoftCard` with `ModernCard` for consistent styling
- ✅ Changed link items to use teal icon background
- ✅ Made AppBar transparent to integrate with background

---

## Example 2: Dashboard Screen Refactor

### Before
```dart
class DashboardScreen extends StatefulWidget {
  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[100],
      appBar: AppBar(title: Text('Dashboard')),
      body: ListView(
        children: [
          Container(
            margin: const EdgeInsets.all(12),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(8),
              boxShadow: [BoxShadow(blurRadius: 4)],
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                Column(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.green[100],
                        shape: BoxShape.circle,
                      ),
                      child: Icon(Icons.sports_soccer, color: Colors.green),
                    ),
                    SizedBox(height: 8),
                    Text('Exercises'),
                  ],
                ),
                // Repeat...
              ],
            ),
          ),
        ],
      ),
    );
  }
}
```

### After
```dart
import '../../components/app_background.dart';
import '../../components/modern_design_system.dart';

class DashboardScreen extends StatefulWidget {
  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBackground( // ← Modern background
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          title: Text('Dashboard'),
          backgroundColor: Colors.transparent,
          elevation: 0,
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Quick access section
              ModernCard(
                withCornerBorder: true,
                cornerBorderColor: Colors.teal,
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    QuickAccessButton( // ← Reusable component
                      icon: Icons.sports_soccer,
                      label: 'Exercises',
                      color: Colors.green,
                      onTap: () => Navigator.pushNamed(context, '/exercises'),
                    ),
                    QuickAccessButton(
                      icon: Icons.bar_chart,
                      label: 'Statistics',
                      color: Colors.blue,
                      onTap: () => Navigator.pushNamed(context, '/stats'),
                    ),
                    QuickAccessButton(
                      icon: Icons.people,
                      label: 'Players',
                      color: Colors.amber,
                      onTap: () => Navigator.pushNamed(context, '/players'),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              
              // Info section
              ExpandableSection( // ← Expandable component
                title: 'Welcome to Academy Pro',
                description: 'Track your training sessions, matches, and improve your athletic performance with the best tools.',
                showCornerBorder: true,
              ),
              const SizedBox(height: 16),
              
              // Contact section
              ModernCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    SectionTitle(title: 'Contact Us'), // ← Modern title
                    const SizedBox(height: 12),
                    ContactItem( // ← Reusable contact
                      icon: Icons.email,
                      text: 'academy@example.com',
                    ),
                    const SizedBox(height: 12),
                    ContactItem(
                      icon: Icons.phone,
                      text: '+1 234 567 8900',
                    ),
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

**Key Changes:**
- ✅ Wrapped entire screen with `AppBackground`
- ✅ Used `QuickAccessButton` for consistent quick access items
- ✅ Applied `ModernCard` with corner borders
- ✅ Used `ExpandableSection` for expandable content
- ✅ Used `SectionTitle` for headings
- ✅ Used `ContactItem` for contact information
- ✅ Made AppBar transparent

---

## Example 3: Admin Panel Refactor

### Before
```dart
class AdminDashboardScreen extends StatefulWidget {
  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Admin Dashboard')),
      body: ListView(
        children: [
          Container(
            margin: const EdgeInsets.all(12),
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('KPI Metrics', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                SizedBox(height: 8),
                Row(
                  children: [
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: Colors.green[50],
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Column(
                          children: [
                            Icon(Icons.people, color: Colors.green),
                            Text('150'),
                            Text('Players'),
                          ],
                        ),
                      ),
                    ),
                    // Repeat...
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
```

### After
```dart
import '../../components/app_background.dart';
import '../../components/modern_design_system.dart';

class AdminDashboardScreen extends StatefulWidget {
  @override
  State<AdminDashboardScreen> createState() => _AdminDashboardScreenState();
}

class _AdminDashboardScreenState extends State<AdminDashboardScreen> {
  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          title: Text('Admin Dashboard'),
          backgroundColor: Colors.transparent,
          elevation: 0,
        ),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          children: [
            // Header
            ModernCard(
              withCornerBorder: true,
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: Colors.teal.withOpacity(0.15),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(Icons.admin_panel_settings, color: Colors.teal, size: 24),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Admin Portal',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: Colors.teal.shade700,
                          ),
                        ),
                        Text(
                          'Manage all academy operations',
                          style: TextStyle(color: Colors.grey.shade600),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            
            // KPI Cards
            ModernCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SectionTitle(title: 'Key Metrics'),
                  const SizedBox(height: 12),
                  GridView.count(
                    crossAxisCount: 2,
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    mainAxisSpacing: 12,
                    crossAxisSpacing: 12,
                    childAspectRatio: 1.2,
                    children: [
                      _buildMetricTile(
                        icon: Icons.people,
                        label: 'Players',
                        value: '150',
                        color: Colors.teal,
                      ),
                      _buildMetricTile(
                        icon: Icons.category,
                        label: 'Divisions',
                        value: '8',
                        color: Colors.blue,
                      ),
                      _buildMetricTile(
                        icon: Icons.event,
                        label: 'Activities',
                        value: '42',
                        color: Colors.amber,
                      ),
                      _buildMetricTile(
                        icon: Icons.attach_money,
                        label: 'Revenue',
                        value: '\$5.2K',
                        color: Colors.green,
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            
            // Action buttons
            ModernCard(
              child: Row(
                children: [
                  Expanded(
                    child: ModernButton(
                      label: 'Data Management',
                      backgroundColor: Colors.teal,
                      onPressed: () => Navigator.pushNamed(context, '/data-management'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ModernButton(
                      label: 'Statistics',
                      backgroundColor: Colors.blue,
                      onPressed: () => Navigator.pushNamed(context, '/statistics'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMetricTile({
    required IconData icon,
    required String label,
    required String value,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.2)),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, color: color, size: 28),
          const SizedBox(height: 8),
          Text(
            value,
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w900,
              color: Colors.black87,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            label,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey.shade600,
            ),
          ),
        ],
      ),
    );
  }
}
```

**Key Changes:**
- ✅ Applied `AppBackground` with decorative circles
- ✅ Used `ModernCard` with corner borders for sections
- ✅ Replaced metric boxes with modern styling
- ✅ Used `ModernButton` for action buttons
- ✅ Made AppBar transparent

---

## Example 4: Form/Input Screen

### Before
```dart
class RegisterScreen extends StatefulWidget {
  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Register')),
      body: ListView(
        children: [
          TextField(
            decoration: InputDecoration(
              labelText: 'Email',
              hintText: 'Enter email',
              border: OutlineInputBorder(),
            ),
          ),
          TextField(
            decoration: InputDecoration(
              labelText: 'Password',
              hintText: 'Enter password',
              border: OutlineInputBorder(),
            ),
            obscureText: true,
          ),
          ElevatedButton(
            onPressed: () {},
            child: Text('Register'),
          ),
        ],
      ),
    );
  }
}
```

### After
```dart
import '../../components/modern_design_system.dart';
import '../../components/app_background.dart';

class RegisterScreen extends StatefulWidget {
  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _emailCtl = TextEditingController();
  final _passwordCtl = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          title: Text('Register'),
          backgroundColor: Colors.transparent,
          elevation: 0,
        ),
        body: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 28),
          child: ModernCard(
            withCornerBorder: true,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Create Account',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                    color: Colors.teal.shade700,
                  ),
                ),
                const SizedBox(height: 16),
                ModernTextField(
                  label: 'Email Address',
                  hint: 'example@gmail.com',
                  icon: Icons.email,
                  controller: _emailCtl,
                  accentColor: Colors.teal,
                ),
                const SizedBox(height: 16),
                ModernTextField(
                  label: 'Password',
                  hint: 'Secure password',
                  icon: Icons.lock,
                  controller: _passwordCtl,
                  obscureText: true,
                  accentColor: Colors.teal,
                ),
                const SizedBox(height: 20),
                ModernButton(
                  label: 'Create Account',
                  onPressed: () { /* Handle registration */ },
                  backgroundColor: Colors.teal,
                ),
                const SizedBox(height: 12),
                Center(
                  child: Text(
                    'Already have an account?',
                    style: TextStyle(color: Colors.grey.shade700),
                  ),
                ),
                Center(
                  child: TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: Text(
                      'Sign in here',
                      style: TextStyle(
                        color: Colors.teal,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _emailCtl.dispose();
    _passwordCtl.dispose();
    super.dispose();
  }
}
```

**Key Changes:**
- ✅ Used `ModernTextField` for consistent input styling
- ✅ Used `ModernButton` for main action
- ✅ Applied `ModernCard` with corner borders
- ✅ Used AppBackground for modern look
- ✅ Improved spacing and typography

---

## Quick Integration Checklist

When migrating a screen:

1. **Add Imports**
   ```dart
   import '../../components/modern_design_system.dart';
   import '../../components/app_background.dart';
   ```

2. **Wrap with Background**
   ```dart
   AppBackground(
     child: Scaffold(
       backgroundColor: Colors.transparent,
       // ...
     ),
   )
   ```

3. **Update AppBar**
   ```dart
   AppBar(
     backgroundColor: Colors.transparent,
     elevation: 0,
   )
   ```

4. **Replace Cards**
   - Old: `Container` → New: `ModernCard`
   - Old: `SoftCard` → New: `ModernCard` (with updated styling)

5. **Replace Buttons**
   - Old: `ElevatedButton` → New: `ModernButton`
   - Old: `TextField` → New: `ModernTextField`

6. **Add Section Titles**
   - Old: Plain `Text` → New: `SectionTitle`

7. **Update Icons/Links**
   - Old: `ListTile` → New: `ContactItem` or custom with `ModernCard`

8. **Test**
   - ✓ Light/dark mode
   - ✓ RTL layout (if applicable)
   - ✓ Responsive design
   - ✓ Color consistency

---

## Color References

```dart
// Primary teal
Colors.teal           // Default
Colors.teal.shade700  // For titles/headers

// Accent colors
Colors.green          // For success/exercises
Colors.blue           // For info/stats
Colors.amber          // For warnings/caution
Colors.red            // For errors/alerts

// Backgrounds
Colors.white.withOpacity(0.5)    // Card backgrounds
Colors.white.withOpacity(0.6)    // Light backgrounds
Colors.grey.shade100.withOpacity(0.6) // Alternative light

// Text colors
Colors.grey.shade800  // Primary text
Colors.grey.shade700  // Secondary text
Colors.grey.shade600  // Tertiary text
```

---

This guide covers 90% of refactoring scenarios. Reference the component documentation for advanced usage.
