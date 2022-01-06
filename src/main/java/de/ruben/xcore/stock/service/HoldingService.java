package de.ruben.xcore.stock.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.Holding;
import de.ruben.xcore.stock.model.HoldingHistory;
import de.ruben.xcore.stock.model.HoldingPlayer;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;
import org.bson.codecs.CollectibleCodec;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.stream.Collectors;

public class HoldingService {

    private HoldingPlayer holdingPlayer;

    public HoldingService(UUID uuid){
        this.holdingPlayer = getHoldingPlayer(uuid);
    }

    public boolean hasHolding(String symbol){
        return holdingPlayer.getHoldings().containsKey(symbol);
    }

    public Holding getHolding(String symbol){
        return holdingPlayer.getHoldings().get(symbol);
    }

    public Double getHoldingAmount(String symbol){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            return holding.getAmount();
        }else{
            return 0.0;
        }
    }

    public double getHoldingBuyPrice(String symbol){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            return holding.getBuyPrice();
        }else{
            return 0;
        }
    }

    public void addHoldingAmount(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            holding.setAmount(holding.getAmount() + amount);
            updateHolding(holding, false);
        }
    }

    public void setHoldingAmount(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            holding.setAmount( amount);
            updateHolding(holding, false);
        }
    }

    public void removeHoldingAmount(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);

            if(holding.getAmount() <= 0){
                removeHolding(holding);
            }else{
                holding.setAmount(holding.getAmount() - amount);
            }
            updateHolding(holding, false);
        }
    }

    public void addHoldingBuyPrice(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            holding.setBuyPrice(holding.getBuyPrice() + amount);
            updateHolding(holding, false);
        }
    }

    public void setHoldingBuyPrice(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            holding.setBuyPrice(amount);
            updateHolding(holding, false);
        }
    }

    public void removeHoldingBuyPrice(String symbol, double amount){
        if(hasHolding(symbol)) {
            Holding holding = getHolding(symbol);
            holding.setBuyPrice(holding.getBuyPrice() - amount);
            updateHolding(holding, false);
        }
    }

    public void removeHolding(Holding holding){
        Map<String, Holding> holdings = holdingPlayer.getHoldings();
        if(holdings.containsKey(holding.getSymbol())){
            holdings.remove(holding.getSymbol());
            holdingPlayer.setHoldings(holdings);
            updateHoldingPlayer(holdingPlayer);
        }
    }

    public void addHolding(Holding holding){
        updateHolding(holding, true);
    }

    public void addHoldingHistory(HoldingHistory holdingHistory){
        List<HoldingHistory> history  = holdingPlayer.getHistory();
        history.add(holdingHistory);
        holdingPlayer.setHistory(history);

        updateHoldingPlayer(holdingPlayer);
    }

    public void trimHoldings(){
        List<HoldingHistory> history  = holdingPlayer.getHistorySorted();
        holdingPlayer.setHistory(history.stream().limit(56).collect(Collectors.toList()));
        updateHoldingPlayer(holdingPlayer);
    }

    public void removePlayerFromCache(){
        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().replaceOne(Filters.eq("_id", holdingPlayer.getUuid()), holdingPlayer.toDocument());
            XStocks.getInstance().getHoldingPlayerCache().remove(holdingPlayer.getUuid());
        });
    }

    public HoldingPlayer getHoldingPlayer() {
        return holdingPlayer;
    }

    public void updateHolding(Holding holding, boolean canput){
        Map<String, Holding> holdings = holdingPlayer.getHoldings();

        if(hasHolding(holding.getSymbol())) {
            holdings.replace(holding.getSymbol(), holding);
        }else{
            if(canput) {
                holdings.put(holding.getSymbol(), holding);
            }else {
                return;
            }
        }

        holdingPlayer.setHoldings(holdings);

        updateHoldingPlayer(holdingPlayer);
    }

    public void updateHoldingPlayer(HoldingPlayer holdingPlayer){

        if(Bukkit.getOfflinePlayer(holdingPlayer.getUuid()).isOnline()) XStocks.getInstance().getHoldingPlayerCache().replace(holdingPlayer.getUuid(), holdingPlayer);

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().replaceOne(Filters.eq("_id", holdingPlayer.getUuid()), holdingPlayer.toDocument());
        });

    }


    private HoldingPlayer getHoldingPlayer(UUID uuid){
        if(XStocks.getInstance().getHoldingPlayerCache().containsKey(uuid)){
            return XStocks.getInstance().getHoldingPlayerCache().get(uuid);
        }else{
            Document document = getCollection().find(Filters.eq("_id", uuid)).first();

            if(document == null){
                HoldingPlayer holdingPlayer = new HoldingPlayer(uuid, new HashMap<>(), new ArrayList<>());
                getCollection().insertOne(holdingPlayer.toDocument());
                if(Bukkit.getOfflinePlayer(uuid).isOnline()) XStocks.getInstance().getHoldingPlayerCache().putIfAbsent(uuid, holdingPlayer);
                return holdingPlayer;
            }else{
                HoldingPlayer holdingPlayer = new HoldingPlayer().fromDocument(document);
                if(Bukkit.getOfflinePlayer(uuid).isOnline()) XStocks.getInstance().getHoldingPlayerCache().putIfAbsent(uuid, holdingPlayer);
                return holdingPlayer;
            }
        }
    }

    private MongoCollection<Document> getCollection(){
        return XCore.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Stock").getCollection("Data_HoldingPlayers");
    }

    private MongoDBStorage getMongoDBStorage(){
        return XStocks.getInstance().getMongoDBStorage();
    }

}
