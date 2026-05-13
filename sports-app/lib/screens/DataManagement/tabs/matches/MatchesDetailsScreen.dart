import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../../components/app_background.dart';
import '../../../../components/ui_kit.dart';
import '../../../../controllers/matchController.dart';
import '../../../../models/match.dart';
import '../../../../theme/app_theme.dart';

class MatchDetailsScreen extends StatefulWidget {
  final int matchId;
  const MatchDetailsScreen({super.key, required this.matchId});

  @override
  State<MatchDetailsScreen> createState() => _MatchDetailsScreenState();
}

class _MatchDetailsScreenState extends State<MatchDetailsScreen> {
  final MatchController _controller = MatchController();
  MatchModel? _match;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    await _controller.loadAll();
    try {
      _match = _controller.matches.firstWhere((m) => m.id == widget.matchId);
    } catch (_) {
      _match = null;
    }
    if (mounted) setState(() => _loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
    elevation: 0,
    backgroundColor: Colors.transparent,title: const Text('Match Details')),
        body: _loading
            ? const Center(child: CircularProgressIndicator(color: AppTheme.teal))
            : _match == null
            ? const Center(child: Text('Match not found', style: TextStyle(color: Colors.white)))
            : ListView(
          padding: const EdgeInsets.only(bottom: 24),
          children: [
            SectionTitle(title: _match!.opponent ?? 'Opponent', subtitle: _match!.date),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _kv('Date', _formatDate(_match!.date)),
                    _kv('Location', _match!.location ?? '-'),
                    _kv('Result', _match!.result ?? '-'),
                    _kv('Score', _match!.score ?? '-'),
                    _kv('Division', _match!.divisionId?.toString() ?? '-'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatDate(String d) {
    final dt = DateTime.tryParse(d);
    if (dt == null) return d;
    return DateFormat('dd MMM yyyy').format(dt);
  }

  Widget _kv(String k, String v) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        children: [
          SizedBox(width: 110, child: Text(k, style: TextStyle( fontWeight: FontWeight.w700))),
          Expanded(child: Text(v, style: const TextStyle(fontWeight: FontWeight.w700))),
        ],
      ),
    );
  }
}


