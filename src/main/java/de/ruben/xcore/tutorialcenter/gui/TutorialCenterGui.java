package de.ruben.xcore.tutorialcenter.gui;

import de.ruben.xcore.changelog.gui.ChangelogGui;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xcore.tutorialcenter.model.TutorialModule;
import de.ruben.xcore.tutorialcenter.service.TutorialModuleService;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TutorialCenterGui extends Gui {

    private PaginatedArrayList paginatedArrayList;

    public TutorialCenterGui() {
        super(6, "§9§lTutorials", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.paginatedArrayList = new PaginatedArrayList(new TutorialModuleService().getTutorialMap().values(),28);

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));

        this.setDefaultClickAction(event -> event.setCancelled(true));
    }

    public void open(@NotNull HumanEntity player, Integer page) {
        paginatedArrayList.gotoPage(page);

        this.setItem(49, ItemPreset.closeItem(event -> this.close(player)));

        if(paginatedArrayList.isNextPageAvailable()){
            this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                new TutorialCenterGui().open(clicked, paginatedArrayList.getPageIndex()+1);
            }));
        }

        if(paginatedArrayList.isPreviousPageAvailable()){
            this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                new TutorialCenterGui().open(clicked, paginatedArrayList.getPageIndex()-1);
            }));
        }

        for (int i = 0; i < 28; i++) {
            if (i >= paginatedArrayList.size()) {
                break;
            }

            TutorialModule tutorialModule = (TutorialModule) paginatedArrayList.get(i);

            this.addItem(ItemBuilder.from(tutorialModule.getItemStack()).asGuiItem(event -> {
                tutorialModule.performOpen((Player) player, 0);
            }));

        }

        super.open(player);
    }
}
