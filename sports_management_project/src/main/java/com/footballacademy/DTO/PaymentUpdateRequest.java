package com.footballacademy.DTO;

import java.util.List;

public
class PaymentUpdateRequest {
    private List<Long> paymentIds;
    private boolean isPaid;
    public PaymentUpdateRequest() {
    }
    public PaymentUpdateRequest(List<Long> paymentIds, boolean isPaid) {
        this.paymentIds = paymentIds;
        this.isPaid = isPaid;
    }
    public List<Long> getPaymentIds() {
        return paymentIds;
    }
    public void setPaymentIds(List<Long> paymentIds) {
        this.paymentIds = paymentIds;
    }
    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}
