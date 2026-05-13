import 'package:flutter/material.dart';
import '../../components/app_background.dart';
import '../../components/modern_design_system.dart';
import '../../controllers/session_controller.dart';
import '../../models/player.dart';
import '../../services/PlayerServices.dart';
import '../../theme/app_theme.dart';
import '../../l10n/app_strings.dart';

class TrainerPlayerStatsScreen extends StatefulWidget {
  const TrainerPlayerStatsScreen({super.key});

  @override
  State<TrainerPlayerStatsScreen> createState() => _TrainerPlayerStatsScreenState();
}

class _TrainerPlayerStatsScreenState extends State<TrainerPlayerStatsScreen> {
  final _service = PlayerService();
  bool _loading = true;
  String? _error;
  List<Player> _items = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() { _loading = true; _error = null; });
    try {
      final all = await _service.getAllPlayers();
      final divisionId = AppSession.instance.session.divisionId;
      _items = divisionId == null ? all : all.where((p) => p.divisionId == divisionId).toList();
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _quickUpdate(Player p) async {
    // Minimal: mark played true to demonstrate; extend as needed.
    await _service.updatePlayerStats(playerId: p.id, played: true);
    _load();
  }

  @override
  Widget build(BuildContext context) {
    final t = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(t.tr('update_player_stats'))),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppTheme.teal))
          : _error != null
              ? Center(child: Text(_error!))
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.builder(
                    itemCount: _items.length,
                    itemBuilder: (_, i) {
                      final p = _items[i];
                      return Card(
                        child: ListTile(
                          title: Text(p.nom ?? 'Player'),
                          subtitle: Text('Goals: ${p.goals ?? 0}  Assists: ${p.assists ?? 0}  Matches: ${p.matches ?? 0}'),
                          trailing: IconButton(
                            icon: const Icon(Icons.check_circle, color: AppTheme.teal),
                            onPressed: () => _quickUpdate(p),
                          ),
                        ),
                      );
                    },
                  ),
                ),
    );
  }
}


