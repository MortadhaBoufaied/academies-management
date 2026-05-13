# Multi-Sport Academy Platform Audit Report

Generated from the uploaded aggregated Flutter and Spring Boot/Admin source files.

> Scope note: this is an automated codebase reconstruction + static audit pass. It does not execute the Flutter app or the Spring Boot backend. Items marked as “verify” need runtime/manual confirmation.

## A. Executive Summary

The project is already a large multi-role academy platform. The Flutter codebase contains role-aware navigation, dashboards, profiles, data management, chat, notifications, payments, scouting, settings, and a modern UI component set. The backend/admin source contains a broader Spring Boot platform with academy, sport, sport category, theme, webhook, subscription, payment, notification campaign, chatbot, chat, scouting/ranking, reporting, and CRUD services.

The biggest improvement area is **mobile/admin parity**: several backend/admin capabilities appear deeper than their Flutter exposure. The second biggest improvement area is **multi-sport consistency**: the source still contains football/soccer terminology and sport-specific metrics such as goals/assists/matches that may not apply to swimming, martial arts, gymnastics, athletics, tennis, and other sports.

## B. Project Architecture Summary

### Reconstructed Flutter source
- Files reconstructed: **194**
- Dart screens: **89**
- Dart controllers: **21**
- Dart services: **26**
- Dart models: **31**
- Dart components: **15**
- Routes detected in `main.dart`: **0**

### Reconstructed backend/admin source
- Files reconstructed: **446**
- Java files: **288**
- Controller-like Java files: **73**
- Service-like Java files: **51**
- Repository-like Java files: **29**
- Model/entity-like Java files: **30**
- HTML templates/resources: **148**

## C. Multi-Sport Compatibility Audit

### Term scan — Flutter
| Term | Flutter count |
| --- | --- |
| academy | 187 |
| sport | 135 |
| match/matches | 126 |
| goal/goals | 93 |
| assist/assists | 76 |
| football | 51 |
| soccer | 35 |

### Term scan — Backend/Admin
| Term | Backend/Admin count |
| --- | --- |
| academy | 1239 |
| football | 1217 |
| sport | 702 |
| match/matches | 150 |
| assist/assists | 138 |
| goal/goals | 129 |
| soccer | 7 |

### Hardcoded sport/football hotspots
| Term | File | Count |
| --- | --- | --- |
| football | lib/screens/DataManagement/tabs/players/footballers_screen.dart | 8 |
| football | lib/screens/DataManagement/tabs/players/footballer_details_screen.dart | 6 |
| football | lib/screens/DataManagement/tabs/players/PlayerForm/components/football_info_step.dart | 6 |
| football | lib/screens/DataManagement/tabs/divisions/division_detail_screen.dart | 4 |
| football | lib/screens/DataManagement/tabs/players/PlayerForm/player_form.dart | 3 |
| football | lib/screens/user/profile_router_screen.dart | 2 |
| soccer | lib/screens/user/staff_profile_screen.dart | 5 |
| soccer | lib/screens/MainPage/pages/home_screen.dart | 4 |
| soccer | lib/screens/StatisticsScreen.dart | 3 |
| soccer | lib/screens/DataManagement/tabs/unassigned_tab.dart | 3 |
| soccer | lib/screens/user/user_matches_screen.dart | 2 |
| soccer | lib/screens/DataManagement/tabs/players/footballer_details_screen.dart | 2 |
| goals | lib/screens/DataManagement/tabs/players/footballer_details_screen.dart | 21 |
| goals | lib/screens/AdvancedSearchScreen.dart | 10 |
| goals | lib/models/player.dart | 10 |
| goals | lib/services/export_service.dart | 8 |
| goals | lib/services/player_ranking_service.dart | 7 |
| goals | lib/screens/StatisticsScreen.dart | 6 |
| assists | lib/screens/DataManagement/tabs/players/footballer_details_screen.dart | 21 |
| assists | lib/models/player.dart | 10 |
| assists | lib/services/export_service.dart | 8 |
| assists | lib/services/player_ranking_service.dart | 6 |
| assists | lib/controllers/StatisticsController.dart | 6 |
| assists | lib/controllers/PlayerController.dart | 6 |
| football | src/test.disabled/java/com/footballacademy/services/sport/SportServiceTest.java | 33 |
| football | src/main/java/com/footballacademy/services/sport/SportDataInitializer.java | 29 |
| football | src/main/java/com/footballacademy/controllers_rest/player/PlayerController.java | 22 |
| football | src/main/java/com/footballacademy/controllers_rest/notification/AdminNotificationController.java | 20 |
| football | src/main/java/com/footballacademy/services/NotificationService.java | 18 |
| football | src/main/java/com/footballacademy/controllers_rest/superadmin/SuperAdminMobileController.java | 18 |
| goals | src/main/java/com/footballacademy/services/sport/SportDataInitializer.java | 29 |
| goals | src/main/java/com/footballacademy/controllers_mvc/AdminPlayersManageController.java | 18 |
| goals | src/main/java/com/footballacademy/DTO/PlayerDTO.java | 9 |
| goals | src/main/java/com/footballacademy/services/player/PlayerStatsService.java | 7 |
| goals | src/main/resources/pages/players/form.html | 6 |
| goals | src/main/resources/pages/modules/data-management/players/form.html | 6 |
| assists | src/main/java/com/footballacademy/services/sport/SportDataInitializer.java | 36 |
| assists | src/main/java/com/footballacademy/controllers_mvc/AdminPlayersManageController.java | 18 |
| assists | src/main/java/com/footballacademy/DTO/PlayerDTO.java | 9 |
| assists | src/main/java/com/footballacademy/services/player/PlayerStatsService.java | 7 |

