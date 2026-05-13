import 'package:flutter/foundation.dart';

import '../models/scouting_models.dart';
import '../services/scouting_service.dart';

class ScoutingController extends ChangeNotifier {
  ScoutingController({ScoutingService? service})
      : _service = service ?? ScoutingService();

  final ScoutingService _service;

  bool loading = false;
  bool syncing = false;
  String? error;

  List<ScoutingPlayerCard> searchResults = [];
  List<ScoutingPlayerCard> comparedPlayers = [];
  Map<String, int> compareHighlights = {};

  String shortlistTitle = 'Scouting Shortlist';
  String shortlistStrategy = 'balanced';
  List<ScoutingPlayerCard> shortlistPlayers = [];

  final Map<int, Map<String, dynamic>> potentialByPlayer = {};
  final Map<int, Map<String, dynamic>> evolutionByPlayer = {};
  final Map<int, Map<String, dynamic>> churnByPlayer = {};

  Future<void> search({
    String? q,
    String? position,
    int? ageMin,
    int? ageMax,
    double? minPotential,
    double? maxChurn,
    String? trendLabel,
    double? minAvgRating,
    int limit = 20,
  }) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final result = await _service.searchPlayers(
        q: q,
        position: position,
        ageMin: ageMin,
        ageMax: ageMax,
        minPotential: minPotential,
        maxChurn: maxChurn,
        trendLabel: trendLabel,
        minAvgRating: minAvgRating,
        limit: limit,
      );
      searchResults = result.items;
    } catch (e) {
      error = e.toString();
      searchResults = [];
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> compare(List<int> playerExternalIds) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final result = await _service.comparePlayers(playerExternalIds);
      comparedPlayers = result.players;
      compareHighlights = result.highlights;
    } catch (e) {
      error = e.toString();
      comparedPlayers = [];
      compareHighlights = {};
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> generateShortlist({
    required String title,
    required String strategy,
    String? q,
    String? position,
    int? ageMin,
    int? ageMax,
    double? minPotential,
    double? maxChurn,
    String? trendLabel,
    double? minAvgRating,
    int topN = 10,
  }) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final result = await _service.generateShortlist(
        title: title,
        strategy: strategy,
        q: q,
        position: position,
        ageMin: ageMin,
        ageMax: ageMax,
        minPotential: minPotential,
        maxChurn: maxChurn,
        trendLabel: trendLabel,
        minAvgRating: minAvgRating,
        topN: topN,
      );

      shortlistTitle = result.title;
      shortlistStrategy = result.strategy;
      shortlistPlayers = result.players;
    } catch (e) {
      error = e.toString();
      shortlistPlayers = [];
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> loadInsights(int playerExternalId, {int window = 8}) async {
    loading = true;
    error = null;
    notifyListeners();

    try {
      final potential = await _service.getPotential(playerExternalId);
      final evolution = await _service.getEvolution(playerExternalId, window: window);
      final churn = await _service.getChurn(playerExternalId);

      potentialByPlayer[playerExternalId] = potential;
      evolutionByPlayer[playerExternalId] = evolution;
      churnByPlayer[playerExternalId] = churn;
    } catch (e) {
      error = e.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<Map<String, dynamic>?> syncFromBackend() async {
    syncing = true;
    error = null;
    notifyListeners();

    try {
      return await _service.syncFromFootballAcademy();
    } catch (e) {
      error = e.toString();
      return null;
    } finally {
      syncing = false;
      notifyListeners();
    }
  }

  String formatPercent(double value) {
    return (value * 100).toStringAsFixed(1);
  }
}


