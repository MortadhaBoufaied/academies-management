import 'dart:convert';
import 'package:http/http.dart' as http;

import '../components/Constants.dart';

class ChatbotApiResult {
  final String response;
  final double? score;
  final String? category;
  final String? source;
  final String? matchedQuestion;

  ChatbotApiResult({
    required this.response,
    this.score,
    this.category,
    this.source,
    this.matchedQuestion,
  });

  factory ChatbotApiResult.fromJson(Map<String, dynamic> json) {
    return ChatbotApiResult(
      response: (json['response'] ?? '').toString(),
      score: json['score'] is num ? (json['score'] as num).toDouble() : null,
      category: json['category']?.toString(),
      source: json['source']?.toString(),
      matchedQuestion: json['matched_question']?.toString(),
    );
  }
}

/// Chatbot service that talks to the local Django chatbot API.
/// Endpoint: POST {CHATBOT_API_CHAT_URL}
class ChatbotService {
  final http.Client _client;

  ChatbotService({http.Client? client}) : _client = client ?? http.Client();

  Future<ChatbotApiResult> sendMessage(String message) async {
    final headers = <String, String>{
      'Content-Type': 'application/json',
    };
    if (CHATBOT_API_KEY != null && CHATBOT_API_KEY!.trim().isNotEmpty) {
      headers['X-API-Key'] = CHATBOT_API_KEY!;
    }

    final uri = Uri.parse(CHATBOT_API_CHAT_URL);

    final resp = await _client
        .post(
          uri,
          headers: headers,
          body: jsonEncode({'message': message}),
        )
        .timeout(API_TIMEOUT);

    if (resp.statusCode == 200) {
      final Map<String, dynamic> data = jsonDecode(utf8.decode(resp.bodyBytes));
      return ChatbotApiResult.fromJson(data);
    }

    // Try to decode error body
    String details = '';
    try {
      final j = jsonDecode(utf8.decode(resp.bodyBytes));
      details = j is Map && j['error'] != null ? ' (${j['error']})' : '';
    } catch (_) {}

    throw Exception('Chatbot API error: ${resp.statusCode}$details');
  }

  /// Backward compatible helper used by older UI.
  Future<String> getAnswer(String question) async {
    final r = await sendMessage(question);
    return r.response.isNotEmpty
        ? r.response
        : 'No confident answer found. Try rephrasing your question.';
  }

  /// The new Django chatbot API does not expose the full knowledge base.
  /// Keep this method for compatibility but return an empty list.
  Future<List<dynamic>> getKnowledgeBase() async => [];
}


