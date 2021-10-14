package de.ruben.xcore.currency.account;

import de.ruben.xcore.currency.account.type.PrivateState;

public class CashAccount {
    private Double value;
    private PrivateState privateState;

    public CashAccount(Double value, PrivateState privateState) {
        this.value = value;
        this.privateState = privateState;
    }

    public CashAccount() {
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public PrivateState getPrivateState() {
        return privateState;
    }

    public void setPrivateState(PrivateState privateState) {
        this.privateState = privateState;
    }

    @Override
    public String toString() {
        return "CashAccount{" +
                "value=" + value +
                ", privateState=" + privateState +
                '}';
    }
}