### Key multi-sport risk
- Backend/admin appears to support sports, sport categories, sport themes, and academy sport configuration.
- Flutter contains sport-related routes and screens, but several UI areas still use football-oriented naming, icons, player profiles, and statistics.
- Any UI that displays goals, assists, footballer, soccer icons, or football academy branding should become sport-aware.

Recommended multi-sport strategy:
1. Introduce a central `SportDescriptor` / `SportUiConfig` layer in Flutter.
2. Map each sport to labels, icons, stat names, profile sections, and competition wording.
3. Keep backend package names stable, but move displayed product text into configurable properties/localization.
4. Replace generic “Player” in UI with “Athlete” where the current sport is not a team ball sport.
5. Make ranking/stat cards dynamic by sport category.

## D. Backend Capability Map

| Capability | Backend/Admin detected | Flutter detected | Status |
| --- | --- | --- | --- |
| users | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| academies | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| sports | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| divisions | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| players/athletes | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| trainers/coaches | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| parents | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| activities/trainings | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| matches/competitions | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| payments/subscriptions | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| notifications/campaigns | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| chat/conversations | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| chatbot knowledge | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| webhooks | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| themes/branding | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| reports/statistics | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| files/uploads | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |
| scouting/ranking | Yes | Yes | BACKEND EXISTS, FLUTTER PARTIAL/VERIFY UI DEPTH |

## E. Backend Endpoint/Controller Hotspots

