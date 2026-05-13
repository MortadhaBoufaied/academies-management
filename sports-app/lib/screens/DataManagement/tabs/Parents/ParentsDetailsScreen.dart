import 'package:flutter/material.dart';

import '../../../../components/app_background.dart';
import '../../../../components/ui_kit.dart';
import '../../../../controllers/parent_controller.dart';
import '../../../../models/parent.dart';
import '../../../../models/player.dart';
import '../../../../services/parent_service.dart';
import '../../../../theme/app_theme.dart';
import '../players/footballer_details_screen.dart';

class ParentDetailsScreen extends StatefulWidget {
  final int parentId;
  const ParentDetailsScreen({super.key, required this.parentId});

  @override
  State<ParentDetailsScreen> createState() => _ParentDetailsScreenState();
}

class _ParentDetailsScreenState extends State<ParentDetailsScreen> {
  final ParentController _controller = ParentController();
  final ParentService _parentService = ParentService();

  Parent? _parent;
  List<Player> _children = [];
  bool _loading = true;
  bool _childrenLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _childrenLoading = true;
      _error = null;
    });

    try {
      await _controller.loadAll();
      final matches =
          _controller.parents.where((p) => p.id == widget.parentId).toList();
      _parent = matches.isEmpty ? null : matches.first;
      if (_parent != null) {
        _children = await _parentService.getChildren(widget.parentId);
      }
    } catch (e) {
      _error = e.toString();
      _parent = null;
      _children = [];
    } finally {
      if (mounted) {
        setState(() {
          _loading = false;
          _childrenLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          elevation: 0,
          backgroundColor: Colors.transparent,
          title: const Text('Parent details'),
        ),
        body:
            _loading
                ? const Center(
                  child: CircularProgressIndicator(color: AppTheme.teal),
                )
                : _parent == null
                ? _notFound()
                : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    padding: const EdgeInsets.fromLTRB(12, 12, 12, 24),
                    children: [
                      _header(),
                      const SizedBox(height: 12),
                      _contactCard(),
                      const SizedBox(height: 14),
                      SectionTitle(title: 'Children'),
                      const SizedBox(height: 8),
                      if (_childrenLoading)
                        const Center(
                          child: CircularProgressIndicator(
                            color: AppTheme.teal,
                          ),
                        )
                      else if (_children.isEmpty)
                        _emptyChildren()
                      else
                        ..._children.map(_childCard),
                    ],
                  ),
                ),
      ),
    );
  }

  Widget _notFound() {
    final message =
        _error == null ? 'Parent not found' : 'Parent not found: $_error';
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Text(
          message,
          textAlign: TextAlign.center,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.w800,
          ),
        ),
      ),
    );
  }

  Widget _header() {
    final p = _parent!;
    final name = _value(
      p.nom,
      fallback: _value(p.email, fallback: 'Parent profile'),
    );
    final subtitle = _value(
      p.email,
      fallback: _value(p.tel, fallback: 'Family account'),
    );

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 4),
      child: Row(
        children: [
          CircleAvatar(
            radius: 30,
            backgroundColor: AppTheme.teal.withOpacity(0.22),
            child: Text(
              _initial(name),
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w900,
                fontSize: 20,
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  name,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.w900,
                    fontSize: 20,
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  subtitle,
                  style: TextStyle(
                    color: Theme.of(
                      context,
                    ).colorScheme.surface.withOpacity(0.92),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _contactCard() {
    final p = _parent!;
    return SoftCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Contact information',
            style: TextStyle(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 10),
          _kv('Name', _value(p.nom)),
          _kv('Email', _value(p.email)),
          _kv('Phone', _value(p.tel)),
          _kv('Second phone', _value(p.tel2)),
          _kv('Address', _value(p.address)),
          _kv('Account ID', p.userId.toString()),
        ],
      ),
    );
  }

  Widget _emptyChildren() {
    return SoftCard(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Column(
          children: const [
            Icon(Icons.family_restroom, color: AppTheme.teal, size: 38),
            SizedBox(height: 8),
            Text(
              'No linked children',
              style: TextStyle(fontWeight: FontWeight.w900),
            ),
            SizedBox(height: 4),
            Text('Link players to this parent to complete the family profile.'),
          ],
        ),
      ),
    );
  }

  Widget _childCard(Player c) {
    final name = _value(c.nom, fallback: 'Player');
    final subtitle = [
      _value(c.divisionName),
      _value(c.position),
    ].where((item) => item.isNotEmpty && item != '-').join(' - ');

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: AppTheme.teal.withOpacity(0.15),
          child: Text(
            _initial(name),
            style: const TextStyle(
              color: AppTheme.teal,
              fontWeight: FontWeight.w900,
            ),
          ),
        ),
        title: Text(name, style: const TextStyle(fontWeight: FontWeight.w900)),
        subtitle: subtitle.isEmpty ? null : Text(subtitle),
        trailing: const Icon(Icons.chevron_right),
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => FootballerDetailsScreen(playerId: c.id),
            ),
          );
        },
      ),
    );
  }

  Widget _kv(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 116,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
        ],
      ),
    );
  }

  String _value(String? value, {String fallback = '-'}) {
    final text = value?.trim() ?? '';
    return text.isEmpty ? fallback : text;
  }

  String _initial(String value) {
    final text = value.trim();
    return text.isEmpty ? '?' : text[0].toUpperCase();
  }
}


