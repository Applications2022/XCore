package de.ruben.xcore.profile.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.currency.account.type.PrivateState;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class ProfileGui extends Gui {

    public ProfileGui(Player player, UUID uuid) {
        super(6, "§9§lProfil §8("+ Bukkit.getOfflinePlayer(uuid).getName()+")", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.disableAllInteractions();

        ProfileService profileService = new ProfileService();
        PlayerProfile playerProfile = profileService.getProfile(uuid);

        this.getFiller().fill(ItemPreset.fillItem(inventoryClickEvent -> {}));

        this.setItem(49, ItemPreset.closeItem(inventoryClickEvent -> this.close(player)));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.setItem(11, ItemBuilder
                .from(Material.CLOCK)
                .name(Component.text("§bZeitliche Daten"))
                .lore(
                        Component.text(" "),
                        Component.text("§7Erstmals beigetreten: §b"+ simpleDateFormat.format(new Date(playerProfile.getFirstJoin()))+"Uhr"),
                        Component.text("§7Zuletzt beigetreten: §b"+ simpleDateFormat.format(new Date(playerProfile.getLastJoin()))+"Uhr"),
                        Component.text(" ")
                )
                .asGuiItem());

        this.setItem(13, ItemBuilder
                .from(Material.SUNFLOWER)
                .name(Component.text("§bTransaktionen"))
                .lore(
                        Component.text(" "),
                        Component.text("§7Summe: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getTransferData().getTransferredAmount())+"€"),
                        Component.text("§7Anzahl: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getTransferData().getTransferCount())),
                        Component.text(" ")
                )
                .asGuiItem());

        this.setItem(15, ItemBuilder
                .from(Material.WRITABLE_BOOK)
                .name(Component.text("§bChat Statistik"))
                .lore(
                        Component.text(" "),
                        Component.text("§7Nachrichten: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getMessages())),
                        Component.text("§7Commands: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getCommands())),
                        Component.text(" ")
                )
                .asGuiItem());
        CashAccount cashAccount = XCurrency.getInstance().getCashService().getAccount(uuid);
        this.setItem(30, ItemBuilder
                .from(Material.CHEST)
                .name(Component.text("§bAnderes"))
                .lore(
                        Component.text(" "),
                        Component.text("§7Skydrops: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getSkyDrops())),
                       cashAccount.getPrivateState() == PrivateState.PUBLIC ? Component.text("§7Bargeld: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(cashAccount.getValue())) : Component.text("§7Bargeld: §cprivat"),
                        Component.text(" ")
                )
                .asGuiItem());

        this.setItem(32, ItemBuilder
                .from(Material.NETHERITE_SWORD)
                .name(Component.text("§bKampf"))
                .lore(
                       Component.text(" "),
                        Component.text("§7Spieler getötet: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getPlayerKills())),
                        Component.text("§7Monster getötet: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getMonsterKills())),
                        Component.text("§7Gestorben: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(playerProfile.getDied())),
                        Component.text(" ")

                )
                .asGuiItem());

        Bukkit.getScheduler().runTask(XCore.getInstance(), () -> this.open(player));

    }
}
