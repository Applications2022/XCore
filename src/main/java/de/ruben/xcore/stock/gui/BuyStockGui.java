package de.ruben.xcore.stock.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.*;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xcore.stock.service.StockContainerFetcher;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import de.ruben.xdevapi.custom.gui.response.ConfirmationResponse;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Set;

public class BuyStockGui extends Gui {

    private String stockSymbol;
    private StockType stockType;

    public BuyStockGui(Player player, String stockSymbol, StockType stockType) {
        super(4, "§9Aktien §8| §bKaufen §8| §9"+stockSymbol, Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

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
            this.setItem(13, ItemBuilder.from(getInfoStack(stockContainer.getRegularMarketPrice(), 1)).asGuiItem());

            this.setItem(12, getMinusItem(1, stockContainer.getRegularMarketPrice()));
            this.setItem(11, getMinusItem(3, stockContainer.getRegularMarketPrice()));
            this.setItem(10, getMinusItem(5, stockContainer.getRegularMarketPrice()));

            this.setItem(14, getPlusItem(1, stockContainer.getRegularMarketPrice()));
            this.setItem(15, getPlusItem(3, stockContainer.getRegularMarketPrice()));
            this.setItem(16, getPlusItem(5, stockContainer.getRegularMarketPrice()));

            this.setItem(35, ItemBuilder.from(Material.NETHER_STAR).name(Component.text("§2Kaufen")).asGuiItem(event -> {
                int amount = getCurrentAmount(this.getGuiItem(13).getItemStack());

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Aktien werden gekauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*amount;
                                double recentAmount = amount;

                                if(new CashService().getValue(player1.getUniqueId()) < payAmount){
                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu besitzt du zu wenig Geld!");
                                    return;
                                }

                                new CashService().removeValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    if(holdingService.hasHolding(stockContainer1.getSymbol())){
                                        holdingService.addHoldingAmount(stockContainer1.getSymbol(), recentAmount);
                                    }else{
                                        holdingService.addHolding(new Holding(stockContainer1.getSymbol(), recentAmount, 0, stockContainer1.getStockType()));
                                    }

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" Aktien §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7gekauft!");
                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.BOUGHT));

                                });

                            }
                        });
                    }

                });

            }));

        }else{

            for(int i = 9; i < 18; i++){
                this.setItem(i, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            }


            fetchStock((Player) player);
        }


        super.open(player);
    }

    private void fetchStock(Player player) {

        StockContainerFetcher.fetchContainer(stockSymbol, stockContainer -> {

            if (stockContainer == null) {
                Bukkit.getScheduler().runTask(XCore.getInstance(), () -> player.closeInventory());
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "Beim Suchen der Investition ist ein Fehler aufgetreten.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "Das kann folgende Ursachen haben:");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "- Investition exestiert unter dem Kürzel nicht.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "- Wir unterstützen nur Investitionen, welche in den Währungen §b$ §7oder §b€ §7gehandelt werden.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "- Wir unterstützen keine Investitionen des Typs §bWährung§7 und §bMutualfund§7.");
                return;
            }

        this.updateItem(13, ItemBuilder.from(getInfoStack(stockContainer.getRegularMarketPrice(), 1)).asGuiItem());

            this.updateItem(12, getMinusItem(1, stockContainer.getRegularMarketPrice()));
            this.updateItem(11, getMinusItem(3, stockContainer.getRegularMarketPrice()));
            this.updateItem(10, getMinusItem(5, stockContainer.getRegularMarketPrice()));

            this.updateItem(14, getPlusItem(1, stockContainer.getRegularMarketPrice()));
            this.updateItem(15, getPlusItem(3, stockContainer.getRegularMarketPrice()));
            this.updateItem(16, getPlusItem(5, stockContainer.getRegularMarketPrice()));

            this.setItem(35, ItemBuilder.from(Material.NETHER_STAR).name(Component.text("§2Kaufen")).asGuiItem(event -> {
                int amount = getCurrentAmount(this.getGuiItem(13).getItemStack());

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§9Kauf Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Aktien werden gekauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*amount;
                                double recentAmount = amount;

                                if(new CashService().getValue(player1.getUniqueId()) < payAmount){
                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu besitzt du zu wenig Geld!");
                                    return;
                                }

                                new CashService().removeValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    if(holdingService.hasHolding(stockContainer1.getSymbol())){
                                        holdingService.addHoldingAmount(stockContainer1.getSymbol(), recentAmount);
                                    }else{
                                        holdingService.addHolding(new Holding(stockContainer1.getSymbol(), recentAmount, 0, stockContainer1.getStockType()));
                                    }

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" Aktien §7für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7gekauft!");

                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.BOUGHT));

                                });

                            }
                        });
                    }

                });

            }));

    });

    }

    private GuiItem getMinusItem(int amount, double price){
        return ItemBuilder
                .from(Material.RED_STAINED_GLASS_PANE)
                .name(Component.text("§c-"+amount))
                .asGuiItem(event -> {
                    int finalAmount = getCurrentAmount(this.getGuiItem(13).getItemStack())-amount;
                    finalAmount = finalAmount >= 0 ? finalAmount : 0;

                    this.updateItem(13, ItemBuilder.from(getInfoStack(price, finalAmount)).asGuiItem());
                });
    }

    private GuiItem getPlusItem(int amount, double price){
        return ItemBuilder
                .from(Material.LIME_STAINED_GLASS_PANE)
                .name(Component.text("§a+"+amount))
                .asGuiItem(event -> {
                    this.updateItem(13, ItemBuilder.from(getInfoStack(price, getCurrentAmount(this.getGuiItem(13).getItemStack())+amount)).asGuiItem());
                });
    }


    private Integer getCurrentAmount(ItemStack itemStack){
        return new NBTItem(itemStack).getInteger("amount");
    }

    private ItemStack getInfoStack(Double price, int amount){
        ItemStack itemStack = ItemBuilder
                .from(Material.TORCH)
                .name(Component.text("§bInfo:"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Aktuelle Anzahl: §b"+amount),
                        Component.text("§7➥ Einzelner Kaufpreis: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(price)+"€"),
                        Component.text("§7➥ Insgesamter Kaufpreis: §b"+ (XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(price * amount)) + "€"),
                        Component.text(" ")
                )
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger("amount", amount);
        nbtItem.setDouble("price", price);

        return nbtItem.getItem();
    }
}
