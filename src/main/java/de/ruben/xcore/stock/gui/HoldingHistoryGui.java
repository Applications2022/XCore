package de.ruben.xcore.stock.gui;

import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xcore.stock.model.Holding;
import de.ruben.xcore.stock.model.HoldingHistory;
import de.ruben.xcore.stock.model.HoldingSortType;
import de.ruben.xcore.stock.model.StockType;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.PlainDocument;
import java.util.Set;

public class HoldingHistoryGui extends Gui {

    private PaginatedArrayList paginatedArrayList;
    private StockType stockType;
    private HoldingSortType holdingSortType;


    public HoldingHistoryGui(Player player, StockType stockType, HoldingSortType holdingSortType) {
        super(6, "§9Aktien §8| §9Verlauf", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        paginatedArrayList = new PaginatedArrayList(new HoldingService(player.getUniqueId()).getHoldingPlayer().getHistorySorted(), 28);
        this.stockType = stockType;
        this.holdingSortType = holdingSortType;
    }


    public void open(@NotNull HumanEntity player, int page) {
        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));

        this.setItem(45, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new DepotGui((Player) player, stockType, holdingSortType).open(player, 0);
        }));

        this.setItem(49, ItemPreset.closeItem(event -> {
            this.close(player);
        }));

        super.open(player);

        setPageItems((Player) player, page);
    }

    private void setPageItems(Player player, int page){
        paginatedArrayList.gotoPage(page);

        XDevApi.getInstance().getxScheduler().async(() -> {
            for (int i = 0; i < 28; i++) {
                if (i >= paginatedArrayList.size()) {
                    break;
                }

                HoldingHistory holdingHistory = (HoldingHistory) paginatedArrayList.get(i);

                this.addItem(ItemBuilder.from(holdingHistory.toItemStack()).asGuiItem());
            }

            if (paginatedArrayList.isNextPageAvailable()) {
                this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                    new HoldingHistoryGui(player, stockType, holdingSortType).open(player, paginatedArrayList.getPageIndex() + 1);
                }));
            }

            if (paginatedArrayList.isPreviousPageAvailable()) {
                this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                    new HoldingHistoryGui(player, stockType, holdingSortType).open(player, paginatedArrayList.getPageIndex() - 1);
                }));
            }

            this.update();

        });
    }
}
