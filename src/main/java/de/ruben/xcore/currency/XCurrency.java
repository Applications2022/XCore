package de.ruben.xcore.currency;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.currency.command.AdminCashCommands;
import de.ruben.xcore.currency.command.PlayerBankCommands;
import de.ruben.xcore.currency.command.PlayerCashCommands;
import de.ruben.xcore.currency.listener.PlayerListener;
import de.ruben.xcore.currency.service.BankService;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.codecs.configuration.CodecRegistries;
import org.bukkit.Bukkit;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.UUID;

public class XCurrency implements SubSystem {

    private static XCurrency instance;

    private MongoDBStorage mongoDBStorage;


    private CashService cashService;

    private BankService bankService;

    private Cache<UUID, BankAccount> bankAccountCache;

    private Cache<UUID, CashAccount> cashAccountCache;

    @Override
    public void onEnable(){
        instance = this;

//        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), "localhost", "Currency", 27017, MongoClientOptions.builder().codecRegistry(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new TransactionCodec()))).build());
        this.mongoDBStorage = new MongoDBStorage(XDevApi.getInstance(), 10, "localhost", "Currency", 27017, "currency", "wrgO4FTbV6UyLwtMzfsp", MongoClientOptions.builder().codecRegistry(CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new TransactionCodec()))).build());

        mongoDBStorage.connect();

        bankAccountCache = Cache2kBuilder.of(UUID.class, BankAccount.class)
                .name("bankCache")
                .eternal(true)
                .entryCapacity(150)
                .build();

        this.cashAccountCache = Cache2kBuilder.of(UUID.class, CashAccount.class)
                .name("cashCache")
                .eternal(true)
                .entryCapacity(150)
                .build();

        this.cashService = new CashService();
        this.bankService = new BankService();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(), XCore.getInstance());

        XDevApi.getInstance().getMessageService().addMessageWithPrefix("offlinePlayer", "&cFehler: &7Dieser Spieler ist offline!");

        XCore.getInstance().getCommand("cash").setExecutor(new PlayerCashCommands());
        XCore.getInstance().getCommand("pay").setExecutor(new PlayerCashCommands());
        XCore.getInstance().getCommand("eco").setExecutor(new AdminCashCommands());
        XCore.getInstance().getCommand("bank").setExecutor(new PlayerBankCommands());
    }

    @Override
    public void onDisable(){
        getMongoDBStorage().disconnect();
        getBankAccountCache().clearAndClose();
        getCashAccountCache().clearAndClose();
    }

    public static XCurrency getInstance() {
        return instance;
    }

    public MongoDBStorage getMongoDBStorage() {
        return mongoDBStorage;
    }

    public Cache<UUID, BankAccount> getBankAccountCache() {
        return bankAccountCache;
    }

    public Cache<UUID, CashAccount> getCashAccountCache() {
        return cashAccountCache;
    }

    public CashService getCashService() {
        return cashService;
    }

    public BankService getBankService() {
        return bankService;
    }
}