| Approx. mapping count | Backend Java file |
| --- | --- |
| 26 | main/java/com/footballacademy/controllers_mvc/SuperAdminPortalManageController.java |
| 22 | main/java/com/footballacademy/controllers_mvc/SuperAdminPortalController.java |
| 15 | main/java/com/footballacademy/controllers_mvc/AdminPagesController.java |
| 14 | main/java/com/footballacademy/controllers_rest/admin/AdminRoleCrudController.java |
| 13 | main/java/com/footballacademy/controllers_rest/webhook/WebhookController.java |
| 12 | main/java/com/footballacademy/controllers_rest/player/PlayerController.java |
| 11 | main/java/com/footballacademy/controllers_rest/trainer/TrainerController.java |
| 11 | main/java/com/footballacademy/controllers_rest/sport/SportController.java |
| 11 | main/java/com/footballacademy/controllers_rest/chatbot/ChatbotDataController.java |
| 10 | main/java/com/footballacademy/controllers_rest/player/PlayerStatsController.java |
| 10 | main/java/com/footballacademy/controllers_rest/payment/PaymentController.java |
| 10 | main/java/com/footballacademy/controllers_rest/parent/ParentController.java |
| 10 | main/java/com/footballacademy/controllers_rest/notification/NotificationController.java |
| 10 | main/java/com/footballacademy/controllers_rest/division/DivisionController.java |
| 10 | main/java/com/footballacademy/controllers_rest/admin/AdminController.java |
| 10 | main/java/com/footballacademy/controllers_rest/activity/ActivityController.java |
| 9 | main/java/com/footballacademy/controllers_rest/notification/AdvancedNotificationController.java |
| 9 | main/java/com/footballacademy/controllers_rest/activity/MatchController.java |
| 9 | main/java/com/footballacademy/controllers_mvc/PlayersViewController.java |
| 8 | main/java/com/footballacademy/tracing/TracingController.java |
| 8 | main/java/com/footballacademy/controllers_rest/superadmin/SuperAdminMobileController.java |
| 8 | main/java/com/footballacademy/controllers_rest/scouting/ScoutingController.java |
| 8 | main/java/com/footballacademy/controllers_rest/activity/TrainingController.java |
| 8 | main/java/com/footballacademy/controllers_mvc/UsersViewController.java |
| 7 | main/java/com/footballacademy/controllers_rest/superadmin/AcademyManagementController.java |
| 7 | main/java/com/footballacademy/controllers_rest/sport/SportStatisticController.java |
| 7 | main/java/com/footballacademy/controllers_rest/sport/SportPositionController.java |
| 7 | main/java/com/footballacademy/controllers_rest/payment/OnlinePaymentController.java |
| 7 | main/java/com/footballacademy/controllers_rest/notification/AdminNotificationController.java |
| 7 | main/java/com/footballacademy/controllers_rest/chat/ConversationController.java |
| 7 | main/java/com/footballacademy/controllers_rest/auth/AuthController.java |
| 7 | main/java/com/footballacademy/controllers_rest/admin/AdvancedAdminDashboardController.java |
| 7 | main/java/com/footballacademy/controllers_rest/academy/AcademyInfoController.java |
| 6 | main/java/com/footballacademy/controllers_rest/sport/SportCategoryController.java |
| 6 | main/java/com/footballacademy/controllers_mvc/ActivitiesViewController.java |
| 5 | main/java/com/footballacademy/controllers_rest/theme/SportThemeController.java |
| 5 | main/java/com/footballacademy/controllers_rest/admin/AdminAcademyProfileController.java |
| 5 | main/java/com/footballacademy/controllers_rest/MobileTestController.java |
| 5 | main/java/com/footballacademy/controllers_mvc/TrainersViewController.java |
| 5 | main/java/com/footballacademy/controllers_mvc/ParentsViewController.java |
| 5 | main/java/com/footballacademy/controllers_mvc/AdminUsersManageController.java |
| 4 | main/java/com/footballacademy/controllers_rest/player/PlayerRankingController.java |
| 4 | main/java/com/footballacademy/controllers_rest/mobile/MobileConfigController.java |
| 4 | main/java/com/footballacademy/controllers_rest/file/FileUploadController.java |
| 4 | main/java/com/footballacademy/controllers_rest/dashboard/DashboardController.java |
| 4 | main/java/com/footballacademy/controllers_rest/auth/ProfileController.java |
| 4 | main/java/com/footballacademy/controllers_rest/HealthCheckController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminTrainersManageController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminPlayersManageController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminParentsManageController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminLoginController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminDivisionsManageController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminChatbotViewController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminActivitiesManageController.java |
| 4 | main/java/com/footballacademy/controllers_mvc/AdminAcademyInfoManageController.java |
| 3 | main/java/com/footballacademy/controllers_rest/division/DivisionDTOController.java |
| 3 | main/java/com/footballacademy/controllers_rest/activity/CalendarController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/DashboardViewController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/AdminSubscriptionManageController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/AdminPaymentsManageController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/AdminPagesDynamicController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/AdminNotificationsManageController.java |
| 3 | main/java/com/footballacademy/controllers_mvc/AdminModulesController.java |
| 2 | main/java/com/footballacademy/controllers_rest/search/SearchController.java |
| 2 | main/java/com/footballacademy/controllers_rest/chat/ChatSearchController.java |
| 2 | main/java/com/footballacademy/controllers_rest/chat/ChatContactsController.java |
| 2 | main/java/com/footballacademy/controllers_mvc/ProfileManageController.java |
| 2 | main/java/com/footballacademy/controllers_mvc/AdminSubscriptionViewController.java |
| 2 | main/java/com/footballacademy/controllers_mvc/AdminSettingsManageController.java |
| 2 | main/java/com/footballacademy/controllers_mvc/AdminChatViewController.java |
| 1 | main/java/com/footballacademy/controllers_mvc/StaticResourceController.java |
| 1 | main/java/com/footballacademy/controllers_mvc/AdminTemplateAccessController.java |

## F. Flutter Routing Map

| Flutter route |
| --- |

## G. Issue List

