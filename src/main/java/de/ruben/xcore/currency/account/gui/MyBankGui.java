package de.ruben.xcore.currency.account.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.currency.account.type.Transaction;
import de.ruben.xcore.profile.model.TransferData;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.LabyGUITemplate;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MyBankGui extends Gui {

    private final ProfileService profileService = new ProfileService();

    public MyBankGui(Player player, BankAccount bankAccount) {
        super(4, "§9§lBank", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.getFiller().fill(ItemPreset.fillItem(inventoryClickEvent -> {}));

        this.setItem(10, ItemBuilder.from(Material.CHEST).name(Component.text("§bGeld einzahlen")).asGuiItem(inventoryClickEvent -> {

            if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId()) && XDevApi.getInstance().getLabyUsers().getVersionAsLong(player.getUniqueId()) > 377){

                LabyGUITemplate.createInput(player, "Gebe den Betrag an, den du einzahlen willst!", "Der Betrag hier!", 10, (uuid, s) -> {
                    Player player1 = Bukkit.getPlayer(uuid);
                    String input = null;

                    try {
                        input = new ObjectMapper().readTree(s).get("value").asText();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if(input != null && !input.equals("") && !input.equals(" ")) {

                        if (isDouble(input)) {
                            player1.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du musst eine Zahl als Betrag angeben!");
                            return;
                        }

                        double aDouble = Double.parseDouble(input);

                        CashAccount cashAccount = XCurrency.getInstance().getCashService().getAccount(player.getUniqueId());

                        if (aDouble <= 0) {
                            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du musst einen größeren Betrag angeben!");
                            return;
                        }

                        if (cashAccount.getValue() < aDouble) {
                            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Dazu hast du zu wenig geld in deiner Brieftasche!");
                            return;
                        }

                        depositMoney(player, player.getUniqueId(), aDouble);

                    }
                });

            }else{
                NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY, XDevApi.getInstance(), "§9Geld einzahlen", "§8Betrag",XDevApi.getInstance().getMessages().getMessage("prefix")+"§cFehler: Du musst eine Zahl als Betrag angeben!", aDouble -> {

                    CashAccount cashAccount = XCurrency.getInstance().getCashService().getAccount(player.getUniqueId());

                    if(aDouble <= 0){
                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix")+"§cFehler: §7Du musst einen größeren Betrag angeben!");
                        return AnvilGUI.Response.close();
                    }

                    if(cashAccount.getValue() < aDouble){
                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix")+"§cFehler: §7Dazu hast du zu wenig geld in deiner Brieftasche!");
                        return AnvilGUI.Response.close();
                    }

                    depositMoney(player, player.getUniqueId(), aDouble);

                    return AnvilGUI.Response.close();
                }).open(player);
            }

        }));

        List<Component> transactionsLore = new ArrayList<>();

        transactionsLore.add(Component.text(" "));

        Lists.reverse(bankAccount.getTransactions()).stream().limit(6).forEach(transaction -> transactionsLore.add(Component.text(transaction.getTransactionString(player.getUniqueId()))));

        transactionsLore.add(Component.text(" "));

        this.setItem(12, ItemBuilder.from(Material.TORCH).name(Component.text("§bLetzte Transaktionen")).lore(transactionsLore).asGuiItem());

        this.setItem(14, ItemBuilder.from(Material.GOLD_INGOT).name(Component.text("§7Aktuell auf dem Konto: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(bankAccount.getValue())+"€")).asGuiItem());

        this.setItem(16, ItemBuilder.from(Material.DISPENSER).name(Component.text("§bGeld auszahlen")).asGuiItem(inventoryClickEvent -> {

            if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId()) && XDevApi.getInstance().getLabyUsers().getVersionAsLong(player.getUniqueId()) > 377){

                LabyGUITemplate.createInput(player, "Gebe den Betrag an, den du auszahlen willst!", "Der Betrag hier!", 10, (uuid, s) -> {
                    Player player1 = Bukkit.getPlayer(uuid);
                    String input = null;

                    try {
                        input = new ObjectMapper().readTree(s).get("value").asText();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    if(input != null && !input.equals("") && !input.equals(" ")) {

                        if (isDouble(input)) {
                            player1.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du musst eine Zahl als Betrag angeben!");
                            return;
                        }

                        double aDouble = Double.parseDouble(input);

                        BankAccount bankAccount1 = XCurrency.getInstance().getBankService().getAccount(player.getUniqueId());

                        if (aDouble <= 0) {
                            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du musst einen größeren Betrag angeben!");
                            return;
                        }

                        if (bankAccount1.getValue() < aDouble) {
                            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Dazu hast du zu wenig Geld auf deiner Bank!");
                            return;
                        }

                        withdrawMoney(player, player.getUniqueId(), aDouble);

                    }
                });

            }else{
                NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY, XDevApi.getInstance(), "§9Geld auszahlen", "§8Betrag",XDevApi.getInstance().getMessages().getMessage("prefix")+"§cFehler: Du musst eine Zahl als Betrag angeben!", aDouble -> {

                    BankAccount bankAccount1 = XCurrency.getInstance().getBankService().getAccount(player.getUniqueId());

                    if (aDouble <= 0) {
                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du musst einen größeren Betrag angeben!");
                        return AnvilGUI.Response.close();
                    }

                    if (bankAccount1.getValue() < aDouble) {
                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Dazu hast du zu wenig Geld auf deiner Bank!");
                        return AnvilGUI.Response.close();
                    }

                    withdrawMoney(player, player.getUniqueId(), aDouble);

                    return AnvilGUI.Response.close();
                }).open(player);
            }

        }));

        this.setItem(27, ItemPreset.backItem(inventoryClickEvent -> new BankGui(player).open(player)));

        this.setItem(31, ItemPreset.closeItem(inventoryClickEvent -> this.close(player)));

        this.setItem(35, ItemBuilder.from(Material.PAPER).name(Component.text("§bZugriffsberechtigte Verwalten")).asGuiItem(inventoryClickEvent -> {
            Player clicked = (Player) inventoryClickEvent.getWhoClicked();
            new BankAccessSettingsGui(clicked, bankAccount).open(clicked, 0);
        }));
    }

    private void depositMoney(Player player, UUID playerUUID, double aDouble) {
        XCurrency.getInstance().getCashService().removeValue(playerUUID, aDouble, cashAccount1 -> {
            BankAccount bankAccount1 = XCurrency.getInstance().getBankService().getAccount(playerUUID);
            List<Transaction> transactions = bankAccount1.getTransactions();
            transactions.add(new Transaction(true, aDouble, playerUUID, new Date(System.currentTimeMillis())));

            bankAccount1.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));
            bankAccount1.setValue(bankAccount1.getValue()+aDouble);

            XCurrency.getInstance().getBankService().updateBankAccount(playerUUID, bankAccount1);
            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix")+"§7Du hast erfolgreich §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(aDouble)+"€ §7auf deine Bank eingezahlt!");

            updateTranserrefData(player, aDouble);

        });
    }

    private void withdrawMoney(Player player, UUID playerUUID, double aDouble){
        BankAccount bankAccount1 = XCurrency.getInstance().getBankService().getAccount(playerUUID);
        List<Transaction> transactions = bankAccount1.getTransactions();
        transactions.add(new Transaction(false, aDouble, playerUUID, new Date(System.currentTimeMillis())));

        bankAccount1.setTransactions(Lists.reverse(Lists.reverse(transactions).stream().limit(10).collect(Collectors.toList())));
        bankAccount1.setValue(bankAccount1.getValue()-aDouble);

        XCurrency.getInstance().getBankService().updateBankAccount(playerUUID, bankAccount1, bankAccount -> XCurrency.getInstance().getCashService().addValue(playerUUID, aDouble, cashAccount2 -> {
            player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix")+"§7Du hast dir erfolgreich §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(aDouble)+"€ §7ausgezahlt!");
            updateTranserrefData(player, aDouble);
        }));
    }

    public void updateTranserrefData(Player player, double aDouble) {
        profileService.getProfileAsync(player.getUniqueId(), playerProfile -> {
            TransferData transferData = playerProfile.getTransferData();
            transferData.setTransferCount(transferData.getTransferCount()+1);
            transferData.setTransferredAmount(transferData.getTransferredAmount()+aDouble);
            playerProfile.setTransferData(transferData);

            profileService.updateProfile(player.getUniqueId(), playerProfile);
        });
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
