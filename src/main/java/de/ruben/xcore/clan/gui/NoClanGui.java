package de.ruben.xcore.clan.gui;

import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.model.gui.FilterType;
import de.ruben.xcore.clan.model.gui.SortType;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class NoClanGui extends Gui {
    public NoClanGui() {
        super(4, "§9§lClan", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.disableAllInteractions();
        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));


    }

    @Override
    public void open(@NotNull HumanEntity player) {


        this.setItem(11, ItemBuilder
                .from(Material.MAP)
                .name(Component.text("§bClans durchsuchen"))
                .asGuiItem(event -> {
                    new ClanBrowseGui(SortType.NO, FilterType.ALL).open(player, 0);
                })
        );

        this.setItem(15, ItemBuilder
                .from(Material.GREEN_DYE)
                .name(Component.text("§bClan erstellen"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Erstelle einen Clan für §b1.000.000€§7."),
                        Component.text(" ")
                )
                .asGuiItem(event -> {
                    event.getWhoClicked().closeInventory();
                    XClan.getInstance().getClanCreateConversation().getConversationFactory().buildConversation((Conversable) event.getWhoClicked()).begin();
                })
        );

        this.setItem(31, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));

        this.setItem(13, ItemBuilder
                .from(Material.TORCH)
                .name(Component.text("§6§lInfo"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Klicke um eine Liste aller möglichen Commands anzuzeigen!"),
                        Component.text(" ")
                )
                .asGuiItem(event -> {
                    // TODO: Create Help message
                })
        );

        super.open(player);
    }
}
