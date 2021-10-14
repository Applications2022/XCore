package de.ruben.xcore.currency.account.type;

import de.ruben.xdevapi.XDevApi;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Transaction {

    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");

    private boolean positive;
    private double amount;
    private UUID transactionMadeBy;
    private Date date;

    public Transaction(boolean positive, double amount, UUID transactionMadeBy, Date date) {
        this.positive = positive;
        this.amount = amount;
        this.transactionMadeBy = transactionMadeBy;
        this.date = date;
    }

    public Transaction() {
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public UUID getTransactionMadeBy() {
        return transactionMadeBy;
    }

    public void setTransactionMadeBy(UUID transactionMadeBy) {
        this.transactionMadeBy = transactionMadeBy;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getTransactionString(UUID displayed){
        String transactionMadeBy = displayed.toString().equals(getTransactionMadeBy().toString()) ? "Du" : Bukkit.getOfflinePlayer(getTransactionMadeBy()).getName();
        String amountString = positive ? "§a+"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) : "§c-"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount);

        long seconds = (System.currentTimeMillis()-getDate().getTime())/1000;

        String timeString = XDevApi.getInstance().getxUtil().getGlobal().getTimeUtil().convertSecondsHM((int) seconds);

        timeString = timeString.startsWith(" ") ? timeString : " "+timeString;
        return amountString+" §8("+transactionMadeBy+" vor"+timeString+")";
    }

    public Document toDocument(){
        Document document = new Document();
        document.append("positive", positive);
        if(amount != 0) document.append("amount", amount);
        if(transactionMadeBy != null) document.append("transactionMadeBy", transactionMadeBy);
        if(date != null) document.append("date", date);

        return document;
    }

    public Document toDocument(Transaction transaction){
        Document document = new Document();
        document.append("positive", transaction.isPositive());
        if(transaction.getAmount()!= 0) document.append("amount", transaction.getAmount());
        if(transaction.getTransactionMadeBy() != null) document.append("transactionMadeBy", transaction.getTransactionMadeBy());
        if(transaction.getDate() != null) document.append("date", transaction.getDate());

        return document;
    }

    public Transaction fromDocument(Document document){
        return new Transaction(document.getBoolean("positive"), document.getDouble("amount"), document.get("transactionMadeBy", UUID.class), document.getDate("date"));
    }
}
