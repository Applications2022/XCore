package de.ruben.xcore.stock.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.*;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xcore.stock.service.StockContainerFetcher;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.LabyGUITemplate;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import de.ruben.xdevapi.custom.gui.response.ConfirmationResponse;
import de.ruben.xdevapi.performance.concurrent.TaskBatch;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BuyCryptoGui extends Gui {
    private String stockSymbol;
    private StockType stockType;

    public BuyCryptoGui(String stockSymbol, StockType stockType) {
        super(4, "§9Aktien §8| §9Crypto-Handel", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockSymbol = stockSymbol;
        this.stockType = stockType;

    }

    @Override
    public void open(@NotNull HumanEntity player) {

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));

        this.setItem(31, ItemPreset.closeItem(event -> {
            this.close(player);
        }));

        this.setItem(27, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new BuySellGui((Player) player, stockType, stockSymbol).open(player);
        }));

        if(XStocks.getInstance().getRecentStockCache().containsKey(stockSymbol)){
            StockContainer stockContainer = XStocks.getInstance().getRecentStockCache().get(stockSymbol);

            double amount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);
            double price = stockContainer.getRegularMarketPrice();

            this.setItem(13, ItemBuilder.from(getInfoStack(price, amount)).asGuiItem());

            this.setItem(15,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§2Kaufen §7(Eingabe in Euro)"))
                            .asGuiItem(event -> {

                                if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung kaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if(!input.equals("") && !input.equals(" ") && !input.isEmpty()){
                                                if(isDouble(input)){
                                                    double money = Double.parseDouble(input);

                                                    if(money <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if(confirmationResponse != ConfirmationResponse.NO){
                                                            CashService cashService = new CashService();

                                                            if(cashService.getValue(player.getUniqueId()) >= money){

                                                                cashService.removeValue(player.getUniqueId(), money, cashAccount -> {
                                                                    double holdingToAdd = money/stockContainer.getRegularMarketPrice();

                                                                    HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                    if(holdingService.hasHolding(stockSymbol)){
                                                                        holdingService.addHoldingAmount(stockSymbol, holdingToAdd);
                                                                    }else{
                                                                        holdingService.addHolding(new Holding(stockSymbol, holdingToAdd, 0, stockType));
                                                                    }

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                            +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingToAdd)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), holdingToAdd, money, HoldingHistoryType.BOUGHT));
                                                                });

                                                            }else{
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                            }

                                                        }
                                                    });


                                                }else{
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                }else{
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            ,"Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double money = aDouble;

                                                if(money <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();

                                                    if(confirmationResponse != ConfirmationResponse.NO){
                                                        CashService cashService = new CashService();

                                                        if(cashService.getValue(player.getUniqueId()) >= money){

                                                            cashService.removeValue(player.getUniqueId(), money, cashAccount -> {
                                                                double holdingToAdd = money/stockContainer.getRegularMarketPrice();

                                                                HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                if(holdingService.hasHolding(stockSymbol)){
                                                                    holdingService.addHoldingAmount(stockSymbol, holdingToAdd);
                                                                }else{
                                                                    holdingService.addHolding(new Holding(stockSymbol, holdingToAdd, 0, stockType));
                                                                }

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                        +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingToAdd)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), holdingToAdd, money, HoldingHistoryType.BOUGHT));
                                                            });

                                                        }else{
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
                    );

            this.setItem(11,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§2Kaufen §7(Eingabe in Crypto)"))
                            .asGuiItem(event -> {

                                if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel du von der Crypto-Währung du kaufen willst", "Eingabe in Crypto", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if(!input.equals("") && !input.equals(" ") && !input.isEmpty()){
                                                if(isDouble(input)){
                                                    double crypto = Double.parseDouble(input);

                                                    if(crypto <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }

                                                    double money = stockContainer.getRegularMarketPrice()*crypto;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if(confirmationResponse != ConfirmationResponse.NO){
                                                            CashService cashService = new CashService();

                                                            if(cashService.getValue(player.getUniqueId()) >= money){

                                                                cashService.removeValue(player.getUniqueId(), money, cashAccount -> {

                                                                    HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                    if(holdingService.hasHolding(stockSymbol)){
                                                                        holdingService.addHoldingAmount(stockSymbol, crypto);
                                                                    }else{
                                                                        holdingService.addHolding(new Holding(stockSymbol, crypto, 0, stockType));
                                                                    }

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                            +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(crypto)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), crypto, money, HoldingHistoryType.BOUGHT));

                                                                });

                                                            }else{
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                            }

                                                        }
                                                    });


                                                }else{
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                }else{
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Crypto"
                                            ,"Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double crypto = aDouble;

                                                if(crypto <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                double money = stockContainer.getRegularMarketPrice()*crypto;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if(confirmationResponse != ConfirmationResponse.NO){
                                                        CashService cashService = new CashService();

                                                        if(cashService.getValue(player.getUniqueId()) >= money){

                                                            cashService.removeValue(player.getUniqueId(), money, cashAccount -> {

                                                                HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                if(holdingService.hasHolding(stockSymbol)){
                                                                    holdingService.addHoldingAmount(stockSymbol, crypto);
                                                                }else{
                                                                    holdingService.addHolding(new Holding(stockSymbol, crypto, 0, stockType));
                                                                }

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                        +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(crypto)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), crypto, money, HoldingHistoryType.BOUGHT));
                                                            });

                                                        }else{
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );



        }else{
            this.setItem(11, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(13, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(15, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());

            fetchStock((Player) player);
        }
        super.open(player);
    }

    private void fetchStock(Player player) {
        StockContainerFetcher.fetchContainer(stockSymbol, stockContainer -> {

            double amount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);
            double price = stockContainer.getRegularMarketPrice();

            this.updateItem(13, ItemBuilder.from(getInfoStack(stockContainer.getRegularMarketPrice(), new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol))).asGuiItem());

            this.updateItem(15,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§2Kaufen §7(Eingabe in Euro)"))
                            .asGuiItem(event -> {

                                if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung kaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if(!input.equals("") && !input.equals(" ") && !input.isEmpty()){
                                                if(isDouble(input)){
                                                    double money = Double.parseDouble(input);

                                                    if(money <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }


                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if(confirmationResponse != ConfirmationResponse.NO){
                                                            CashService cashService = new CashService();

                                                            if(cashService.getValue(player.getUniqueId()) >= money){

                                                                cashService.removeValue(player.getUniqueId(), money, cashAccount -> {
                                                                    double holdingToAdd = money/stockContainer.getRegularMarketPrice();

                                                                    HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                    if(holdingService.hasHolding(stockSymbol)){
                                                                        holdingService.addHoldingAmount(stockSymbol, holdingToAdd);
                                                                    }else{
                                                                        holdingService.addHolding(new Holding(stockSymbol, holdingToAdd, 0, stockType));
                                                                    }

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                            +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingToAdd)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), holdingToAdd, money, HoldingHistoryType.BOUGHT));
                                                                });

                                                            }else{
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                            }

                                                        }
                                                    });

                                                }else{
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                }else{
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            ,"Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double money = aDouble;

                                                if(money <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI(player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if(confirmationResponse != ConfirmationResponse.NO){
                                                        CashService cashService = new CashService();

                                                        if(cashService.getValue(player.getUniqueId()) >= money){

                                                            cashService.removeValue(player.getUniqueId(), money, cashAccount -> {
                                                                double holdingToAdd = money/stockContainer.getRegularMarketPrice();

                                                                HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                if(holdingService.hasHolding(stockSymbol)){
                                                                    holdingService.addHoldingAmount(stockSymbol, holdingToAdd);
                                                                }else{
                                                                    holdingService.addHolding(new Holding(stockSymbol, holdingToAdd, 0, stockType));
                                                                }

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                        +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingToAdd)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), holdingToAdd, money, HoldingHistoryType.BOUGHT));

                                                            });

                                                        }else{
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

            this.updateItem(11,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§2Kaufen §7(Eingabe in Crypto)"))
                            .asGuiItem(event -> {

                                if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel du von der Crypto-Währung du kaufen willst", "Eingabe in Crypto", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if(!input.equals("") && !input.equals(" ") && !input.isEmpty()){
                                                if(isDouble(input)){
                                                    double crypto = Double.parseDouble(input);

                                                    if(crypto <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }


                                                    double money = stockContainer.getRegularMarketPrice()*crypto;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if(confirmationResponse != ConfirmationResponse.NO){
                                                            CashService cashService = new CashService();

                                                            if(cashService.getValue(player.getUniqueId()) >= money){

                                                                cashService.removeValue(player.getUniqueId(), money, cashAccount -> {

                                                                    HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                    if(holdingService.hasHolding(stockSymbol)){
                                                                        holdingService.addHoldingAmount(stockSymbol, crypto);
                                                                    }else{
                                                                        holdingService.addHolding(new Holding(stockSymbol, crypto, 0, stockType));
                                                                    }

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                            +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(crypto)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), crypto, money, HoldingHistoryType.BOUGHT));
                                                                });

                                                            }else{
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                            }

                                                        }
                                                    });


                                                }else{
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                }else{
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Crypto"
                                            ,"Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double crypto = aDouble;

                                                if(crypto <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                double money = stockContainer.getRegularMarketPrice()*crypto;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if(confirmationResponse != ConfirmationResponse.NO){
                                                        CashService cashService = new CashService();

                                                        if(cashService.getValue(player.getUniqueId()) >= money){

                                                            cashService.removeValue(player.getUniqueId(), money, cashAccount -> {

                                                                HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                                if(holdingService.hasHolding(stockSymbol)){
                                                                    holdingService.addHoldingAmount(stockSymbol, crypto);
                                                                }else{
                                                                    holdingService.addHolding(new Holding(stockSymbol, crypto, 0, stockType));
                                                                }

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"
                                                                        +XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(crypto)+" "+stockSymbol+" §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money)+"€ §7gekauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), crypto, money, HoldingHistoryType.BOUGHT));

                                                            });

                                                        }else{
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

        });
    }

    private ItemStack getInfoStack(Double price, double amount){
        ItemStack itemStack = ItemBuilder
                .from(Material.TORCH)
                .name(Component.text("§bInfo:"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Einzelner Kaufpreis: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(price)+"€"),
                        Component.text(" ")
                )
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setDouble("amount", amount);
        nbtItem.setDouble("price", price);

        return nbtItem.getItem();
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
