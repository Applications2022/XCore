package de.ruben.xcore.clan.model;

import de.ruben.xcore.clan.service.ClanService;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ClanStatus {
    OPEN,
    CLOSED,
    ON_REQUEST;

    public static ClanStatus getNextStatus(ClanStatus clanStatus){
        switch (clanStatus){
            case OPEN -> {
                return ON_REQUEST;
            }
            case ON_REQUEST -> {
                return CLOSED;
            }
            case CLOSED -> {
                return OPEN;
            }
        }

        return ON_REQUEST;
    }

    public static ItemStack getClanStatusItemStack(ClanStatus clanStatus){

        String open = (isStatus(clanStatus, ClanStatus.OPEN) ? "§b" : "§7" )+"Geöffnet";
        String on_request = (isStatus(clanStatus, ClanStatus.ON_REQUEST) ? "§b" : "§7" )+"Auf Anfrage";
        String closed = (isStatus(clanStatus, ClanStatus.CLOSED) ? "§b" : "§7" )+"Geschlossen";

        return ItemBuilder
                .from(getMaterial(clanStatus))
                .name(Component.text("§bStatus"))
                .lore(
                        Component.text(" "),
                        Component.text(open),
                        Component.text(on_request),
                        Component.text(closed)
                )
                .build();

    }

    private static Material getMaterial(ClanStatus clanStatus){
        if(clanStatus == ClanStatus.OPEN){
            return Material.SOUL_TORCH;
        }else if(clanStatus == ClanStatus.CLOSED){
            return Material.REDSTONE_TORCH;
        }else{
            return Material.TORCH;
        }
    }

    private static boolean isStatus(ClanStatus clanStatus, ClanStatus clanStatus2){
        return clanStatus == clanStatus2;
    }
}
