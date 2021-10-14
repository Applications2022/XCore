package de.ruben.xcore.currency.account;


import de.ruben.xcore.currency.account.type.Transaction;

import java.util.List;
import java.util.UUID;

public class BankAccount {
    private Double value;
    private List<Transaction> transactions;
    private List<UUID> accessGrantedAccounts;
    private List<UUID> accessGrantedPlayers;
    private boolean frozen;

    public BankAccount(Double value, List<Transaction> transactions, List<UUID> accessGrantedAccounts, List<UUID> accessGrantedPlayers, boolean frozen) {
        this.value = value;
        this.transactions = transactions;
        this.accessGrantedAccounts = accessGrantedAccounts;
        this.accessGrantedPlayers = accessGrantedPlayers;
        this.frozen = frozen;
    }

    public BankAccount() {
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<UUID> getAccessGrantedAccounts() {
        return accessGrantedAccounts;
    }

    public void setAccessGrantedAccounts(List<UUID> accessGrantedAccounts) {
        this.accessGrantedAccounts = accessGrantedAccounts;
    }

    public List<UUID> getAccessGrantedPlayers() {
        return accessGrantedPlayers;
    }

    public void setAccessGrantedPlayers(List<UUID> accessGrantedPlayers) {
        this.accessGrantedPlayers = accessGrantedPlayers;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
}
