import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import '../theme/app_colors.dart';

const String _apiHostOverride = String.fromEnvironment('API_HOST');
const String _chatbotHostOverride = String.fromEnvironment('CHATBOT_HOST');

String _backendHost() {
  if (_apiHostOverride.isNotEmpty) return _apiHostOverride;
  if (kIsWeb) return '192.168.0.159:8091';
  if (defaultTargetPlatform == TargetPlatform.android) {
    // On Android, localhost points to the phone itself.
    // Use the host machine IP or adb reverse when running on a real device.
    // Example with adb reverse:
    //   adb reverse tcp:8091 tcp:8091
    // Example with explicit host:
    //   flutter run --dart-define=API_HOST=192.168.x.x:8091
    return '192.168.0.159:8091';
  }
  return '192.168.0.159:8091';
}

// REST API BASE
final String API_BASE_URL =
    'http://${_backendHost()}/api';

const Duration API_TIMEOUT = Duration(seconds: 30);

// WEBSOCKET BASE
final String API_WS_BASE_URL =
  'ws://${_backendHost()}/ws';

// ═══════════════════════════════════════════════════════════════════════════
// TEXT COLORS - Use AppColors centralized palette
// ═══════════════════════════════════════════════════════════════════════════
const kLightTextColor = AppColors.textLight;
const kHardTextColor = AppColors.textDark;

// ═══════════════════════════════════════════════════════════════════════════
// PRIMARY COLORS - Use AppColors centralized palette
// ═══════════════════════════════════════════════════════════════════════════
const kPrimaryDarkColor = AppColors.successGreen;
const kPrimarylightColor = AppColors.accentGreen;
const kBackgroundColor = AppColors.bgLight;

// ═══════════════════════════════════════════════════════════════════════════
// CATEGORY COLORS - For displaying categories/divisions
// ═══════════════════════════════════════════════════════════════════════════
const List<Color> kCategoriesPrimaryColor = [
  AppColors.warningAmber,     // Amber with opacity
  AppColors.infoBlue,         // Blue with opacity
  AppColors.accentGreen,      // Green with opacity
  AppColors.errorRed,         // Red with opacity
];

const List<Color> kCategoriesSecondryColor = [
  Color(0xFFF1C40F),  // Golden Yellow
  Color(0xFF3498DB),  // Sky Blue
  Color(0xFF27AE60),  // Forest Green
  Color(0xFFE74C3C)   // Coral Red
];

const String? FILES_BASE_URL = null;

// Chatbot local
final String CHATBOT_API_BASE_URL =
  _chatbotHostOverride.isNotEmpty
    ? 'http://$_chatbotHostOverride'
    : defaultTargetPlatform == TargetPlatform.android && !kIsWeb
        ? 'http://localhost:8000'
        : 'http://localhost:8000';
final String CHATBOT_API_CHAT_URL = '$CHATBOT_API_BASE_URL/api/chat';
const String? CHATBOT_API_KEY = null;

