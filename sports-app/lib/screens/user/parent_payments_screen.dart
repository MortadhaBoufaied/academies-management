import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../models/payment.dart';
import '../../models/player.dart';
import '../../services/parent_service.dart';
import '../../services/payment_service.dart';
import '../../theme/app_theme.dart';

class ParentPaymentsScreen extends StatefulWidget {
  final int parentId;
  const ParentPaymentsScreen({super.key, required this.parentId});

  @override
  State<ParentPaymentsScreen> createState() => _ParentPaymentsScreenState();
}

class _ParentPaymentsScreenState extends State<ParentPaymentsScreen> {
  final ParentService _parentService = ParentService();
  final PaymentService _paymentService = PaymentService();

  bool loading = true;
  String? error;
  List<Player> children = [];
  List<Payment> payments = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      loading = true;
      error = null;
    });
    try {
      final results = await Future.wait([
        _parentService.getChildren(widget.parentId),
        _paymentService.getPaymentsForParent(widget.parentId),
      ]);
      children = results[0] as List<Player>;
      payments =
          (results[1] as List<Payment>)
            ..sort((a, b) => b.mois.compareTo(a.mois));
    } catch (e) {
      error = e.toString();
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  Future<void> _markPaid(Payment payment) async {
    if (payment.id == null) return;
    try {
      await _paymentService.markAsPaid(payment.id!);
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Payment marked as paid')));
      await _load();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Error: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final money = NumberFormat.currency(
      locale: Localizations.localeOf(context).toString(),
      symbol: 'DT',
      decimalDigits: 2,
    );
    final unpaid = payments.where((p) => !p.isPaid).toList();
    final unpaidTotal = unpaid.fold<double>(0, (sum, p) => sum + p.montant);

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: const Text('Family payments'),
        backgroundColor: Colors.transparent,
      ),
      body:
          loading
              ? const Center(
                child: CircularProgressIndicator(color: AppTheme.teal),
              )
              : RefreshIndicator(
                onRefresh: _load,
                child: ListView(
                  physics: const AlwaysScrollableScrollPhysics(),
                  padding: const EdgeInsets.all(16),
                  children: [
                    if (error != null)
                      Card(
                        color: cs.errorContainer.withOpacity(0.25),
                        child: ListTile(
                          leading: const Icon(
                            Icons.error_outline,
                            color: Colors.red,
                          ),
                          title: Text(error!),
                        ),
                      ),
                    Card(
                      child: ListTile(
                        leading: const Icon(
                          Icons.family_restroom,
                          color: AppTheme.teal,
                        ),
                        title: Text('Children (${children.length})'),
                        subtitle:
                            children.isEmpty
                                ? const Text('No children linked')
                                : Text(
                                  children
                                      .map((c) => (c.nom ?? '').trim())
                                      .where((name) => name.isNotEmpty)
                                      .take(3)
                                      .join(', '),
                                ),
                      ),
                    ),
                    const SizedBox(height: 10),
                    Card(
                      child: ListTile(
                        leading: Icon(
                          unpaid.isEmpty
                              ? Icons.verified
                              : Icons.warning_amber_rounded,
                          color: unpaid.isEmpty ? Colors.green : Colors.orange,
                        ),
                        title: const Text('Outstanding balance'),
                        subtitle: Text(money.format(unpaidTotal)),
                        trailing: Text(
                          '${unpaid.length} unpaid',
                          style: const TextStyle(fontWeight: FontWeight.w900),
                        ),
                      ),
                    ),
                    const SizedBox(height: 10),
                    if (payments.isEmpty)
                      const Card(
                        child: ListTile(
                          leading: Icon(
                            Icons.receipt_long,
                            color: AppTheme.teal,
                          ),
                          title: Text('No payment records yet'),
                          subtitle: Text(
                            'Family payment history will appear here once invoices are created.',
                          ),
                        ),
                      )
                    else
                      ...payments.map((payment) {
                        final paid = payment.isPaid;
                        final month = DateFormat(
                          'yyyy-MM',
                        ).format(payment.mois);
                        return Card(
                          child: ListTile(
                            leading: Icon(
                              paid ? Icons.check_circle : Icons.pending,
                              color: paid ? Colors.green : Colors.orange,
                            ),
                            title: Text(money.format(payment.montant)),
                            subtitle: Text('Month: $month'),
                            trailing:
                                paid
                                    ? const Text(
                                      'Paid',
                                      style: TextStyle(
                                        color: Colors.green,
                                        fontWeight: FontWeight.w900,
                                      ),
                                    )
                                    : TextButton(
                                      onPressed: () => _markPaid(payment),
                                      child: const Text('Mark paid'),
                                    ),
                          ),
                        );
                      }),
                  ],
                ),
              ),
    );
  }
}


