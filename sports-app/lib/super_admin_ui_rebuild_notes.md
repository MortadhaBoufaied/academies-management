# Super Admin UI Rebuild - Duplicate Links Removed

The super-admin Home page keeps Academies, Webhooks, and Academy Payments as compact Quick access chips only.
They were removed from the large grid below to avoid repeated links and reduce vertical space.

## Current layout
- Quick access: Academies, Webhooks, Academy Payments.
- Platform operations: App Data only, because the other operation links are already quick actions.
- Configuration & Governance: Sports, Sport Categories, Themes, Global Chatbot.
- Support & Platform Settings: Contact Admins, Platform Settings.

Other roles remain unchanged.

## Dashboard moved to bottom app bar
The existing `AdminDashboardScreen` is now available as a dedicated super-admin bottom navigation tab named **Dashboard**. The duplicate `dashboard` module was removed from the in-page module navigation list/rail.


