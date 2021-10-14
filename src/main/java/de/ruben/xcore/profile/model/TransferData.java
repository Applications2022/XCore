package de.ruben.xcore.profile.model;

public class TransferData {
    private long transferCount;
    private double transferredAmount;

    public TransferData(long transferCount, double transferredAmount) {
        this.transferCount = transferCount;
        this.transferredAmount = transferredAmount;
    }

    public TransferData() {
    }

    public long getTransferCount() {
        return transferCount;
    }

    public void setTransferCount(long transferCount) {
        this.transferCount = transferCount;
    }

    public double getTransferredAmount() {
        return transferredAmount;
    }

    public void setTransferredAmount(double transferredAmount) {
        this.transferredAmount = transferredAmount;
    }
}
