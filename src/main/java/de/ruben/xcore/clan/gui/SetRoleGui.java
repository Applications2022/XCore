package de.ruben.xcore.clan.gui;

import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanMember;
import de.ruben.xcore.clan.model.ClanRank;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.job.metrix.JobXpMetrix;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SetRoleGui extends Gui {

    private Clan clan;

    public SetRoleGui(Clan clan, Player player) {
        super((Math.round(clan.getFilteredRanks(player).size()/7+(clan.getFilteredRanks(player).size()%7) == 0 ? 0 : 1))+2, "§9§lClan §8| §9§lRolle Setzen", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));
        this.clan = clan;

        this.getFiller().fillBorder(ItemPreset.fillItem(event -> {}));
        this.setItem(this.getInventory().getSize()-5, ItemPreset.closeItem(event -> event.getWhoClicked().closeInventory()));
        this.setItem(this.getInventory().getSize()-9, ItemPreset.backItem(event -> new ClanMemberGui(clan).open(event.getWhoClicked(), 0)));

    }

    public void open(@NotNull HumanEntity player, ClanMember clanMember) {
        ClanMember playerMember = clan.getClanMembers().get(player.getUniqueId().toString());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(clanMember.getId());

        clan.getFilteredRanks((Player) player).forEach((s, clanRank) -> {

            boolean isMemberRank = clanMember.getClanRank(clan).getUuid().toString().equals(clanRank.getUuid().toString());

            ItemStack itemStack = ItemBuilder
                                        .from(Material.EMERALD)
                                        .amount(clanRank.getWeight())
                                        .name(Component.text(XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(clanRank.getColorTag())+clanRank.getName()))
                                        .lore(
                                                Component.text(" "),
                                                Component.text(isMemberRank ? "§b"+offlinePlayer.getName()+" §7hat diesen Clan Rang!" : "§7Klicke um Rolle zu setzen!")
                                        )
                                        .build();

            if(isMemberRank){
                itemStack.addUnsafeEnchantment(Enchantment.LURE, 1);
                itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            this.addItem(ItemBuilder.from(itemStack).asGuiItem(event -> {
                if(!isMemberRank){
                    new ClanService().updateClanRank(clan.getId(), clanMember, clanRank);
                    new SetRoleGui(clan, (Player) event.getWhoClicked()).open(player, clanMember);
                }
            }));


        });
        super.open(player);
    }
}
