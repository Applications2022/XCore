package de.ruben.xcore.currency.account.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.BankAccount;
import de.ruben.xcore.pagination.PaginatedArrayList;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class OtherBanksGui extends Gui {

    private final PaginatedArrayList paginatedArrayList;

    public OtherBanksGui(Player player, BankAccount bankAccount) {

        super(
                6,
                "§9§lBank",
                Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE)
        );

        this.paginatedArrayList = new PaginatedArrayList(bankAccount.getAccessGrantedAccounts(), 27);

        this.disableAllInteractions();
        this.getFiller().fillBorder(ItemPreset.fillItem(inventoryClickEvent -> {}));

        this.setItem((this.getRows()*9)-9, ItemPreset.backItem(inventoryClickEvent -> new BankGui(player).open(player)));

        this.setItem((this.getRows()*9)-5, ItemPreset.closeItem(inventoryClickEvent -> this.close(player)));
    }

    @Override
    public void open(@NotNull HumanEntity player) {
        super.open(player);

        setPageItems((Player) player, 0);

        this.update();
    }

    public void open(Player player, int page){
        super.open(player);

        setPageItems((Player) player, page);


    }

    private void setPageItems(Player player, int page){
        paginatedArrayList.gotoPage(page);

        XDevApi.getInstance().getxScheduler().async(() -> {
            for(int i = 0; i < 27; i++) {
                if (i >= paginatedArrayList.size()) {
                    break;
                }

                UUID uuid = (UUID) paginatedArrayList.get(i);

                NBTItem nbtItem = new NBTItem(new ItemStack(Material.PLAYER_HEAD));
                nbtItem.setUUID("uuid", uuid);

                this.addItem(ItemBuilder.from(nbtItem.getItem())
                        .setSkullOwner(Bukkit.getOfflinePlayer(uuid))
                        .name(Component.text("§b"+Bukkit.getOfflinePlayer(uuid).getName()+"'s Bank"))
                        .asGuiItem(inventoryClickEvent1 -> {
                            NBTItem nbtItem1 = new NBTItem(inventoryClickEvent1.getCurrentItem());

                            XCurrency.getInstance().getBankService().getAccountAsync(nbtItem1.getUUID("uuid"), bankAccount1 -> {
                                if(bankAccount1.isFrozen()){
                                    Bukkit.getScheduler().runTask(XCore.getInstance(), () -> {
                                        inventoryClickEvent1.getWhoClicked().closeInventory();
                                        inventoryClickEvent1.getWhoClicked().sendMessage(XDevApi.getInstance().getMessages().getMessage("prefix") + "§cFehler: §7Das Konto von §b"+Bukkit.getOfflinePlayer(nbtItem.getUUID("uuid")).getName()+" §7ist eingefroren! Melde dich bei ihm oder bei einem unserer Administratoren um mehr Infos zu erhalten!");
                                    });
                                    return;
                                }

                                Gui gui = new OtherBankGui((Player) inventoryClickEvent1.getWhoClicked(), nbtItem1.getUUID("uuid"), bankAccount1);
                                Bukkit.getScheduler().runTask(XCore.getInstance(), () -> gui.open(player));
                            });
                        }));
            }

            if(paginatedArrayList.isNextPageAvailable()){
                this.setItem(50, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                    Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                    new OtherBanksGui(clicked, XCurrency.getInstance().getBankService().getAccount(clicked.getUniqueId())).open(clicked, paginatedArrayList.getPageIndex()+1);
                }));
            }

            if(paginatedArrayList.isPreviousPageAvailable()){
                this.setItem(48, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                    Player clicked = (Player) inventoryClickEvent.getWhoClicked();
                    new OtherBanksGui(clicked, XCurrency.getInstance().getBankService().getAccount(clicked.getUniqueId())).open(clicked, paginatedArrayList.getPageIndex()-1);
                }));
            }

            this.update();
        });

    }

}
