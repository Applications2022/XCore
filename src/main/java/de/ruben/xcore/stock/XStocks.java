package de.ruben.xcore.stock;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.stock.command.AktienCommand;
import de.ruben.xcore.stock.gui.StockGui;
import de.ruben.xcore.stock.listener.JoinLeaveListener;
import de.ruben.xcore.stock.model.HoldingPlayer;
import de.ruben.xcore.stock.model.StockContainer;
import de.ruben.xcore.stock.model.StockType;
import de.ruben.xcore.stock.service.StockContainerFetcher;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import org.bson.codecs.configuration.CodecRegistries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class XStocks implements SubSystem {

    public static String[] standartQuotes = new String[]{
            "AMZN", "TSLA", "AAPL", "PYPL", "MSFT", "DTE.DE", "SAP", "VOD", "GOOGL", "LHA.DE", "RWE.DE", "ENR.DE", "BAYN.DE", "TMV.DE", "PSM.DE",
            "BNTX", "CABGY", "DBK.DE", "JNJ", "USE.HA", "PAH3.DE", "ALV.DE", "DAI.DE", "VOW3.DE", "GTLB", "INTC", "AMD", "ORCL", "BTC-USD", "ETH-USD", "BNB-USD", "USDT-USD", "ADA-USD", "DOT1-USD", "DOGE-USD", "LTC-USD", "XLM-USD", "TRX-USD", "XTZ-USD", "XMR-USD", "EOS-USD",
            "LRC-USD", "MIOTA-USD", "ZEC-USD", "BAT-USD", "QTUM-USD", "DASH-USD", "WAVES-USD", "XEM-USD", "ENJ-USD", "MLN-USD", "SOL1-USD", "VET-EUR", "MATIC-USD",
            "UNI3-USD", "ICP1-USD"
    };

    private double currentUSDEURExchange;

    private DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());

    private DecimalFormat stockFormat;

    private Cache<String, StockContainer> standartStockCache;

    private Cache<String, StockContainer> recentStockCache;

    private Cache<UUID, HoldingPlayer> holdingPlayerCache;

    private Cache<UUID, ItemStack> headsCache;

    private static XStocks instance;

    public XStocks(){
        otherSymbols.setDecimalSeparator(',');
        otherSymbols.setGroupingSeparator('.');
    }

    @Override
    public void onEnable() {
        this.instance = this;

        this.standartStockCache = Cache2kBuilder.of(String.class, StockContainer.class)
                .name("standartStockCache")
                .entryCapacity(150)
                .eternal(true)
                .build();


        this.recentStockCache = Cache2kBuilder.of(String.class, StockContainer.class)
                .name("recentStockCache")
                .entryCapacity(150000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        this.holdingPlayerCache = Cache2kBuilder.of(UUID.class, HoldingPlayer.class)
                .name("holdingPlayerCache")
                .entryCapacity(150)
                .eternal(true)
                .build();

        this.headsCache = Cache2kBuilder.of(UUID.class, ItemStack.class)
                .name("headsCache")
                .entryCapacity(150)
                .eternal(true)
                .build();

        this.stockFormat = new DecimalFormat("###,###.#####", otherSymbols);

        Bukkit.getPluginManager().registerEvents(new JoinLeaveListener(), XCore.getInstance());

        XCore.getInstance().getCommand("aktien").setExecutor(new AktienCommand());

        XDevApi.getInstance().getxScheduler().asyncInterval(() -> {
            for (String standartQuote : standartQuotes) {
                if(standartStockCache.isClosed()) {
                    standartStockCache = Cache2kBuilder.of(String.class, StockContainer.class)
                            .name("standartStockCache")
                            .entryCapacity(150)
                            .eternal(true)
                            .build();
                }
                    StockContainerFetcher.fetchContainer(standartQuote, stockContainer -> {

                            if (getStandartStockCache().containsKey(stockContainer.getSymbol())) {
                                getStandartStockCache().replace(stockContainer.getSymbol(), stockContainer);
                            } else {
                                getStandartStockCache().put(stockContainer.getSymbol(), stockContainer);
                            }

                    });

            }

            StockContainerFetcher.fetchCurrentUSDEURExchange(aDouble -> setCurrentUSDEURExchange(aDouble));
        }, 0, 12000);
    }

    @Override
    public void onDisable() {
        standartStockCache.clearAndClose();
        recentStockCache.clearAndClose();
        holdingPlayerCache.clearAndClose();
    }

    public static XStocks getInstance() {
        return instance;
    }

    public Cache<String, StockContainer> getStandartStockCache() {
        return standartStockCache;
    }

    public Cache<String, StockContainer> getRecentStockCache() {
        return recentStockCache;
    }

    public DecimalFormat getStockFormat() {
        return stockFormat;
    }

    public Cache<UUID, HoldingPlayer> getHoldingPlayerCache() {
        return holdingPlayerCache;
    }

    public Cache<UUID, ItemStack> getHeadsCache() {
        return headsCache;
    }

    public MongoDBStorage getMongoDBStorage() {
        return XCore.getInstance().getMongoDBStorage();
    }

    public double getCurrentUSDEURExchange() {
        return currentUSDEURExchange;
    }

    public void setCurrentUSDEURExchange(double currentUSDEURExchange) {
        this.currentUSDEURExchange = currentUSDEURExchange;
    }
}