| ID | Priority | Category | File | Role | Sport | Current | Expected | Fix | Complexity |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ISS-001 | P1 | FOOTBALL-HARDCODED | lib/screens/DataManagement/tabs/players/footballers_screen.dart | All roles | All non-football sports | Contains 8 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-002 | P1 | FOOTBALL-HARDCODED | lib/screens/DataManagement/tabs/players/footballer_details_screen.dart | All roles | All non-football sports | Contains 6 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-003 | P1 | FOOTBALL-HARDCODED | lib/screens/DataManagement/tabs/players/PlayerForm/components/football_info_step.dart | All roles | All non-football sports | Contains 6 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-004 | P1 | FOOTBALL-HARDCODED | lib/screens/DataManagement/tabs/divisions/division_detail_screen.dart | All roles | All non-football sports | Contains 4 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-005 | P1 | FOOTBALL-HARDCODED | lib/screens/DataManagement/tabs/players/PlayerForm/player_form.dart | All roles | All non-football sports | Contains 3 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-006 | P1 | FOOTBALL-HARDCODED | lib/screens/user/profile_router_screen.dart | All roles | All non-football sports | Contains 2 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-007 | P1 | FOOTBALL-HARDCODED | lib/screens/user/account_profile_router_screen.dart | All roles | All non-football sports | Contains 2 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-008 | P1 | FOOTBALL-HARDCODED | lib/screens/search_players_screen.dart | All roles | All non-football sports | Contains 2 football references. | UI/domain text should be sport-aware or academy-theme driven. | Move labels/icons/stat names to sport theme/config/localization and use sport-specific metadata. | Medium |
| ISS-009 | P2 | SPORT ICON HARD-CODED | lib/screens/user/staff_profile_screen.dart | All roles | All non-football sports | Contains 5 soccer references/icons. | Sport icon should come from current sport/theme. | Use sport icon mapping or SportTheme iconStyle instead of a soccer icon everywhere. | Low |
| ISS-010 | P2 | SPORT ICON HARD-CODED | lib/screens/MainPage/pages/home_screen.dart | All roles | All non-football sports | Contains 4 soccer references/icons. | Sport icon should come from current sport/theme. | Use sport icon mapping or SportTheme iconStyle instead of a soccer icon everywhere. | Low |
| ISS-011 | P2 | SPORT ICON HARD-CODED | lib/screens/StatisticsScreen.dart | All roles | All non-football sports | Contains 3 soccer references/icons. | Sport icon should come from current sport/theme. | Use sport icon mapping or SportTheme iconStyle instead of a soccer icon everywhere. | Low |
| ISS-012 | P2 | SPORT ICON HARD-CODED | lib/screens/DataManagement/tabs/unassigned_tab.dart | All roles | All non-football sports | Contains 3 soccer references/icons. | Sport icon should come from current sport/theme. | Use sport icon mapping or SportTheme iconStyle instead of a soccer icon everywhere. | Low |
| ISS-013 | P2 | SPORT ICON HARD-CODED | lib/screens/user/user_matches_screen.dart | All roles | All non-football sports | Contains 2 soccer references/icons. | Sport icon should come from current sport/theme. | Use sport icon mapping or SportTheme iconStyle instead of a soccer icon everywhere. | Low |
| ISS-014 | P2 | BACKEND TERMINOLOGY | src/test.disabled/java/com/footballacademy/services/sport/SportServiceTest.java | Admin/Super Admin | All sports | Contains 33 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-015 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/services/sport/SportDataInitializer.java | Admin/Super Admin | All sports | Contains 29 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-016 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/controllers_rest/player/PlayerController.java | Admin/Super Admin | All sports | Contains 22 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-017 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/controllers_rest/notification/AdminNotificationController.java | Admin/Super Admin | All sports | Contains 20 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-018 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/services/NotificationService.java | Admin/Super Admin | All sports | Contains 18 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-019 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/controllers_rest/superadmin/SuperAdminMobileController.java | Admin/Super Admin | All sports | Contains 18 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-020 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/controllers_mvc/SuperAdminPortalController.java | Admin/Super Admin | All sports | Contains 15 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-021 | P2 | BACKEND TERMINOLOGY | src/main/java/com/footballacademy/controllers_mvc/DashboardViewController.java | Admin/Super Admin | All sports | Contains 15 football references. | Backend may keep package names, but displayed/product text should be multi-sport. | Avoid renaming Java packages blindly; refactor displayed text/config defaults gradually. | High |
| ISS-022 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | sports exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for sports. | Medium |
| ISS-023 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | payments/subscriptions exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for payments/subscriptions. | Medium |
| ISS-024 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | notifications/campaigns exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for notifications/campaigns. | Medium |
| ISS-025 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | chatbot knowledge exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for chatbot knowledge. | Medium |
| ISS-026 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | webhooks exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for webhooks. | Medium |
| ISS-027 | P2 | VERIFY PARITY DEPTH | project-wide | Admin/Super Admin | All sports | themes/branding exists on both sides by keyword scan, but depth may differ. | Flutter should match backend/admin web CRUD depth for key operations. | Perform manual endpoint-to-screen mapping and add missing forms/actions for themes/branding. | Medium |

