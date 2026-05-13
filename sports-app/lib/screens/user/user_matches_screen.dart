import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../components/app_background.dart';
import '../../components/ui_kit.dart';
import '../../l10n/app_strings.dart';
import '../../models/match.dart';
import '../../services/match_service.dart';
import '../../theme/app_theme.dart';

class UserMatchesScreen extends StatefulWidget {
  const UserMatchesScreen({super.key});

  @override
  State<UserMatchesScreen> createState() => _UserMatchesScreenState();
}

class _UserMatchesScreenState extends State<UserMatchesScreen> {
  final MatchService _service = MatchService();
  final TextEditingController _search = TextEditingController();

  bool _loading = true;
  String? _error;
  List<MatchModel> _items = [];

  @override
  void initState() {
    super.initState();
    _search.addListener(() => setState(() {}));
    _load();
  }

  @override
  void dispose() {
    _search.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      _items = await _service.getAll();
      _items.sort((a, b) => b.date.compareTo(a.date));
    } catch (e) {
      _error = e.toString();
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  DateTime? _parseDate(String s) {
    try {
      return DateTime.parse(s);
    } catch (_) {
      return null;
    }
  }

  List<MatchModel> get _filtered {
    final q = _search.text.trim().toLowerCase();
    if (q.isEmpty) return _items;
    return _items.where((m) {
      return (m.opponent ?? '').toLowerCase().contains(q) ||
          (m.location ?? '').toLowerCase().contains(q) ||
          (m.result ?? '').toLowerCase().contains(q) ||
          (m.score ?? '').toLowerCase().contains(q) ||
          m.date.toLowerCase().contains(q);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final t = AppStrings.of(context);

    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          elevation: 0,
          backgroundColor: Colors.transparent,
          title: Text(t.tr('matches')),),
      body: _loading
            ? const Center(child: CircularProgressIndicator(color: AppTheme.teal))
            : _error != null
                ? Center(child: Text(_error!, style: const TextStyle(color: Colors.white)))
                : Column(
                    children: [
                      Padding(
                        padding: const EdgeInsets.fromLTRB(12, 8, 12, 10),
                        child: TextField(
                          controller: _search,
                          decoration: InputDecoration(
                            hintText: '${t.tr('matches')}',
                            prefixIcon: const Icon(Icons.search),
                            filled: true,
                            fillColor: Theme.of(context).colorScheme.surface.withOpacity(0.92),
                            border: OutlineInputBorder(
                              borderRadius: BorderRadius.circular(16),
                              borderSide: BorderSide.none,
                            ),
                          ),
                        ),
                      ),
                      Expanded(
                        child: _filtered.isEmpty
                            ? ListView(
                                physics: const AlwaysScrollableScrollPhysics(),
                                children: [
                                  const SizedBox(height: 90),
                                  Icon(Icons.sports_soccer, size: 72, color: Theme.of(context).colorScheme.surface.withOpacity(0.92)),
                                  const SizedBox(height: 14),
                                  Center(child: Text(t.tr('empty'), style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700))),
                                ],
                              )
                            : RefreshIndicator(
                                onRefresh: _load,
                                child: ListView.separated(
                                  physics: const AlwaysScrollableScrollPhysics(),
                                  padding: const EdgeInsets.fromLTRB(12, 0, 12, 24),
                                  itemCount: _filtered.length,
                                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                                  itemBuilder: (_, i) => _matchCard(context, _filtered[i]),
                                ),
                              ),
                      ),
                    ],
                  ),
      ),
    );
  }

  Widget _matchCard(BuildContext context, MatchModel m) {
    final t = AppStrings.of(context);
    final dt = _parseDate(m.date);
    final dateLabel = dt == null ? m.date : DateFormat('EEE, MMM d  yyyy').format(dt);
    final res = (m.result ?? '').toLowerCase();

    Color badgeColor = Colors.grey;
    String badgeText = m.result ?? '';
    if (res.contains('win')) {
      badgeColor = Colors.green;
      badgeText = 'WIN';
    } else if (res.contains('loss')) {
      badgeColor = Colors.red;
      badgeText = 'LOSS';
    } else if (res.contains('draw')) {
      badgeColor = Colors.orange;
      badgeText = 'DRAW';
    }

    return SoftCard(
      padding: const EdgeInsets.all(14),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: Colors.blue.withOpacity(0.12),
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.sports_soccer, color: Colors.blue),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        m.opponent ?? 'Match',
                        style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 16),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    if (badgeText.isNotEmpty)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                        decoration: BoxDecoration(
                          color: badgeColor.withOpacity(0.12),
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(color: badgeColor.withOpacity(0.25)),
                        ),
                        child: Text(badgeText, style: TextStyle(color: badgeColor, fontWeight: FontWeight.w900, fontSize: 12)),
                      ),
                  ],
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    _chip('${t.tr('date')}: $dateLabel', Icons.calendar_today),
                    if ((m.location ?? '').isNotEmpty) _chip('${t.tr('location')}: ${m.location}', Icons.place),
                    if ((m.score ?? '').isNotEmpty) _chip('Score: ${m.score}', Icons.scoreboard),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _chip(String text, IconData icon) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: Colors.teal),
          const SizedBox(width: 6),
          Flexible(child: Text(text, overflow: TextOverflow.ellipsis)),
        ],
      ),
    );
  }
}


