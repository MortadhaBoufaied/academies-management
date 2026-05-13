import 'package:flutter/material.dart';

import '../../components/app_background.dart';
import '../../components/ui_kit.dart';
import '../../data/admin/admin_module_catalog.dart';
import '../../models/admin/admin_module.dart';
import 'widgets/admin_module_widgets.dart';

class AdminModuleScreen extends StatelessWidget {
  final String moduleKey;
  final AdminWebRole role;
  final bool embedded;

  const AdminModuleScreen({
    super.key,
    required this.moduleKey,
    required this.role,
    this.embedded = false,
  });

  @override
  Widget build(BuildContext context) {
    final module = moduleFor(moduleKey, role);
    final modules = modulesForRole(role)
        .where((item) => !(role == AdminWebRole.superAdmin && item.key == 'dashboard'))
        .toList(growable: false);
    final body = LayoutBuilder(
      builder: (context, constraints) {
        final wide = constraints.maxWidth >= 840;
        final content = _content(context, module, modules, wide);
        return wide
            ? Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SizedBox(
                    width: 270,
                    child: Padding(
                      padding: const EdgeInsets.all(14),
                      child: AdminModuleRail(
                        active: module,
                        modules: modules,
                        vertical: true,
                        onSelected: (selected) => _replace(context, selected),
                      ),
                    ),
                  ),
                  Expanded(child: content),
                ],
              )
            : content;
      },
    );

    if (embedded) return body;
    return AppBackground(
      child: Scaffold(
        backgroundColor: Colors.transparent,
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          elevation: 0,
          title: Text(module.title),
        ),
        body: body,
      ),
    );
  }

  Widget _content(BuildContext context, AdminModuleSpec module, List<AdminModuleSpec> modules, bool wide) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(14, 14, 14, 28),
      children: [
        AdminModuleHero(module: module),
        if (!wide) ...[
          const SizedBox(height: 12),
          AdminModuleRail(active: module, modules: modules, onSelected: (selected) => _replace(context, selected)),
        ],
        const SizedBox(height: 12),
        const SizedBox(height: 12),
        AdminInfoBlock(title: 'Main capabilities', icon: Icons.check_circle_rounded, items: module.capabilities),
        AdminActionsBlock(actions: module.actions),
      ],
    );
  }

  Widget _summaryCard(BuildContext context, AdminModuleSpec module, _Summary item) {
    final cs = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: cs.surface.withValues(alpha: 0.78),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: cs.outlineVariant.withValues(alpha: 0.50)),
      ),
      child: Row(
        children: [
          Icon(item.icon, color: module.color),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(item.value, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
                Text(item.label, maxLines: 1, overflow: TextOverflow.ellipsis, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: cs.onSurfaceVariant)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _replace(BuildContext context, AdminModuleSpec selected) {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => AdminModuleScreen(moduleKey: selected.key, role: role),
      ),
    );
  }
}

class _Summary {
  final String label;
  final String value;
  final IconData icon;
  const _Summary(this.label, this.value, this.icon);
}


