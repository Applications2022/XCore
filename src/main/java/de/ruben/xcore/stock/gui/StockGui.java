package de.ruben.xcore.stock.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.Holding;
import de.ruben.xcore.stock.model.HoldingSortType;
import de.ruben.xcore.stock.model.StockType;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.LabyGUITemplate;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class StockGui extends Gui {
    public StockGui() {
        super(6, "§9Aktien", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));
    }

    public void open(@NotNull HumanEntity player, StockType stockType) {

        this.setItem(49, ItemPreset.closeItem(event -> this.close(player)));

        this.setItem(53, ItemBuilder.from(XStocks.getInstance().getHeadsCache().get(player.getUniqueId())).name(Component.text("§bDein Depot")).lore(new ArrayList<>()).asGuiItem(event -> {
            new DepotGui((Player) player, stockType, HoldingSortType.ALL).open(player, 0);
        }));

        this.setItem(45, ItemBuilder
                .from(Material.OAK_SIGN)
                .name(Component.text("§bInvestition Suchen"))
                .asGuiItem(event -> {

                    if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
                        LabyGUITemplate.createInput((Player) player, "§bInvestition nach Symbol Suchen", "AAPL", 32, (uuid, s) -> {
                            try {
                                String symbol = new ObjectMapper().readTree(s).get("value").asText();

                                if(!symbol.equals("") && !symbol.equals(" ") && !symbol.isEmpty()){
                                    new SingleStockGui(symbol.toUpperCase(), stockType).open(player);
                                }
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }

                        });
                    }else{
                        NoLabyGUITemplate.createStringInput(XDevApi.getInstance(), "§bInvestition nach Symbol Suchen", "AAPL", s -> {
                            new SingleStockGui(s.toUpperCase(), stockType).open(player);
                            return AnvilGUI.Response.close();
                        }).open((Player) player);
                    }

                })
        );


        if(stockType == StockType.CRYPTOCURRENCY){
            this.setItem(3, ItemBuilder.from(Material.GOLD_NUGGET).name(Component.text("§9§lCrypto-Währungen")).flags(ItemFlag.HIDE_ENCHANTS).enchant(Enchantment.LURE, 1).asGuiItem());
            this.setItem(5, ItemBuilder.from(Material.IRON_INGOT).name(Component.text("§b§lAktien")).asGuiItem(event -> {
                new StockGui().open(player, StockType.STOCK);
            }));
        }else{
            this.setItem(3, ItemBuilder.from(Material.GOLD_NUGGET).name(Component.text("§9§lCrypto-Währungen")).asGuiItem(event -> {
                new StockGui().open(player, StockType.CRYPTOCURRENCY);
            }));
            this.setItem(5, ItemBuilder.from(Material.IRON_INGOT).name(Component.text("§b§lAktien")).flags(ItemFlag.HIDE_ENCHANTS).enchant(Enchantment.LURE, 1).asGuiItem());
        }

        super.open(player);

        XStocks.getInstance().getStandartStockCache()
                .asMap()
                .values()
                .stream()
                .filter(stockContainer -> stockContainer.getStockType()==stockType)
                .sorted((o1, o2) -> o2.getRegularMarketPrice().compareTo(o1.getRegularMarketPrice()))
                .forEach(stockContainer -> {
                    if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol()) && XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol()).getLoadTime() >= stockContainer.getLoadTime()){
                        this.addItem(ItemBuilder.from(XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol()).toItemStack()).asGuiItem(event -> {
                            new SingleStockGui(stockContainer.getSymbol(), stockContainer.getStockType()).open(player);
                        }));
                    }else if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol()) && XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol()).getLoadTime() < stockContainer.getLoadTime()){
                        XStocks.getInstance().getRecentStockCache().remove(stockContainer.getSymbol());
                        this.addItem(ItemBuilder.from(stockContainer.toItemStack()).asGuiItem(event -> {
                            new SingleStockGui(stockContainer.getSymbol(), stockContainer.getStockType()).open(player);
                        }));
                    }else{
                        this.addItem(ItemBuilder.from(stockContainer.toItemStack()).asGuiItem(event -> {
                            new SingleStockGui(stockContainer.getSymbol(), stockContainer.getStockType()).open(player);
                        }));
                    }
        });

        this.update();
    }
}
