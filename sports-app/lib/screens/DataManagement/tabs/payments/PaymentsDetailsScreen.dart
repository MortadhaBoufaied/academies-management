import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../../components/app_background.dart';
import '../../../../components/ui_kit.dart';
import '../../../../controllers/payment_controller.dart';
import '../../../../models/payment.dart';
import '../../../../theme/app_theme.dart';

class PaymentDetailsScreen extends StatefulWidget {
  final int paymentId;
  const PaymentDetailsScreen({super.key, required this.paymentId});

  @override
  State<PaymentDetailsScreen> createState() => _PaymentDetailsScreenState();
}

class _PaymentDetailsScreenState extends State<PaymentDetailsScreen> {
  final PaymentController _controller = PaymentController();
  Payment? _payment;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _loading = true);
    await _controller.getAllPayments();
    try {
      _payment = _controller.payments.firstWhere((p) => p.id == widget.paymentId);
    } catch (_) {
      _payment = null;
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
    backgroundColor: Colors.transparent,title: const Text('Payment Details')),
        body: _loading
            ? const Center(child: CircularProgressIndicator(color: AppTheme.teal))
            : _payment == null
            ? const Center(child: Text('Payment not found', style: TextStyle(color: Colors.white)))
            : ListView(
          padding: const EdgeInsets.only(bottom: 24),
          children: [
            SectionTitle(
              title: 'Payment #${_payment!.id}',
              subtitle: DateFormat('yyyy-MM').format(_payment!.mois),
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: SoftCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _kv('Amount', '${_payment!.montant.toStringAsFixed(2)} DT'),
                    _kv('Paid', _payment!.isPaid ? 'Yes' : 'No'),
                    _kv('PlayerId', _payment!.playerId?.toString() ?? '-'),
                    _kv('ParentId', _payment!.parentId?.toString() ?? '-'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
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


