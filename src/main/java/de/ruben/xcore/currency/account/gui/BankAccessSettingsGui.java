package de.ruben.xcore.currency.account.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.LabyGUITemplate;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import de.ruben.xdevapi.custom.gui.response.ConfirmationResponse;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class BankAccessSettingsGui extends Gui {
    private final PaginatedArrayList paginatedArrayList;


    public BankAccessSettingsGui(Player player, BankAccount bankAccount) {
        super(6, "§9§lBank", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.disableAllInteractions();

        this.paginatedArrayList = new PaginatedArrayList(bankAccount.getAccessGrantedPlayers(),27);

        this.getFiller().fillBorder(ItemPreset.fillItem(inventoryClickEvent -> {}));

        this.setItem(45, ItemPreset.backItem(inventoryClickEvent -> new MyBankGui(player, bankAccount).open(player)));

        this.setItem(49, ItemPreset.closeItem(inventoryClickEvent -> this.close(player)));

        this.setItem(53, ItemBuilder.from(Material.ANVIL)
                .name(Component.text("§bSpieler hinzufügen"))
                .asGuiItem(inventoryClickEvent -> {
                        if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId()) && XDevApi.getInstance().getLabyUsers().getVersionAsLong(player.getUniqueId()) >= 377){
                            LabyGUITemplate.createInput(player, "Gebe den Namen des Spielers ein, den du hinzufügen willst.", "Hier der Name", 20, (uuid, s) -> {

                                Player player1 = Bukkit.getPlayer(uuid);
                                String input = null;

                                try {
                                    input = new ObjectMapper().readTree(s).get("value").asText();
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }

                                if(input == null || input.equals("") || input.equals(" ")) return;

                                if(input.equals(player1.getName())){
                                    player1.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du kannst dich nicht selber auf die Zugriffsliste deines Kontos hinzufügen!");
                                    return;
                                }

                                String finalInput = input;
                                XCurrency.getInstance().getBankService().existUser(Bukkit.getOfflinePlayer(input).getUniqueId(), (bankAccount1, aBoolean) -> {
                                    if(aBoolean){

                                        NoLabyGUITemplate.createStandardSelectConfirmationGUI(player1, ((gui, confirmationResponse, player2) -> {

                                            if(confirmationResponse == ConfirmationResponse.NO){
                                                gui.close(player2);
                                            }else if(confirmationResponse == ConfirmationResponse.YES){
                                                gui.close(player2);

                                                if(bankAccount1.getAccessGrantedAccounts().contains(player2.getUniqueId())){
                                                    player2.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§b" + Bukkit.getOfflinePlayer(finalInput).getName() + " §7ist bereits auf deiner Zugriffsliste!");
                                                }else {
                                                    XCurrency.getInstance().getBankService().addAccessGrantedAccount(Bukkit.getOfflinePlayer(finalInput).getUniqueId(), player2.getUniqueId());
                                                    XCurrency.getInstance().getBankService().addAccessGrantedPlayer(player2.getUniqueId(), Bukkit.getOfflinePlayer(finalInput).getUniqueId());

                                                    player2.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du hast §b" + Bukkit.getOfflinePlayer(finalInput).getName() + " §7erfolgreich zu deiner Zugriffsliste hinzugefügt!");
                                                    if(Bukkit.getOfflinePlayer(finalInput).isOnline()) Bukkit.getPlayer(finalInput).sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du wurdest von §b"+player2.getName()+" §7zu seiner Bank-Zugriffsliste hinzugefügt!");
                                                }
                                            }

                                        }));

                                    }else{
                                        player1.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") +"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                                    }
                                });

                            });
                        }else{
                            NoLabyGUITemplate.createStringInput(XDevApi.getInstance(), "§9Name des Spielers.", "Name", s -> {
                                if(s == null || s.equals("") || s.equals(" ")) return AnvilGUI.Response.close();

                                if(s.equals(player.getName())){
                                    player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Du kannst dich nicht selber auf die Zugriffsliste deines Kontos hinzufügen!");
                                    return AnvilGUI.Response.close();
                                }

                                XCurrency.getInstance().getBankService().existUser(Bukkit.getOfflinePlayer(s).getUniqueId(), (bankAccount1, aBoolean) -> {
                                    if(aBoolean){

                                        NoLabyGUITemplate.createStandardSelectConfirmationGUI(player, ((gui, confirmationResponse, player2) -> {

                                            if(confirmationResponse == ConfirmationResponse.NO){
                                                gui.close(player2);
                                            }else if(confirmationResponse == ConfirmationResponse.YES){
                                                gui.close(player2);

                                                if(bankAccount1.getAccessGrantedAccounts().contains(player2.getUniqueId())){
                                                    player2.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§b" + Bukkit.getOfflinePlayer(s).getName() + " §7ist bereits auf deiner Zugriffsliste!");
                                                }else {
                                                    XCurrency.getInstance().getBankService().addAccessGrantedAccount(Bukkit.getOfflinePlayer(s).getUniqueId(), player2.getUniqueId());
                                                    XCurrency.getInstance().getBankService().addAccessGrantedPlayer(player2.getUniqueId(), Bukkit.getOfflinePlayer(s).getUniqueId());

                                                    player2.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du hast §b" + Bukkit.getOfflinePlayer(s).getName() + " §7erfolgreich zu deiner Zugriffsliste hinzugefügt!");
                                                    if(Bukkit.getOfflinePlayer(s).isOnline()) Bukkit.getPlayer(s).sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du wurdest von §b"+player2.getName()+" §7zu seiner Bank-Zugriffsliste hinzugefügt!");

                                                }
                                            }

                                        }));

                                    }else{
                                        player.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") +"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                                    }
                                });

                                return AnvilGUI.Response.close();
                            }).open(player);
                        }
                })
        );

    }


    public void open(@NotNull Player player, int page) {
        super.open(player);

        paginatedArrayList.gotoPage(page);

        XDevApi.getInstance().getxScheduler().async(() -> {

            for(int i = 0; i < 27; i++){
                if(i >= paginatedArrayList.size()){
                    break;
                }

                UUID uuid = (UUID) paginatedArrayList.get(i);

                NBTItem nbtItem = new NBTItem(new ItemStack(Material.PLAYER_HEAD));
                nbtItem.setUUID("uuid", uuid);

                this.addItem(ItemBuilder.from(nbtItem.getItem()).setSkullOwner(Bukkit.getOfflinePlayer(uuid)).name(Component.text("§b"+Bukkit.getOfflinePlayer(uuid).getName())).lore(Component.text(" "), Component.text("§7Klick: Spieler von der"), Component.text("§7Berechtignungsliste entfernen"), Component.text(" "))
                        .asGuiItem(inventoryClickEvent -> {
                            NBTItem nbtItem1 = new NBTItem(inventoryClickEvent.getCurrentItem());
                            UUID uuid1 = nbtItem1.getUUID("uuid");

                            NoLabyGUITemplate.createStandardSelectConfirmationGUI(player, "§9§l"+Bukkit.getOfflinePlayer(uuid1).getName()+" Entfernen?", (gui, confirmationResponse, player1) -> {
                                if(confirmationResponse == ConfirmationResponse.YES){
                                    gui.close(player1);
                                    XCurrency.getInstance().getBankService().removeAccessGrantedAccount(uuid1, player1.getUniqueId(), bankAccount -> XCurrency.getInstance().getBankService().removeAccessGrantedPlayer(player1.getUniqueId(), uuid1, bankAccount1 -> {
                                        player1.sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du hast §b"+Bukkit.getOfflinePlayer(uuid1).getName()+" §7erfolgreich aus der Zugriffsliste deines Kontos entfernt!");
                                        if(Bukkit.getOfflinePlayer(uuid1).isOnline()) Bukkit.getPlayer(uuid1).sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§7Du wurdest von §b"+player1.getName()+" §7aus seiner Bank-Zugriffsliste entfernt!");

                                    }));
                                }else if(confirmationResponse == ConfirmationResponse.NO){
                                    gui.close(player1);
                                    player1.closeInventory();
                                }
                            });
                        })
                );
            }

            if(paginatedArrayList.isNextPageAvailable()){
                this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                    Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                    new BankAccessSettingsGui(clicked, XCurrency.getInstance().getBankService().getAccount(clicked.getUniqueId())).open(clicked, paginatedArrayList.getPageIndex()+1);
                }));
            }

            if(paginatedArrayList.isPreviousPageAvailable()){
                this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                    Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                    new BankAccessSettingsGui(clicked, XCurrency.getInstance().getBankService().getAccount(clicked.getUniqueId())).open(clicked, paginatedArrayList.getPageIndex()-1);
                }));
            }

            this.update();
        });
    }
}
