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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class BuySellGui extends Gui {

    private StockType stockType;
    private String stockSymbol;

    public BuySellGui(Player player, StockType stockType, String stockSymbol) {
        super(4, "§9Aktien §8| §9Handel", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockType = stockType;
        this.stockSymbol = stockSymbol;
    }

    @Override
    public void open(@NotNull HumanEntity player){

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));

        this.setItem(31, ItemPreset.closeItem(event -> {
            this.close(player);
        }));

        this.setItem(27, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new SingleStockGui(stockSymbol, stockType).open(player);
        }));

        HoldingService holdingService = new HoldingService(player.getUniqueId());

        int slot = (holdingService.hasHolding(stockSymbol) && holdingService.getHoldingAmount(stockSymbol) > 0) ? 13 : 11;

        if(XStocks.getInstance().getRecentStockCache().containsKey(stockSymbol)){
            StockContainer stockContainer = XStocks.getInstance().getRecentStockCache().get(stockSymbol);

            ItemStack itemStack =stockContainer.toItemStack();
            itemStack.editMeta(itemMeta -> {
                List<Component> lore = itemMeta.lore();
                lore.add(Component.text(" "));
                lore.add(Component.text("§7In deinem Besitzt: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol))));
                lore.add(Component.text(" "));

                itemMeta.lore(lore);

                itemStack.setItemMeta(itemMeta);
            });

            this.setItem(slot, ItemBuilder.from(itemStack).asGuiItem());

            this.setItem(15,
                    ItemBuilder
                            .from(Material.GREEN_WOOL)
                            .name(Component.text("§2Kaufen"))
                            .asGuiItem(event -> {
                                if(stockContainer.getStockType() == StockType.STOCK){
                                    new BuyStockGui((Player) player, stockSymbol, stockType).open(player);
                                }else{
                                    new BuyCryptoGui(stockSymbol, stockType).open(player);
                                }
                            })
            );

            if(holdingService.hasHolding(stockSymbol) && holdingService.getHoldingAmount(stockSymbol) > 0){
                this.setItem(11,
                        ItemBuilder
                                .from(Material.RED_WOOL)
                                .name(Component.text("§cVerkaufen"))
                                .asGuiItem(event -> {
                                    if(stockContainer.getStockType() == StockType.STOCK){
                                        new SellStockGui((Player) player, stockSymbol, stockType).open(player);
                                    }else{
                                        new SellCryptoGui(stockSymbol, stockType).open(player);
                                    }
                                })
                );
            }

        }else{
            this.setItem(slot, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(15, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            if(holdingService.hasHolding(stockSymbol) && holdingService.getHoldingAmount(stockSymbol) > 0){
                this.setItem(11, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            }
            fetchStock((Player) player, slot);
        }

        super.open(player);
    }

    private void fetchStock(Player player, int slot){
        HoldingService holdingService = new HoldingService(player.getUniqueId());

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

            ItemStack itemStack =stockContainer.toItemStack();
            itemStack.editMeta(itemMeta -> {
                List<Component> lore = itemMeta.lore();
                lore.add(Component.text(" "));
                lore.add(Component.text("§7In deinem Besitzt: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol))));
                lore.add(Component.text(" "));

                itemMeta.lore(lore);

                itemStack.setItemMeta(itemMeta);
            });

            this.updateItem(slot, ItemBuilder.from(itemStack).asGuiItem());

            this.updateItem(15,
                    ItemBuilder
                            .from(Material.GREEN_WOOL)
                            .name(Component.text("§2Kaufen"))
                            .asGuiItem(event -> {
                                if(stockContainer.getStockType() == StockType.STOCK){
                                    new BuyStockGui(player, stockSymbol, stockType).open(player);
                                }else{
                                    new BuyCryptoGui(stockSymbol, stockType).open(player);
                                }
                            })
            );

            if(holdingService.hasHolding(stockSymbol) && holdingService.getHoldingAmount(stockSymbol) > 0){
                this.updateItem(11,
                        ItemBuilder
                                .from(Material.RED_WOOL)
                                .name(Component.text("§cVerkaufen"))
                                .asGuiItem(event -> {
                                    if(stockContainer.getStockType() == StockType.STOCK){
                                        new SellStockGui(player, stockSymbol, stockType).open(player);
                                    }else{
                                        new SellCryptoGui(stockSymbol, stockType).open(player);
                                    }
                                })
                );
            }


        });
    }



}
