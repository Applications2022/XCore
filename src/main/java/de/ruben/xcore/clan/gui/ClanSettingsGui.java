package de.ruben.xcore.clan.gui;

import de.ruben.xcore.clan.gui.conversation.ChangeNameConversation;
import de.ruben.xcore.clan.gui.conversation.ChangeTagConversation;
import de.ruben.xcore.clan.gui.conversation.TagColorConversation;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanRank;
import de.ruben.xcore.clan.model.ClanStatus;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClanSettingsGui extends Gui {
    public ClanSettingsGui(Clan clan1) {
        super(6, "§9§lClan §8| §9§lEinstellungen", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));

        this.setItem(49, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));

        this.setItem(45, ItemPreset.backItem(event -> {
            if(new ClanPlayerService().isInClan(event.getWhoClicked().getUniqueId())){
                new ClanGui().open(((Player) event.getWhoClicked()), new ClanPlayerService().getClan(event.getWhoClicked().getUniqueId()));
            }else{
                new NoClanGui().open(((Player) event.getWhoClicked()));
            }
        }));

        this.setItem(12, ItemBuilder
                .from(Material.NETHERITE_INGOT)
                .name(Component.text("§bClan-Tag ändern"))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    if(new ClanPlayerService().isInClan(player.getUniqueId())){
                        Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                        if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.CHANGE_TAG_NAME)){
                            player.closeInventory();
                            new ChangeTagConversation(clan).getConversationFactory().buildConversation(player).begin();
                        }else{
                            player.closeInventory();
                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDazu hast du in deinem Clan keine Rechte!");
                        }
                    }else{
                        player.closeInventory();
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu bist in keinem Clan!");
                    }
                })
        );

        this.setItem(14, ItemBuilder
                .from(Material.NAME_TAG)
                .name(Component.text("§bName ändern"))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    if(new ClanPlayerService().isInClan(player.getUniqueId())){
                        Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                        if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.CHANGE_NAME)){
                            player.closeInventory();
                            new ChangeNameConversation(clan).getConversationFactory().buildConversation(player).begin();
                        }else{
                            player.closeInventory();
                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDazu hast du in deinem Clan keine Rechte!");
                        }
                    }else{
                        player.closeInventory();
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu bist in keinem Clan!");
                    }
                })
        );

        this.setItem(20, ItemBuilder
                .from(ClanStatus.getClanStatusItemStack(clan1.getClanStatus()))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    if(new ClanPlayerService().isInClan(player.getUniqueId())){
                        Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                        if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.STATUS_SET)){
                            ClanStatus nextStatus = ClanStatus.getNextStatus(clan.getClanStatus());

                            new ClanService().setClanStatus(clan.getId(), nextStatus);

                            this.updateItem(20, ClanStatus.getClanStatusItemStack(nextStatus));
                        }else{
                            player.closeInventory();
                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDazu hast du in deinem Clan keine Rechte!");
                        }
                    }else{
                        player.closeInventory();
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu bist in keinem Clan!");
                    }
                })
        );

        this.setItem(24, ItemBuilder
                .from(Material.GRAY_DYE)
                .name(Component.text("§bTag-Farbe ändern"))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    if(new ClanPlayerService().isInClan(player.getUniqueId())){
                        Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                        if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.CHANGE_TAG_COLOR)){
                            player.closeInventory();
                            new TagColorConversation(clan).getConversationFactory().buildConversation(player).begin();
                        }else{
                            player.closeInventory();
                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDazu hast du in deinem Clan keine Rechte!");
                        }
                    }else{
                        player.closeInventory();
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu bist in keinem Clan!");
                    }
                })
        );

        this.setItem(30, ItemBuilder
                .from(Material.PAPER)
                .name(Component.text("§bSlots"))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    // TODO Status ändern
                })
        );

        this.setItem(32, ItemBuilder
                .from(Material.DIAMOND)
                .name(Component.text("§bRänge"))
                .asGuiItem(event -> {
                    Player player = (Player) event.getWhoClicked();

                    // TODO Clan Rang Inventar
                })
        );
    }
}