## H. Page-by-Page Flutter Audit Guidance

The static scan detected many screens. Use the following page-level checklist for every screen under `lib/screens`:

- Does the page use real backend data or static placeholders?
- Does the page have loading, error, empty, and refresh states?
- Does the page adapt to Super Admin, Admin, Trainer/Coach, Parent, Player/Athlete, and Scouter?
- Does the page adapt to current sport and academy theme?
- Does the page expose all CRUD actions supported by backend/admin web for its entity?
- Does the page support search/filter/sort/pagination where backend supports it?
- Does the page use neutral multi-sport language?
- Does the page avoid football-only stats unless current sport is football?
- Does the page have confirmation dialogs for destructive actions?
- Does the page have success/error feedback?

## I. Missing Forms / Buttons / Services — Main Findings

High-priority areas to manually verify and complete:

1. **Sport management in Flutter** — backend/admin has sport and sport-category concepts; Flutter must expose complete Super Admin workflows.
2. **Theme management in Flutter** — sport/academy theme exists; verify mobile can manage or at least fully consume it.
3. **Webhook management in Flutter** — backend/admin appears stronger than mobile.
4. **Notification campaigns** — basic notification sending may exist, but advanced targeting/campaign analytics likely need mobile parity.
5. **Chatbot knowledge CRUD** — backend/admin has knowledge/data tooling; Flutter likely focuses on user chatbot conversation.
6. **Payment/subscription workflows** — verify admin/super-admin payment/subscription management parity and mobile feedback states.
7. **Multi-sport athlete profile** — footballer/player detail screens should become dynamic athlete profiles.
8. **Scouting/ranking metrics** — goals/assists/rating logic should be configurable per sport.

## J. Security / Validation / Permission Audit — Static Checklist

Verify all sensitive paths with runtime tests:

- Super Admin routes cannot be accessed by Admin/Trainer/Parent/Player/Scouter.
- Admin routes are academy-scoped.
- Trainer routes are division/team scoped.
- Parent routes only show linked children/athletes.
- Player/Athlete routes only show own data unless authorized.
- Payment endpoints require authorization and do not expose other academies.
- Chat conversations validate participant access.
- Notification targeting cannot be abused by non-admin roles.
- File upload validates size/type and access rights.
- Flutter hides actions that backend would reject.
- Backend still enforces permissions even if Flutter hides UI.

## K. Roadmap

### Phase 1 — Fix core workflow gaps
- Run `flutter analyze`, backend tests, and route smoke tests.
- Fix broken imports/routes/null crashes.
- Verify login/session restoration across all roles.
- Verify core CRUD for users, athletes, coaches, parents, divisions, activities, payments.

### Phase 2 — Multi-sport refactor
- Add sport-aware labels/icons/stats in Flutter.
- Replace hardcoded football UI text where user-facing.
- Make athlete profile dynamic per sport.
- Make scouting/ranking metrics sport-configurable.

### Phase 3 — Admin/mobile parity
- Map every admin web page to a Flutter route/screen.
- Add missing forms/buttons/actions.
- Add missing search/filter/sort/batch actions where backend supports them.

### Phase 4 — Role dashboards and profiles
- Improve Super Admin platform dashboard.
- Improve Admin academy dashboard.
- Improve Trainer/Coach team dashboard.
- Improve Parent family dashboard.
- Improve Athlete performance dashboard.
- Improve Scouter discovery dashboard.

### Phase 5 — Advanced platform features
- Add/complete notification campaigns.
- Add/complete chatbot knowledge management.
- Add/complete webhooks.
- Add/complete theme/branding management.
- Add/complete subscriptions/offers/payment history.

### Phase 6 — UI/UX consistency
- Standardize cards, bottom navigation, headers, buttons, empty states, and dialogs.
- Ensure dark mode and mobile responsiveness.
- Use uploaded sports dashboard references as visual direction.

### Phase 7 — QA and regression
- Role-by-role manual test matrix.
- Sport-by-sport smoke test matrix.
- Admin web vs Flutter parity checklist.
- Backend permission tests.

## L. Generated Package Notes

This regenerated package includes:
- Full reconstructed `lib/` from Flutter source.
- Full reconstructed `src/` from backend/admin source.
- This audit report added to both `lib/MULTI_SPORT_AUDIT_REPORT.md` and `src/MULTI_SPORT_AUDIT_REPORT.md`.
- Safe Flutter UI wording update: obvious displayed phrases like “Football Academy Pro” were replaced with “Sports Academy Pro”. Java package names and backend identifiers were not renamed because that would be unsafe without full build migration.
