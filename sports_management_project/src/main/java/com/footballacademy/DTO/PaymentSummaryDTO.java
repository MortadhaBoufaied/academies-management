package com.footballacademy.DTO;

public
class PaymentSummaryDTO {
    private int totalPaid;
    private int totalUnpaid;
    public int getUnpaidPlayers() {
        return unpaidPlayers;
    }
    public void setUnpaidPlayers(int unpaidPlayers) {
        this.unpaidPlayers = unpaidPlayers;
    }
    public int getTotalPaid() {
        return totalPaid;
    }
    public void setTotalPaid(int totalPaid) {
        this.totalPaid = totalPaid;
    }
    public int getTotalUnpaid() {
        return totalUnpaid;
    }
    public void setTotalUnpaid(int totalUnpaid) {
        this.totalUnpaid = totalUnpaid;
    }
    private int unpaidPlayers;
}
