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

public class SellStockGui extends Gui {

    private String stockSymbol;
    private StockType stockType;
    private Player player;

    public SellStockGui(Player player, String stockSymbol, StockType stockType) {
        super(4, "§9Aktien §8| §bVerkaufen §8| §9"+stockSymbol, Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockSymbol = stockSymbol;
        this.stockType = stockType;
        this.player = player;
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

            this.setItem(35, ItemBuilder.from(Material.NETHER_STAR).name(Component.text("§cVerkaufen")).asGuiItem(event -> {
                int amount = getCurrentAmount(this.getGuiItem(13).getItemStack());

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Aktien werden verkauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*amount;
                                double recentAmount = amount;

                                new CashService().addValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    holdingService.removeHoldingAmount(stockContainer1.getSymbol(), recentAmount);

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" Aktien §7 für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7verkauft!");
                                });

                            }
                        });
                    }

                });

            }));

            this.setItem(34, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).name(Component.text("§cAlle Verkaufen")).asGuiItem(event -> {
                int amount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol).intValue();

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf Aller Aktien Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Alle Aktien werden verkauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*amount;
                                double recentAmount = amount;

                                new CashService().addValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    holdingService.removeHoldingAmount(stockContainer1.getSymbol(), recentAmount);

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" Aktien §7 für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7verkauft!");

                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.SOLD));
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

            this.setItem(35, ItemBuilder.from(Material.NETHER_STAR).name(Component.text("§cVerkaufen")).asGuiItem(event -> {
                int amount = getCurrentAmount(this.getGuiItem(13).getItemStack());

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Aktien werden verkauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*amount;
                                double recentAmount = amount;

                                new CashService().addValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    holdingService.removeHoldingAmount(stockContainer1.getSymbol(), recentAmount);

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" Aktien §7 für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7verkauft!");

                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.SOLD));
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
                    int amountMount = amount+getCurrentAmount(this.getGuiItem(13).getItemStack());
                    int finalAmount = (amountMount <= new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol) ? (amountMount) : new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol).intValue());

                    this.updateItem(13, ItemBuilder.from(getInfoStack(price, finalAmount)).asGuiItem());
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
                        Component.text("§7➥ Aktuelle Anzahl: §b"+amount+"/"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol).intValue())),
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
