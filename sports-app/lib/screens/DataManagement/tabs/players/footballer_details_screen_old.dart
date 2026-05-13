import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import '../../../../models/player.dart';
import '../../../../services/PlayerServices.dart';
import '../../../../components/ui_kit.dart';

class FootballerDetailsScreen extends StatefulWidget {
  final int playerId;
  final bool isCurrentUser;

  const FootballerDetailsScreen({
    Key? key,
    required this.playerId,
    this.isCurrentUser = false,
  }) : super(key: key);

  @override
  State<FootballerDetailsScreen> createState() => _FootballerProfilePageState();
}

class _FootballerProfilePageState extends State<FootballerDetailsScreen> {
  Player? _player;
  bool _loading = true;

  final PlayerService _playerService = PlayerService();

  final ScrollController _scrollController = ScrollController();
  double _scrollOffset = 0.0;
  bool get _showExpandedHeader => _scrollOffset < 100;

  int _selectedIndex = 0;

  @override
  void initState() {
    super.initState();
    _loadPlayer();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    setState(() {
      _scrollOffset = _scrollController.offset;
    });
  }

  Future<void> _loadPlayer() async {
    try {
      final player = await _playerService.getPlayerById(widget.playerId);
      setState(() {
        _player = player;
      });
    } catch (e) {
      debugPrint('Failed to load player: $e');
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator(color: Colors.teal)),
      );
    }

    if (_player == null) {
      return const Scaffold(
        body: Center(child: Text('Player not found')),
      );
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF4F4F1),
      body: NestedScrollView(
        controller: _scrollController,
        headerSliverBuilder: (context, innerBoxIsScrolled) {
          return [
            _buildAppBar(),
            _buildTabBar(),
          ];
        },
        body: _buildCurrentTab(),
      ),
    );
  }

  /* ------------------------------- APP BAR ------------------------------- */

  SliverAppBar _buildAppBar() {
    return SliverAppBar(
      expandedHeight: 328.0,
      collapsedHeight: 84.0,
      floating: false,
      pinned: true,
      snap: false,
      stretch: true,
      backgroundColor: const Color(0xFF1B4332),
      elevation: 4,
      shadowColor: Colors.black.withOpacity(0.3),
      flexibleSpace: FlexibleSpaceBar(
        collapseMode: CollapseMode.pin,
        stretchModes: const [StretchMode.zoomBackground],
        background: _showExpandedHeader
            ? _buildExpandedHeader()
            : _buildCollapsedHeader(),
        title: _showExpandedHeader ? null : _buildAppBarTitle(),
        centerTitle: true,
      ),
    );
  }

  Widget _buildExpandedHeader() {
    final p = _player!;
    return Stack(
      fit: StackFit.expand,
      children: [
        _buildHeaderBackdrop(),
        SafeArea(
          bottom: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 18, 20, 18),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.14),
                    borderRadius: BorderRadius.circular(999),
                    border: Border.all(color: Colors.white.withOpacity(0.16)),
                  ),
                  child: Text(
                    widget.isCurrentUser ? 'My Profile' : 'Player Profile',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
                const Spacer(),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    _buildPlayerImage(102),
                    const SizedBox(width: 18),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            p.nom ?? 'Unknown Player',
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 28,
                              fontWeight: FontWeight.w900,
                              height: 1.0,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            p.position ?? 'No position',
                            style: TextStyle(
                              color: Colors.white.withOpacity(0.9),
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          const SizedBox(height: 12),
                          _buildPlayerMetadata(),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 18),
                _buildQuickStats(),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildCollapsedHeader() {
    return _buildHeaderBackdrop(collapsed: true);
  }

  Widget _buildAppBarTitle() {
    final p = _player!;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        CircleAvatar(
          radius: 18,
          backgroundImage: _getPlayerImageProvider(),
          backgroundColor: Colors.white24,
        ),
        const SizedBox(width: 12),
        Flexible(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                p.nom ?? 'Unknown Player',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              Text(
                p.position ?? 'No position',
                style: TextStyle(
                  color: Colors.white.withOpacity(0.88),
                  fontSize: 12,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ],
    );
  }

  SliverPersistentHeader _buildTabBar() {
    return SliverPersistentHeader(
      pinned: true,
      delegate: _TabBarDelegate(
        child: Container(
          color: Theme.of(context).colorScheme.surface,
          child: _buildSegmentedTabs(),
        ),
      ),
    );
  }

  /* ------------------------------ TOP IMAGE ------------------------------ */

  Widget _buildPlayerImage(double size) {
    final p = _player!;
    return Stack(
      children: [
        Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 3),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.3),
                blurRadius: 12,
                offset: const Offset(0, 4),
              ),
            ],
          ),
          child: ClipOval(
            child: _buildPlayerImageContent(),
          ),
        ),
        if ((p.rating ?? 0) > 0)
          Positioned(
            bottom: 0,
            right: 0,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: Colors.amber,
                borderRadius: BorderRadius.circular(12),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.2),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.star, color: Colors.white, size: 12),
                  const SizedBox(width: 2),
                  Text(
                    (p.rating ?? 0).toStringAsFixed(1),
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.surface,
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }

  Widget _buildPlayerImageContent() {
    final p = _player!;
    final url = p.imageUrl;

    if (url == null || url.isEmpty) {
      return Container(
        color: Colors.teal.shade100,
        child: const Icon(Icons.person, color: Colors.teal, size: 40),
      );
    }

    return Image.network(url, fit: BoxFit.cover);
  }

  ImageProvider? _getPlayerImageProvider() {
    final url = _player!.imageUrl;
    if (url == null || url.isEmpty) return null;
    return NetworkImage(url);
  }

  /* ------------------------------ METADATA ------------------------------- */

  Widget _buildPlayerMetadata() {
    final p = _player!;
    final chips = <String>[];

    if ((p.age ?? 0) > 0) chips.add('${p.age} yrs');
    if ((p.nationalite ?? '').isNotEmpty) chips.add(p.nationalite!);
    if ((p.divisionName ?? '').isNotEmpty) chips.add(p.divisionName!);
    if ((p.number ?? 0) > 0) chips.add('No. ${p.number}');

    if (chips.isEmpty) return const SizedBox();

    return Wrap(
      spacing: 8,
      runSpacing: 4,
      children: chips
          .map(
            (text) => Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.12),
                borderRadius: BorderRadius.circular(999),
                border: Border.all(color: Colors.white.withOpacity(0.12)),
              ),
              child: Text(
                text,
                style: TextStyle(
                  color: Colors.white.withOpacity(0.92),
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
      )
          .toList(),
    );
  }

  /* ------------------------------ QUICK STATS ---------------------------- */

  Widget _buildQuickStats() {
    final p = _player!;
    final matches = (p.matches ?? 0).toString();
    final goals = (p.goals ?? 0).toString();
    final assists = (p.assists ?? 0).toString();
    final rating = (p.rating ?? 0).toStringAsFixed(1);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.12),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white.withOpacity(0.16)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _quickStatItem('Matches', matches, Icons.sports_soccer),
          _quickStatItem('Goals', goals, Icons.emoji_events),
          _quickStatItem('Assists', assists, Icons.assistant),
          _quickStatItem('Rating', rating, Icons.star),
        ],
      ),
    );
  }

  Widget _quickStatItem(String label, String value, IconData icon) {
    return Column(
      children: [
        Icon(icon, color: Colors.white, size: 18),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 16,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: TextStyle(
            color: Colors.white.withOpacity(0.84),
            fontSize: 11,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }

  Widget _buildHeaderBackdrop({bool collapsed = false}) {
    final provider = _getPlayerImageProvider();
    return Stack(
      fit: StackFit.expand,
      children: [
        Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFF1B4332), Color(0xFF0F766E)],
            ),
          ),
        ),
        if (provider != null)
          ImageFiltered(
            imageFilter: ImageFilter.blur(
              sigmaX: collapsed ? 10 : 6,
              sigmaY: collapsed ? 10 : 6,
            ),
            child: Image(
              image: provider,
              fit: BoxFit.cover,
            ),
          ),
        DecoratedBox(
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Colors.black.withOpacity(collapsed ? 0.38 : 0.24),
                const Color(0xFF111827).withOpacity(0.22),
                const Color(0xFF111827).withOpacity(0.58),
              ],
            ),
          ),
        ),
        Positioned(
          top: -28,
          right: -20,
          child: Container(
            width: 140,
            height: 140,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.white.withOpacity(0.08),
            ),
          ),
        ),
      ],
    );
  }

  /* -------------------------------- TABS -------------------------------- */

  Widget _buildSegmentedTabs() {
    final items = const ['Profile', 'Statistics', 'Career'];

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: SegmentedButton<int>(
        style: ButtonStyle(
          backgroundColor: MaterialStateProperty.all(
              Theme.of(context).colorScheme.surface),
          foregroundColor: MaterialStateProperty.resolveWith<Color>(
                (states) {
              if (states.contains(MaterialState.selected)) {
                return Colors.white; // text color when selected
              }
              return Colors.teal; // text color when unselected
            },
          ),
          overlayColor: MaterialStateProperty.all(Colors.teal.withOpacity(0.1)),
          side: MaterialStateProperty.all(BorderSide.none),
          shape: MaterialStateProperty.all(
            RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
          padding: MaterialStateProperty.all(
            const EdgeInsets.symmetric(vertical: 12),
          ),
        ),
        segments: List.generate(
          items.length,
              (i) => ButtonSegment<int>(
            value: i,
            label: Text(
              items[i],
              style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
            ),
          ),
        ),
        selected: {_selectedIndex},
        onSelectionChanged: (s) => setState(() => _selectedIndex = s.first),
      ),
    );
  }

  Widget _buildCurrentTab() {
    switch (_selectedIndex) {
      case 0:
        return _buildProfileTab();
      case 1:
        return _buildStatsTab();
      case 2:
        return _buildCareerTab();
      default:
        return _buildProfileTab();
    }
  }

  /* ------------------------------ PROFILE TAB --------------------------- */

  Widget _buildProfileTab() {
    final p = _player!;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          _buildSection(
            title: 'Personal Information',
            icon: Icons.person_outline,
            children: [
              _infoRow('Full name', p.nom ?? '-'),
              _infoRow('Position', p.position ?? '-'),
              if ((p.age ?? 0) > 0) _infoRow('Age', '${p.age} years'),
              if ((p.nationalite ?? '').isNotEmpty)
                _infoRow('Nationality', p.nationalite!),
              if ((p.divisionName ?? '').isNotEmpty)
                _infoRow('Division', p.divisionName!),
              if ((p.number ?? 0) > 0) _infoRow('Number', '${p.number}'),
            ],
          ),
          const SizedBox(height: 20),
          if ((p.height ?? 0) > 0 || (p.weight ?? 0) > 0) ...[
            _buildSection(
              title: 'Physical Attributes',
              icon: Icons.fitness_center,
              children: [
                if ((p.height ?? 0) > 0) _infoRow('Height', '${p.height} cm'),
                if ((p.weight ?? 0) > 0) _infoRow('Weight', '${p.weight} kg'),
                if ((p.height ?? 0) > 0 && (p.weight ?? 0) > 0)
                  _buildBMIIndicator(p.height!, p.weight!),
              ],
            ),
            const SizedBox(height: 20),
          ],
          if ((p.tel ?? '').isNotEmpty || (p.email ?? '').isNotEmpty)
            _buildSection(
              title: 'Contact',
              icon: Icons.contact_phone,
              children: [
                if ((p.tel ?? '').isNotEmpty)
                  _contactRow('Phone', p.tel!, Icons.phone),
                if ((p.email ?? '').isNotEmpty)
                  _contactRow('Email', p.email!, Icons.email),
              ],
            ),
        ],
      ),
    );
  }

  /* ---------------------------- STATS TAB -------------------------------- */

  Widget _buildStatsTab() {
    final p = _player!;
    final double rating = (p.rating ?? 0);
    final int goals = p.goals ?? 0;
    final int assists = p.assists ?? 0;
    final int matches = p.matches ?? 0;

    final double maxValue =
    [goals.toDouble(), assists.toDouble(), matches.toDouble()].reduce((a, b) => a > b ? a : b);

    final double goalsPerMatch = matches > 0 ? goals / matches : 0.0;
    final double assistsPerMatch = matches > 0 ? assists / matches : 0.0;
    final double contributionRate =
    matches > 0 ? ((goals + assists) / matches) * 100 : 0.0;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          // Overall rating
          Card(
            elevation: 3,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  Text(
                    'Overall Rating',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.teal.shade800),
                  ),
                  const SizedBox(height: 20),
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      SizedBox(
                        width: 120,
                        height: 120,
                        child: CircularProgressIndicator(
                          value: rating / 5,
                          strokeWidth: 12,
                          backgroundColor: Colors.grey.shade200,
                          color: _ratingColor(rating),
                          valueColor: AlwaysStoppedAnimation(_ratingColor(rating)),
                        ),
                      ),
                      Column(
                        children: [
                          Text(
                            rating.toStringAsFixed(1),
                            style: const TextStyle(
                              fontSize: 28,
                              fontWeight: FontWeight.bold,
                              height: 1),
                          ),
                          Text(
                            '/5.0',
                            style: TextStyle(
                              fontSize: 14,
                              color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  _starRating(rating),
                  const SizedBox(height: 8),
                  Text(
                    _ratingDescription(rating),
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
                      fontSize: 14,
                      fontStyle: FontStyle.italic,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 20),

          // Performance overview (bar chart)
          Card(
            elevation: 3,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Performance Overview',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.teal.shade800),
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 200,
                    child: BarChart(
                      BarChartData(
                        alignment: BarChartAlignment.spaceAround,
                        maxY: (maxValue == 0 ? 10 : maxValue) * 1.3,
                        barTouchData: BarTouchData(enabled: false),
                        titlesData: FlTitlesData(
                          bottomTitles: AxisTitles(
                            sideTitles: SideTitles(
                              showTitles: true,
                              getTitlesWidget: (value, meta) {
                                switch (value.toInt()) {
                                  case 0:
                                    return const Padding(
                                      padding: EdgeInsets.only(top: 8.0),
                                      child: Text('Goals', style: TextStyle(fontSize: 12)),
                                    );
                                  case 1:
                                    return const Padding(
                                      padding: EdgeInsets.only(top: 8.0),
                                      child: Text('Assists', style: TextStyle(fontSize: 12)),
                                    );
                                  case 2:
                                    return const Padding(
                                      padding: EdgeInsets.only(top: 8.0),
                                      child: Text('Matches', style: TextStyle(fontSize: 12)),
                                    );
                                  default:
                                    return const Text('');
                                }
                              },
                            ),
                          ),
                          leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                        ),
                        gridData: const FlGridData(show: false),
                        borderData: FlBorderData(show: false),
                        barGroups: [
                          BarChartGroupData(
                            x: 0,
                            barsSpace: 4,
                            barRods: [
                              BarChartRodData(
                                toY: goals.toDouble(),
                                color: Colors.red.shade400,
                                width: 24,
                                borderRadius: BorderRadius.circular(4),
                              ),
                            ],
                          ),
                          BarChartGroupData(
                            x: 1,
                            barsSpace: 4,
                            barRods: [
                              BarChartRodData(
                                toY: assists.toDouble(),
                                color: Colors.green.shade400,
                                width: 24,
                                borderRadius: BorderRadius.circular(4),
                              ),
                            ],
                          ),
                          BarChartGroupData(
                            x: 2,
                            barsSpace: 4,
                            barRods: [
                              BarChartRodData(
                                toY: matches.toDouble(),
                                color: Colors.blue.shade400,
                                width: 24,
                                borderRadius: BorderRadius.circular(4),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 20),

          // Key metrics
          Card(
            elevation: 3,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Key Metrics',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Colors.teal.shade800),
                  ),
                  const SizedBox(height: 16),
                  GridView.count(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    crossAxisCount: 2,
                    crossAxisSpacing: 12,
                    mainAxisSpacing: 12,
                    childAspectRatio: 1.4,
                    children: [
                      _metricCard(
                        'Matches',
                        matches.toString(),
                        'Total appearances',
                        FontAwesomeIcons.futbol,
                        Colors.blue.shade400,
                      ),
                      _metricCard(
                        'Goals',
                        goals.toString(),
                        '${goalsPerMatch.toStringAsFixed(2)} per match',
                        FontAwesomeIcons.fire,
                        Colors.red.shade400,
                      ),
                      _metricCard(
                        'Assists',
                        assists.toString(),
                        '${assistsPerMatch.toStringAsFixed(2)} per match',
                        FontAwesomeIcons.shoePrints,
                        Colors.green.shade400,
                      ),
                      _metricCard(
                        'Contribution',
                        '${contributionRate.toStringAsFixed(1)}%',
                        'Impact rate per match',
                        FontAwesomeIcons.chartLine,
                        Colors.orange.shade400,
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  /* ----------------------------- CAREER TAB ------------------------------ */

  Widget _buildCareerTab() {
    final p = _player!;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          Card(
            elevation: 3,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(Icons.timeline, color: Colors.teal.shade700, size: 22),
                      const SizedBox(width: 12),
                      Text(
                        'Career Highlights',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: Colors.teal.shade800),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  _careerHighlightItem('Matches played', '${p.matches ?? 0}', Icons.sports_soccer),
                  _careerHighlightItem('Goals scored', '${p.goals ?? 0}', Icons.emoji_events),
                  _careerHighlightItem('Assists', '${p.assists ?? 0}', Icons.assistant),
                  _careerHighlightItem('Average rating', (p.rating ?? 0).toStringAsFixed(1), Icons.star),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),
          _buildAchievements(),
          const SizedBox(height: 20),
          _buildCareerProgression(),
        ],
      ),
    );
  }

  /* ------------------------------- SECTIONS ------------------------------ */

  Widget _buildSection({
    required String title,
    required IconData icon,
    required List<Widget> children,
  }) {
    return SoftCard(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(icon, color: Colors.teal.shade700, size: 22),
                const SizedBox(width: 12),
                Text(
                  title,
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.teal.shade800),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        children: [
          Expanded(
            child: Text(
              label,
              style: TextStyle(
                fontWeight: FontWeight.w600,
                color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
                fontSize: 15,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 15),
              textAlign: TextAlign.end,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBMIIndicator(double heightCm, double weightKg) {
    final bmi = weightKg / ((heightCm / 100) * (heightCm / 100));
    String status = 'Normal';
    Color color = Colors.green;

    if (bmi < 18.5) {
      status = 'Underweight';
      color = Colors.orange;
    } else if (bmi > 25) {
      status = 'Overweight';
      color = Colors.red;
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 8),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('BMI (Body Mass Index)',
                style: TextStyle(fontWeight: FontWeight.w600)),
            Text(bmi.toStringAsFixed(1),
                style: TextStyle(fontWeight: FontWeight.bold, color: color)),
          ],
        ),
        const SizedBox(height: 6),
        Text(status, style: TextStyle(color: color, fontSize: 13)),
        const SizedBox(height: 6),
        LinearProgressIndicator(
          value: (bmi.clamp(15, 35) / 35).toDouble(),
          backgroundColor: Colors.grey.shade200,
          color: color,
          minHeight: 8,
          borderRadius: BorderRadius.circular(4),
        ),
      ],
    );
  }

  Widget _contactRow(String label, String value, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        children: [
          Icon(icon, color: Colors.grey.shade600, size: 20),
          const SizedBox(width: 12),
          Expanded(
            child: Text(label, style: TextStyle(fontWeight: FontWeight.w600)),
          ),
          Expanded(
            flex: 2,
            child: SelectableText(
              value,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              textAlign: TextAlign.end,
            ),
          ),
        ],
      ),
    );
  }

  Widget _metricCard(String title, String value, String subtitle, IconData icon, Color color) {
    return SoftCard(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 28),
            const SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
                color: color),
            ),
            const SizedBox(height: 4),
            Text(
              title,
              style: TextStyle(
                color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              subtitle,
              style: TextStyle(
                color: Theme.of(context).colorScheme.surface.withOpacity(Theme.of(context).brightness == Brightness.dark ? 0.18 : 0.92),
                fontSize: 10,
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  /* ---------------------------- ACHIEVEMENTS ----------------------------- */

  Widget _buildAchievements() {
    final p = _player!;
    final items = <Widget>[];

    if ((p.goals ?? 0) >= 10) {
      items.add(_achievementItem(
        'Team Top Scorer',
        'Scored ${p.goals} goals',
        Icons.emoji_events,
        Colors.amber,
      ));
    }
    if ((p.rating ?? 0) >= 4.5) {
      items.add(_achievementItem(
        'Best Performance',
        'Rating ${(p.rating ?? 0).toStringAsFixed(1)} of 5.0',
        Icons.star,
        Colors.blue,
      ));
    }
    if ((p.matches ?? 0) >= 15) {
      items.add(_achievementItem(
        'Regular Starter',
        '${p.matches} matches played',
        Icons.verified,
        Colors.green,
      ));
    }
    if ((p.assists ?? 0) >= 8) {
      items.add(_achievementItem(
        'Playmaker',
        '${p.assists} assists',
        Icons.assistant,
        Colors.purple,
      ));
    }
    if ((p.goals ?? 0) >= 5 && (p.assists ?? 0) >= 5) {
      items.add(_achievementItem(
        'Complete Contributor',
        '${p.goals} goals & ${p.assists} assists',
        Icons.all_inclusive,
        Colors.orange,
      ));
    }

    if (items.isEmpty) {
      return SoftCard(
        child: const Padding(
          padding: EdgeInsets.all(32),
          child: Column(
            children: [
              Icon(Icons.workspace_premium, size: 48, color: Colors.grey),
              SizedBox(height: 16),
              Text(
                'No achievements yet',
                style: TextStyle(
                  
                  fontSize: 16,
                  fontWeight: FontWeight.w500),
              ),
              SizedBox(height: 8),
              Text(
                'Keep working hard to earn new achievements!',
                style: TextStyle(
                  
                  fontSize: 14),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      );
    }

    return SoftCard(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.workspace_premium, color: Colors.teal.shade700, size: 22),
                const SizedBox(width: 12),
                Text(
                  'Achievements & Awards',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.teal.shade800),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ...items,
          ],
        ),
      ),
    );
  }

  Widget _achievementItem(String title, String subtitle, IconData icon, Color color) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: color.withOpacity(0.08),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withOpacity(0.2)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: color, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title,
                    style: TextStyle(fontWeight: FontWeight.bold, color: color, fontSize: 14)),
                const SizedBox(height: 2),
                Text(subtitle, style: TextStyle(color: color.withOpacity(0.8), fontSize: 12)),
              ],
            ),
          ),
          Icon(Icons.verified, color: color, size: 18),
        ],
      ),
    );
  }

  /* --------------------------- CAREER PROGRESSION ------------------------ */

  Widget _buildCareerProgression() {
    return SoftCard(
      child: const Padding(
        padding: EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.bar_chart, color: Colors.teal, size: 22),
                SizedBox(width: 12),
                Text(
                  'Career Growth',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold),
                ),
              ],
            ),
            SizedBox(height: 20),
            Center(
              child: Column(
                children: [
                  Icon(Icons.auto_graph, size: 64, color: Colors.grey),
                  SizedBox(height: 16),
                  Text(
                    'Career progression analysis will be added soon',
                    style: TextStyle(
                      
                      fontSize: 16),
                    textAlign: TextAlign.center,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  /* ------------------------------ HELPERS -------------------------------- */

  Widget _careerHighlightItem(String label, String value, IconData icon) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 12),
      child: Row(
        children: [
          Icon(icon, color: Colors.teal.shade600, size: 20),
          const SizedBox(width: 12),
          Expanded(child: Text(label, style: const TextStyle(fontWeight: FontWeight.w500))),
          Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
        ],
      ),
    );
  }

  Widget _starRating(double rating) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(5, (index) {
        return Icon(
          index < rating.floor()
              ? Icons.star
              : (index < rating.ceil() ? Icons.star_half : Icons.star_border),
          color: Colors.amber,
          size: 24,
        );
      }),
    );
  }

  Color _ratingColor(double rating) {
    if (rating >= 4.5) return Colors.green;
    if (rating >= 4.0) return Colors.teal;
    if (rating >= 3.5) return Colors.orange;
    return Colors.red;
  }

  String _ratingDescription(double rating) {
    if (rating >= 4.5) return 'Outstanding performance';
    if (rating >= 4.0) return 'Excellent and impactful';
    if (rating >= 3.5) return 'Good and consistent';
    if (rating >= 3.0) return 'Acceptable - room for improvement';
    return 'Needs improvement';
  }
}

/* -------------------------- SLIVER TAB DELEGATE -------------------------- */
class _TabBarDelegate extends SliverPersistentHeaderDelegate {
  final Widget child;

  _TabBarDelegate({required this.child});

  @override
  Widget build(BuildContext context, double shrinkOffset, bool overlapsContent) {
    return child;
  }

  @override
  double get maxExtent => 68;

  @override
  double get minExtent => 68;

  @override
  bool shouldRebuild(covariant _TabBarDelegate oldDelegate) {
    // Rebuild only if the child instance changes
    return oldDelegate.child != child;
  }
}


