package de.ruben.xcore.stock.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.HoldingSortType;
import de.ruben.xcore.stock.model.StockContainer;
import de.ruben.xcore.stock.model.StockType;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xcore.stock.service.StockContainerFetcher;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Set;

public class SingleStockGui extends Gui {

    private String stockSymbol;
    private StockType stockType;

    public SingleStockGui(String stockSymbol, StockType stockType) {
        super(4, "§9Aktien §8| §9"+stockSymbol.toUpperCase(), Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockSymbol = stockSymbol.toUpperCase();
        this.stockType = stockType;


    }

    public SingleStockGui(String stockSymbol) {
        super(4, "§9Aktien §8| §9"+stockSymbol.toUpperCase(), Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockSymbol = stockSymbol.toUpperCase();
        this.stockType = StockType.PROFILE;
    }

    @Override
    public void open(@NotNull HumanEntity player) {

        HoldingService holdingService = new HoldingService(player.getUniqueId());

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));

        if(XStocks.getInstance().getRecentStockCache().containsKey(stockSymbol)){
            StockContainer stockContainer = XStocks.getInstance().getRecentStockCache().get(stockSymbol);

            this.setItem(13, ItemBuilder.from(XStocks.getInstance().getRecentStockCache().get(stockSymbol).toItemStack()).asGuiItem());

            String changeString = (stockContainer.getChange() >= 0 ? "§a+" : "§c") + (XStocks.getInstance().getStockFormat().format(stockContainer.getChange()*holdingService.getHoldingAmount(stockSymbol))) + "€";

            double changePercentage = (stockContainer.getRegularMarketPrice() / ((stockContainer.getRegularMarketPrice()- stockContainer.getChange())/100))-100;

            String changeStringpercentage = (changePercentage >= 0 ? "§a+" : "§c") + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(changePercentage)+"%";

            this.setItem(11,
                    ItemBuilder
                            .from(XStocks.getInstance().getHeadsCache().get(player.getUniqueId()))
                            .name(Component.text("§bDepot"))
                            .lore(
                                    Component.text(" "),
                                    Component.text("§7➥ In Besitz: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingService.getHoldingAmount(stockSymbol))),
                                    Component.text("§7➥ Aktueller Wert: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((stockContainer.getRegularMarketPrice()*holdingService.getHoldingAmount(stockSymbol))) + "€"),
                                    Component.text("§7➥ Änderung (Vortag): "+(holdingService.hasHolding(stockSymbol) ? (changeString+" §7("+changeStringpercentage+"§7)") : "§b---"))
                            )
                            .asGuiItem()
            );

            this.setItem(15, ItemBuilder
                    .from(Material.EMERALD)
                    .name(Component.text("§bKaufen§7/§bVerkaufen"))
                    .asGuiItem(event -> {
                        new BuySellGui((Player) player, stockType, stockSymbol).open(player);
                    })
            );

        }else{
            this.setItem(13, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(11, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(15, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            fetchStock((Player) player, holdingService);
        }

        this.setItem(27, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            if(stockType == StockType.PROFILE){
                new DepotGui((Player) player, StockType.CRYPTOCURRENCY, HoldingSortType.ALL).open(player, 0);
            }else {
                new StockGui().open(player, (stockType == null) ? StockType.CRYPTOCURRENCY : stockType);
            }
        }));

        this.setItem(31, ItemPreset.closeItem(event -> {
            this.close(player);
        }));



        super.open(player);
    }

    private void fetchStock(Player player, HoldingService holdingService){
        StockContainerFetcher.fetchContainer(stockSymbol, stockContainer -> {

            if(stockContainer == null){
                Bukkit.getScheduler().runTask(XCore.getInstance(), () -> player.closeInventory());
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Beim Suchen der Investition ist ein Fehler aufgetreten.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Das kann folgende Ursachen haben:");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"- Investition exestiert unter dem Kürzel nicht.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"- Wir unterstützen nur Investitionen, welche in den Währungen §b$ §7oder §b€ §7gehandelt werden.");
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"- Wir unterstützen keine Investitionen des Typs §bWährung§7 und §bMutualfund§7.");
                return;
            }

            XStocks.getInstance().getRecentStockCache().putIfAbsent(stockContainer.getSymbol(), stockContainer);
            this.updateItem(13, ItemBuilder.from(stockContainer.toItemStack()).asGuiItem());

            String changeString = (stockContainer.getChange() >= 0 ? "§a+" : "§c") + (XStocks.getInstance().getStockFormat().format(stockContainer.getChange()*holdingService.getHoldingAmount(stockSymbol))) + (stockContainer.getCurrency().equals("USD") ? "$" : "€");

            double changePercentage = (stockContainer.getRegularMarketPrice() / ((stockContainer.getRegularMarketPrice()- stockContainer.getChange())/100))-100;

            String changeStringpercentage = (changePercentage >= 0 ? "§a+" : "§c") + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(changePercentage)+"%";

            this.updateItem(11,
                    ItemBuilder
                            .from(XStocks.getInstance().getHeadsCache().get(player.getUniqueId()))
                            .name(Component.text("§bDepot"))
                            .lore(
                                    Component.text(" "),
                                    Component.text("§7➥ In Besitz: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(holdingService.getHoldingAmount(stockSymbol))),
                                    Component.text("§7➥ Aktueller Wert: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((stockContainer.getRegularMarketPrice()*holdingService.getHoldingAmount(stockSymbol))) + (stockContainer.getCurrency().equals("USD") ? "$" : "€")),
                                    Component.text("§7➥ Änderung (Vortag): "+(holdingService.hasHolding(stockSymbol) ? (changeString+" §7("+changeStringpercentage+"§7)") : "§b---"))
                            )
                            .asGuiItem()
            );

            this.updateItem(15, ItemBuilder
                    .from(Material.EMERALD)
                    .name(Component.text("§bKaufen§7/§bVerkaufen"))
                    .asGuiItem(event -> {
                        new BuySellGui(player, stockType, stockSymbol).open(player);
                    })
            );
        });
    }


}
