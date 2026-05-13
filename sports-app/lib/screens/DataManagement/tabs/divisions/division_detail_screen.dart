import 'package:flutter/material.dart';
import 'package:moez_project/components/NavigationLink.dart';

import 'package:moez_project/screens/DataManagement/tabs/Parents/parents_screen.dart';
import 'package:moez_project/screens/DataManagement/tabs/players/footballers_screen.dart';
import 'package:moez_project/screens/DataManagement/tabs/monthly_activities/activities_screen.dart';
import 'package:moez_project/screens/DataManagement/tabs/trainers/trainers_screen.dart';

import '../../../../controllers/divisionsController.dart';
import '../../../../models/division.dart';
import '../division_payments_tab.dart';

class DivisionDetailScreen extends StatefulWidget {
  final int divisionId;
  const DivisionDetailScreen({Key? key, required this.divisionId}) : super(key: key);

  @override
  State<DivisionDetailScreen> createState() => _DivisionDetailScreenState();
}

class _DivisionDetailScreenState extends State<DivisionDetailScreen> {
  final DivisionController _controller = DivisionController();
  Division? division;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    _load();
    _controller.addListener(_onChanged);
  }

  @override
  void dispose() {
    _controller.removeListener(_onChanged);
    super.dispose();
  }

  void _onChanged() {
    if (!mounted) return;
    setState(() {});
  }

  Future<void> _load() async {
    setState(() => loading = true);
    try {
      final d = await _controller.getDivisionById(widget.divisionId);
      if (!mounted) return;
      setState(() {
        division = d;
      });
    } catch (e) {
      // TODO: handle error (snackbar/toast/log)
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    // We have 5 tabs: Activities, Trainers, Footballers, Payments, Parents
    return DefaultTabController(
      length: 5,
      child: Scaffold(
        appBar: PreferredSize(
          preferredSize: const Size.fromHeight(kToolbarHeight + 2 + kTextTabBarHeight),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TopNavigationBar(
                title: '',
                onSearchTap: () => Navigator.pushNamed(context, '/global-search'),
                showLogo: true,
                onNotificationTap: () => Navigator.pushNamed(context, '/notifications'), // <-- add this

              ),
              Material(
                color: Theme.of(context).colorScheme.surface,
                child: const TabBar(
                  indicatorColor: Colors.white,
                  labelColor: Colors.white,
                  unselectedLabelColor: Colors.white70,
                  isScrollable: false,
                  tabs: [
                    Tab(text: 'Activities',   icon: Icon(Icons.event_note)),
                    Tab(text: 'Trainers',     icon: Icon(Icons.sports)),
                    Tab(text: 'Footballers',  icon: Icon(Icons.people_alt)),
                    Tab(text: 'Payments',     icon: Icon(Icons.payment)),
                    Tab(text: 'Parents',      icon: Icon(Icons.family_restroom)),
                  ],
                ),
              ),
            ],
          ),
        ),

        body: Column(
          children: [
            // Header card
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 12, 12, 6),
              child: Material(
                elevation: 2,
                borderRadius: BorderRadius.circular(14),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    color: Theme.of(context).colorScheme.surface,
                  ),
                  child: loading
                      ? const SizedBox(
                    height: 88,
                    child: Center(child: CircularProgressIndicator()),
                  )
                      : Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      // Avatar / icon
                      CircleAvatar(
                        radius: 34,
                        backgroundColor: Colors.transparent,
                        child: Text(
                          division?.nom.isNotEmpty == true
                              ? division!.nom[0].toUpperCase()
                              : '?',
                          style: TextStyle(
                            fontSize: 26,
                            color: Colors.teal.shade900,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Hero(
                              tag: 'division-title-${division?.id ?? widget.divisionId}',
                              child: const Material(
                                color: Colors.transparent,
                                child: SizedBox(), // Hero placeholder if needed
                              ),
                            ),
                            Text(
                              division?.nom ?? 'Division',
                              style: const TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                            const SizedBox(height: 6),
                            Row(
                              children: [
                                if (division?.categorie != null && division!.categorie!.isNotEmpty)
                                  Chip(
                                    label: Text(division!.categorie!),
                                    backgroundColor: Colors.transparent,
                                    labelStyle: const TextStyle(color: Colors.blue),
                                  ),
                                const SizedBox(width: 8),
                                if (division?.playersCount != null)
                                  Text('${division!.playersCount} players'),
                                const SizedBox(width: 8),
                                if (division?.coachesCount != null)
                                  Text('${division!.coachesCount} coaches'),
                              ],
                            ),
                          ],
                        ),
                      ),

                      // Actions
                      Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            onPressed: () async {
                              await _load();
                              if (!mounted) return;
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text("Refreshed")),
                              );
                            },
                            icon: const Icon(Icons.refresh),
                          ),
                          const SizedBox(height: 2),
                          PopupMenuButton<String>(
                            onSelected: (value) {
                              // handle actions like edit, delete, etc.
                            },
                            itemBuilder: (_) => const [
                              PopupMenuItem(value: 'edit', child: Text('Edit')),
                              PopupMenuItem(value: 'delete', child: Text('Delete')),
                            ],
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),

            Expanded(
              child: TabBarView(
                children: [
                  ActivitiesScreen(),
                  TrainersScreen(divisionId: widget.divisionId.toString()),
                  InlineFootballersList(divisionId: widget.divisionId),
                  DivisionPaymentsTab(divisionId: widget.divisionId),
                  ParentsScreen(divisionId: widget.divisionId),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}


