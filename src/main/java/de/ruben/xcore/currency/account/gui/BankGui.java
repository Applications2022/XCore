package de.ruben.xcore.currency.account.gui;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class BankGui extends Gui {
    public BankGui(Player player) {
        super(3, "§9§lBank", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));
        this.disableAllInteractions();

        this.getFiller().fill(ItemPreset.fillItem(inventoryClickEvent -> {}));

        BankAccount bankAccount = XCurrency.getInstance().getBankService().getAccount(player.getUniqueId());

        GuiItem otherBanks = ItemBuilder.from(Material.MAP)
                .name(Component.text("§bExterne Banken"))
                .lore(List.of(
                        Component.text(" "),
                        Component.text("§aInfo:"),
                        Component.text("§7Andere Spieler haben die Möglichkeit"),
                        Component.text("§7dir Zugriff auf ihre Bank zu geben."),
                        Component.text("§7Mit einem Klick kannst du dir diese"),
                        Component.text("§7Konten auflisten lassen und"),
                        Component.text("§7von dort aus auch auf"),
                        Component.text("§7auf sie zugreifen."),
                        Component.text(" ")
                ))
                .asGuiItem(inventoryClickEvent -> new OtherBanksGui(player, bankAccount).open(player, 0));

        GuiItem myBank = ItemBuilder.from(Material.GOLD_INGOT)
                .name(Component.text("§bDeine Bank"))
                .lore(List.of(
                        Component.text(" "),
                        Component.text("§aInfo:"),
                        Component.text("§7Mit einem Klick kannst du"),
                        Component.text("§7auf dein Konto zugreifen."),
                        Component.text(" ")
                ))
                .asGuiItem(inventoryClickEvent -> {

                    if(bankAccount.isFrozen()){
                        player.closeInventory();
                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Dein Konto ist eingefroren! Bitte melde dich für weitere Informationen bei einem Admin!");
                        return;
                    }

                    new MyBankGui(player, bankAccount).open(player);
                });

        this.setItem(11, otherBanks);
        this.setItem(15, myBank);
        this.setItem(22, ItemPreset.closeItem(inventoryClickEvent -> this.close(player)));
    }



}
