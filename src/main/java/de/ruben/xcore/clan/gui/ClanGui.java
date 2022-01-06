package de.ruben.xcore.clan.gui;

import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
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

import java.util.Set;

public class ClanGui extends Gui {
    public ClanGui() {
        super(4, "§9§lClan", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.disableAllInteractions();
        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));
        this.setItem(31, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));
    }

    public void open(Player player, Clan clan) {

        this.setItem(11, ItemBuilder
                .from(Material.REDSTONE_TORCH)
                .name(Component.text("§bEinstellungen"))
                .asGuiItem(event -> {
                    new ClanSettingsGui(new ClanPlayerService().getClan(event.getWhoClicked().getUniqueId())).open(event.getWhoClicked());
                })
        );

        this.setItem(13, ItemBuilder
                .from(Material.GOLD_INGOT)
                .name(Component.text("§bBank"))
                .asGuiItem(event -> {
                    // TODO: Bank Inventar
                })
        );

        this.setItem(15, ItemBuilder
                .from(Material.NETHER_STAR)
                .name(Component.text("§bMitglieder"))
                .asGuiItem(event -> {
                    new ClanMemberGui(clan).open(event.getWhoClicked(), 0);
                })
        );

        if(clan.isOwner(player)){
            this.setItem(35, ItemBuilder
                    .from(Material.RED_WOOL)
                    .name(Component.text("§cClan löschen"))
                    .asGuiItem(event -> {
                        player.closeInventory();
                        Bukkit.dispatchCommand(event.getWhoClicked(), "clan delete");
                    })
            );
        }else{
            this.setItem(35, ItemBuilder
                    .from(Material.RED_DYE)
                    .name(Component.text("§cClan verlassen"))
                    .asGuiItem(event -> {
                        player.closeInventory();
                        Bukkit.dispatchCommand(event.getWhoClicked(), "clan leave");
                    })
            );
        }

        super.open(player);
    }
}
