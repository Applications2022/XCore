package de.ruben.xcore.currency.service;

import com.mongodb.client.model.Filters;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.currency.account.type.PrivateState;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.cache2k.Cache;

import java.util.UUID;
import java.util.function.Consumer;

public class CashService{

    public CashAccount resetValue(UUID uuid){
        CashAccount cashAccount = getAccount(uuid);
        cashAccount.setValue(0.0);

        return updateCashAccount(uuid, cashAccount);
    }

    public CashAccount setValue(UUID uuid, double amount){
        CashAccount cashAccount = getAccount(uuid);
        cashAccount.setValue(amount);

        return updateCashAccount(uuid, cashAccount);
    }

    public CashAccount removeValue(UUID uuid, double amount){
        CashAccount cashAccount = getAccount(uuid);
        cashAccount.setValue(cashAccount.getValue()-amount);

        return updateCashAccount(uuid, cashAccount);
    }

    public CashAccount addValue(UUID uuid, double amount){
        CashAccount cashAccount = getAccount(uuid);
        cashAccount.setValue(cashAccount.getValue()+amount);

        return updateCashAccount(uuid, cashAccount);
    }

    public void setPrivateState(UUID uuid, PrivateState privateState, Consumer<CashAccount> callback){
        getAccountAsync(uuid, cashAccount -> {
            cashAccount.setPrivateState(privateState);
            updateCashAccount(uuid, cashAccount);
            callback.accept(cashAccount);
        });
    }

    public void resetValue(UUID uuid, Consumer<CashAccount> callback){
        getAccountAsync(uuid, cashAccount -> {
            cashAccount.setValue(1000.0);
            updateCashAccount(uuid, cashAccount);
            callback.accept(cashAccount);
        });
    }

    public void setValue(UUID uuid, double amount, Consumer<CashAccount> callback){
        getAccountAsync(uuid, cashAccount -> {
            cashAccount.setValue(amount);
            updateCashAccount(uuid, cashAccount);
            callback.accept(cashAccount);
        });
    }

    public void removeValue(UUID uuid, double amount, Consumer<CashAccount> callback){
        getAccountAsync(uuid, cashAccount -> {
            cashAccount.setValue(cashAccount.getValue()-amount);
            updateCashAccount(uuid, cashAccount);
            callback.accept(cashAccount);
        });
    }

    public void addValue(UUID uuid, double amount, Consumer<CashAccount> callback){
        getAccountAsync(uuid, cashAccount -> {
            cashAccount.setValue(cashAccount.getValue()+amount);
            updateCashAccount(uuid, cashAccount);
            callback.accept(cashAccount);
        });
    }

    public CashAccount setPrivateState(UUID uuid, PrivateState privateState){
        CashAccount cashAccount = getAccount(uuid);
        cashAccount.setPrivateState(privateState);

        return updateCashAccount(uuid, cashAccount);
    }

    public CashAccount updateCashAccount(UUID uuid, CashAccount cashAccount){
        if(Bukkit.getPlayer(uuid) != null && !getCache().containsKey(uuid)) getAccount(uuid);

        if(Bukkit.getPlayer(uuid) != null) getCache().replace(uuid, cashAccount);

        XDevApi.getInstance().getxScheduler().async(() -> {
            Document document = new Document();
            if(cashAccount.getValue() != null) document.append("value", cashAccount.getValue());
            if(cashAccount.getPrivateState() != null) document.append("privateState", cashAccount.getPrivateState().toString());

            Document updateQuery = new Document();
            updateQuery.append("$set", document);

            getMongoDBStorage().updateDocument("Data_Cash", Filters.eq("_id", uuid), updateQuery);
        });

        return cashAccount;
    }

    public Double getValue(UUID uuid) {
        return getAccount(uuid).getValue();
    }

    public CashAccount getAccount(UUID uuid) {
        if(Bukkit.getPlayer(uuid) != null && getCache().containsKey(uuid)){
            return getCache().get(uuid);
        }else{
            Document document = getMongoDBStorage().getMongoDatabase().getCollection("Data_Cash").find(new Document("_id", uuid)).first();
            CashAccount cashAccount;

            if(document != null){
                cashAccount = new CashAccount(document.getDouble("value"), PrivateState.valueOf(document.getString("privateState")));
            }else{
                cashAccount = createAccount(uuid);
            }

            if(Bukkit.getPlayer(uuid) != null) getCache().putIfAbsent(uuid, cashAccount);
            return cashAccount;
        }
    }

    public void getValueAsync(UUID uuid, Consumer<Double> callback) {
            getAccountAsync(uuid, cashAccount -> {
                callback.accept(cashAccount.getValue());
            });
    }

    public void getAccountAsync(UUID uuid, Consumer<CashAccount> callback) {
        if(Bukkit.getPlayer(uuid) != null && getCache().containsKey(uuid)){
            callback.accept(getCache().get(uuid));
        }else{
            getMongoDBStorage().getDocumentByBson("Data_Cash", new Document("_id", uuid)).thenAccept(document -> {
                CashAccount cashAccount;

                if(document != null){
                    cashAccount = new CashAccount(document.getDouble("value"), PrivateState.valueOf(document.getString("privateState")));
                }else{
                    cashAccount = createAccount(uuid);
                }

                if(Bukkit.getPlayer(uuid) != null) getCache().putIfAbsent(uuid, cashAccount);
                callback.accept(cashAccount);
            });
        }
    }

    public CashAccount createAccount(UUID uuid){
        CashAccount cashAccount = new CashAccount(1000.0, PrivateState.PRIVATE);
        Document document = new Document("_id", uuid);
        document.append("value", cashAccount.getValue());
        document.append("privateState", cashAccount.getPrivateState().toString());

        getMongoDBStorage().insertOneDocument("Data_Cash", document).thenAccept(document1 -> System.out.println(document1.toJson()));

        return cashAccount;
    }

    public void removeCacheEntry(UUID uuid){
        if(getCache().containsKey(uuid)){
            getCache().remove(uuid);
        }
    }

    private boolean existUser(UUID uuid){
        if(getCache().containsKey(uuid)){
            return true;
        }

        Document document = getMongoDBStorage().getMongoDatabase().getCollection("Data_Cash").find(new Document("_id", uuid)).first();

        return document != null;
    }

    private void existUser(UUID uuid, Consumer<Boolean>  callback){
        if(getCache().containsKey(uuid)){
            callback.accept(true);
        }

        getMongoDBStorage().getDocumentByBson("Data_Cash", new Document("_id", uuid)).thenAccept(document -> {
            if(document == null){
                callback.accept(false);
            }else{
                callback.accept(true);
            }
        });
    }


    public MongoDBStorage getMongoDBStorage(){
        return XCurrency.getInstance().getMongoDBStorage();
    }

    public Cache<UUID, CashAccount> getCache(){
        return XCurrency.getInstance().getCashAccountCache();
    }
}
