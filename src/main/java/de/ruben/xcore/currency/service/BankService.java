package de.ruben.xcore.currency.service;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xcore.currency.account.type.Transaction;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.cache2k.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BankService {

    public BankAccount setValue(UUID uuid, double amount){
        BankAccount bankAccount = getAccount(uuid);
        bankAccount.setValue(amount);

        return updateBankAccount(uuid, bankAccount);
    }

    public BankAccount resetValue(UUID uuid){
        BankAccount bankAccount = getAccount(uuid);
        bankAccount.setValue(0.0);

        return updateBankAccount(uuid, bankAccount);
    }

    public BankAccount removeValue(UUID uuid, double amount){
        BankAccount bankAccount = getAccount(uuid);
        bankAccount.setValue(bankAccount.getValue()-amount);
        return  updateBankAccount(uuid, bankAccount);
    }

    public BankAccount addValue(UUID uuid, double amount){
        BankAccount bankAccount = getAccount(uuid);
        bankAccount.setValue(bankAccount.getValue()+amount);
        return  updateBankAccount(uuid, bankAccount);
    }

    public void setValue(UUID uuid, double amount, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            bankAccount.setValue(amount);

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void resetValue(UUID uuid, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            bankAccount.setValue(0.0);

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void removeValue(UUID uuid, double amount, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            bankAccount.setValue(bankAccount.getValue()-amount);
            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void addValue(UUID uuid, double amount, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            bankAccount.setValue(bankAccount.getValue()+amount);
            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public BankAccount addAccessGrantedPlayer(UUID uuid, UUID playerUUID){
        BankAccount bankAccount = getAccount(uuid);
        List<UUID> accessGrantedPlayers = bankAccount.getAccessGrantedPlayers();

        if(!accessGrantedPlayers.contains(playerUUID)){
            accessGrantedPlayers.add(playerUUID);
        }

        bankAccount.setAccessGrantedPlayers(accessGrantedPlayers);

        return updateBankAccount(uuid, bankAccount);
    }

    public BankAccount removeAccessGrantedPlayer(UUID uuid, UUID playerUUID){
        BankAccount bankAccount = getAccount(uuid);
        List<UUID> accessGrantedPlayers = bankAccount.getAccessGrantedPlayers();

        if(accessGrantedPlayers.contains(playerUUID)){
            accessGrantedPlayers.remove(playerUUID);
        }

        return updateBankAccount(uuid, bankAccount);
    }

    public void addAccessGrantedPlayer(UUID uuid, UUID playerUUID, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<UUID> accessGrantedPlayers = bankAccount.getAccessGrantedPlayers();

            if(!accessGrantedPlayers.contains(playerUUID)){
                accessGrantedPlayers.add(playerUUID);
            }

            bankAccount.setAccessGrantedPlayers(accessGrantedPlayers);

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void removeAccessGrantedPlayer(UUID uuid, UUID playerUUID, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<UUID> accessGrantedPlayers = bankAccount.getAccessGrantedPlayers();

            accessGrantedPlayers.remove(playerUUID);

            bankAccount.setAccessGrantedPlayers(accessGrantedPlayers);

            System.out.println("Ausgeführt! : "+bankAccount.getAccessGrantedPlayers());

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public BankAccount addAccessGrantedAccount(UUID uuid, UUID accountUUID){
        BankAccount bankAccount = getAccount(uuid);
        List<UUID> accessGrantedAccounts = bankAccount.getAccessGrantedAccounts();

        if(!accessGrantedAccounts.contains(accountUUID)){
            accessGrantedAccounts.add(accountUUID);
        }

        bankAccount.setAccessGrantedAccounts(accessGrantedAccounts);

        return updateBankAccount(uuid, bankAccount);
    }

    public BankAccount removeAccessGrantedAccount(UUID uuid, UUID accountUUID){
        BankAccount bankAccount = getAccount(uuid);
        List<UUID> accessGrantedAccounts = bankAccount.getAccessGrantedAccounts();

        if(accessGrantedAccounts.contains(accountUUID)){
            accessGrantedAccounts.remove(accountUUID);
        }

        bankAccount.setAccessGrantedAccounts(accessGrantedAccounts);

        System.out.println("Ausgeführt! : "+bankAccount.getAccessGrantedAccounts());

        return updateBankAccount(uuid, bankAccount);
    }

    public void addAccessGrantedAccount(UUID uuid, UUID accountUUID, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<UUID> accessGrantedAccounts = bankAccount.getAccessGrantedAccounts();

            if(!accessGrantedAccounts.contains(accountUUID)){
                accessGrantedAccounts.add(accountUUID);
            }

            bankAccount.setAccessGrantedAccounts(accessGrantedAccounts);

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void removeAccessGrantedAccount(UUID uuid, UUID accountUUID, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<UUID> accessGrantedAccounts = bankAccount.getAccessGrantedAccounts();

            accessGrantedAccounts.remove(accountUUID);

            bankAccount.setAccessGrantedAccounts(accessGrantedAccounts);
            System.out.println("Ausgeführt! : "+bankAccount.getAccessGrantedAccounts());


            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public BankAccount addTransaction(UUID uuid, Transaction transaction){
        BankAccount bankAccount = getAccount(uuid);
        List<Transaction> transactions = bankAccount.getTransactions();

        transactions.add(transaction);

        bankAccount.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));

        return updateBankAccount(uuid, bankAccount);
    }

    public BankAccount removeTransaction(UUID uuid, Transaction transaction){
        BankAccount bankAccount = getAccount(uuid);
        List<Transaction> transactions = bankAccount.getTransactions();

        if(transactions.contains(transaction)){
            transactions.remove(transaction);
        }

        bankAccount.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));

        return updateBankAccount(uuid, bankAccount);
    }

    public void addTransaction(UUID uuid, Transaction transaction, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<Transaction> transactions = bankAccount.getTransactions();

            transactions.add(transaction);

            bankAccount.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));


             callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public void removeTransaction(UUID uuid, Transaction transaction, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            List<Transaction> transactions = bankAccount.getTransactions();

            if(transactions.contains(transaction)){
                transactions.remove(transaction);
            }

            bankAccount.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));

            callback.accept(updateBankAccount(uuid, bankAccount));
        });
    }

    public List<UUID> getAccessGrantedPlayers(UUID uuid){
        return getAccount(uuid).getAccessGrantedPlayers();
    }

    public void getAccessGrantedPlayers(UUID uuid, Consumer<List<UUID>> callback){
        getAccountAsync(uuid, bankAccount -> {
            callback.accept(bankAccount.getAccessGrantedPlayers());
        });
    }

    public List<UUID> getAccessGrantedAccounts(UUID uuid){
        return getAccount(uuid).getAccessGrantedAccounts();
    }

    public void getAccessGrantedAccounts(UUID uuid, Consumer<List<UUID>> callback){
        getAccountAsync(uuid, bankAccount -> {
            callback.accept(bankAccount.getAccessGrantedAccounts());
        });
    }

    public List<Transaction> getTransactions(UUID uuid){
        return getAccount(uuid).getTransactions();
    }

    public void getTransactions(UUID uuid, Consumer<List<Transaction>> callback){
        getAccountAsync(uuid, bankAccount -> {
            callback.accept(bankAccount.getTransactions());
        });
    }

    public Double getValue(UUID uuid){
        return getAccount(uuid).getValue();
    }

    public void getValue(UUID uuid, Consumer<Double> callback){
        getAccountAsync(uuid, bankAccount -> {
            callback.accept(bankAccount.getValue());
        });
    }

    public boolean getIsFrozen(UUID uuid){
        BankAccount bankAccount = getAccount(uuid);
        return bankAccount.isFrozen();
    }

    public void getIsFrozen(UUID uuid, Consumer<Boolean> callback){
        getAccountAsync(uuid, bankAccount -> {
            callback.accept(bankAccount.isFrozen());
        });
    }

    public BankAccount setFrozen(UUID uuid, Boolean frozen){
        BankAccount bankAccount = getAccount(uuid);
        bankAccount.setFrozen(frozen);
        return updateBankAccount(uuid, bankAccount);
    }

    public void setFrozen(UUID uuid, Boolean frozen, Consumer<BankAccount> callback){
        getAccountAsync(uuid, bankAccount -> {
            bankAccount.setFrozen(frozen);
            updateBankAccount(uuid, bankAccount, bankAccount1 -> {
                callback.accept(bankAccount1);
            });
        });
    }

    public BankAccount updateBankAccount(UUID uuid, BankAccount bankAccount){

        putInCache(uuid, bankAccount);

        XDevApi.getInstance().getxScheduler().async(() -> {

            Document document = new Document();
            if(bankAccount.getValue() != null) document.append("value", bankAccount.getValue());

            if(bankAccount.getTransactions() != null){
                List<Document> docList = bankAccount.getTransactions().stream().map(Transaction::toDocument).collect(Collectors.toList());
                document.append("transactions", docList);
            };
            if(bankAccount.getAccessGrantedAccounts() != null) document.append("accessGrantedAccounts", bankAccount.getAccessGrantedAccounts());
            if(bankAccount.getAccessGrantedPlayers() != null) document.append("accessGrantedPlayers", bankAccount.getAccessGrantedPlayers());

            document.append("frozen", bankAccount.isFrozen());

            Document updateQuery = new Document();
            updateQuery.append("$set", document);

            getMongoDBStorage().getMongoDatabase().getCollection("Data_Bank").findOneAndUpdate( Filters.eq("_id", uuid), updateQuery);
        });

        return bankAccount;
    }

    public BankAccount updateBankAccount(UUID uuid, BankAccount bankAccount, Consumer<BankAccount> callback){

        putInCache(uuid, bankAccount);

        XDevApi.getInstance().getxScheduler().async(() -> {

            Document document = new Document();
            if(bankAccount.getValue() != null) document.append("value", bankAccount.getValue());

            if(bankAccount.getTransactions() != null){
                List<Document> docList = bankAccount.getTransactions().stream().map(Transaction::toDocument).collect(Collectors.toList());
                document.append("transactions", docList);
            };
            if(bankAccount.getAccessGrantedAccounts() != null) document.append("accessGrantedAccounts", bankAccount.getAccessGrantedAccounts());
            if(bankAccount.getAccessGrantedPlayers() != null) document.append("accessGrantedPlayers", bankAccount.getAccessGrantedPlayers());

            document.append("frozen", bankAccount.isFrozen());

            Document updateQuery = new Document();
            updateQuery.append("$set", document);

            getMongoDBStorage().updateDocument("Data_Bank", Filters.eq("_id", uuid), updateQuery);

            callback.accept(bankAccount);
        });

        return bankAccount;
    }



    public BankAccount getAccount(UUID uuid){
        if(getCache().containsKey(uuid)){
            return getCache().get(uuid);
        }else{
            Document document = getMongoDBStorage().getMongoDatabase().getCollection("Data_Bank").find(new Document("_id", uuid)).first();
            BankAccount bankAccount;

            if(document == null){
                bankAccount = createAccount(uuid);
            }else{
                List<Document> docList = document.getList("transactions", Document.class);

                List<Transaction> transactions = docList.stream().map(document1 -> new Transaction().fromDocument(document1)).collect(Collectors.toList());

                bankAccount = new BankAccount(document.getDouble("value"), transactions, document.getList("accessGrantedAccounts", UUID.class), document.getList("accessGrantedPlayers", UUID.class), document.getBoolean("frozen"));
            }

            putInCache(uuid, bankAccount);

            return bankAccount;
        }
    }

    public void getAccountAsync(UUID uuid, Consumer<BankAccount> callback){
        if(getCache().containsKey(uuid)){
            callback.accept(getCache().get(uuid));
        }else{
            getMongoDBStorage().getDocumentByBson("Data_Bank", new Document("_id", uuid))
                    .thenAccept(document -> {
                        BankAccount bankAccount;

                        if(document == null){
                            bankAccount = createAccount(uuid);
                        }else{
                            List<Document> docList = document.getList("transactions", Document.class);

                            List<Transaction> transactions = docList.stream().map(document1 -> new Transaction().fromDocument(document1)).collect(Collectors.toList());

                            bankAccount = new BankAccount(document.getDouble("value"), transactions, document.getList("accessGrantedAccounts", UUID.class), document.getList("accessGrantedPlayers", UUID.class), document.getBoolean("frozen"));
                        }

                        putInCache(uuid, bankAccount);

                        callback.accept(bankAccount);
                    });

        }
    }

    public void existUser(UUID uuid, BiConsumer<BankAccount, Boolean> callback){
        if(getCache().containsKey(uuid)){
            callback.accept(getCache().get(uuid), true);
        }else{
            XDevApi.getInstance().getxScheduler().async(() -> getMongoDBStorage().getDocumentByBson("Data_Bank", new Document("_id", uuid)).thenAccept(document -> {
                if(document == null){
                    callback.accept(null, false);
                }else{

                    List<Document> docList = document.getList("transactions", Document.class);

                    List<Transaction> transactions = docList.stream().map(document1 -> new Transaction().fromDocument(document1)).collect(Collectors.toList());

                    BankAccount bankAccount = new BankAccount(document.getDouble("value"), transactions, document.getList("accessGrantedAccounts", UUID.class), document.getList("accessGrantedPlayers", UUID.class), document.getBoolean("frozen"));

                    callback.accept(bankAccount, true);
                }
            }));
        }
    }

    public void putInCache(UUID uuid, BankAccount bankAccount){
        if(Bukkit.getPlayer(uuid) == null) return;

        if(getCache().containsKey(uuid)){
            getCache().replace(uuid, bankAccount);
        }else{
            getCache().putIfAbsent(uuid, bankAccount);
        }
    }

    public void removeCacheEntry(UUID uuid){
        if(getCache().containsKey(uuid)){
            getCache().remove(uuid);
        }
    }

    public BankAccount createAccount(UUID uuid){
        BankAccount bankAccount = new BankAccount(0.0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false);

        Document document = new Document("_id", uuid);
        document.append("value",bankAccount.getValue());
        document.append("transactions", bankAccount.getTransactions());
        document.append("accessGrantedAccounts", bankAccount.getAccessGrantedAccounts());
        document.append("accessGrantedPlayers", bankAccount.getAccessGrantedPlayers());
        document.append("frozen", bankAccount.isFrozen());

        getMongoDBStorage().insertOneDocument("Data_Bank", document);

        return bankAccount;
    }

    public MongoDBStorage getMongoDBStorage(){
        return XCurrency.getInstance().getMongoDBStorage();
    }

    public Cache<UUID, BankAccount> getCache(){
        return XCurrency.getInstance().getBankAccountCache();
    }
}
