package de.ruben.xcore.itemstorage.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.gui.items.StorageUpdateItemPreset;
import de.ruben.xcore.itemstorage.model.BarrelStorage;
import de.ruben.xcore.itemstorage.service.BarrelStorageService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.message.MessageService;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;

public class BarrelStorageUpgradeGui extends Gui {

    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    public BarrelStorageUpgradeGui(Player player, BarrelStorage barrelStorage, Block block) {
        super(6, "§9§lUpgrade §8Lvl."+barrelStorage.getLevel()+" ➜ Lvl."+(barrelStorage.getLevel()+1), Set.of());

        this.enableItemPlace();
        this.enableItemTake();
        this.enableItemSwap();

        this.getFiller().fill(ItemPreset.fillItem(inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));

        this.updateItem(22, ItemBuilder.from(Material.AIR).asGuiItem());

        this.updateItem(49, ItemPreset.closeItem(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            this.close(player);
        }));

        BarrelStorageService barrelStorageService = new BarrelStorageService();
        this.setItem(20, ItemBuilder.from(barrelStorageService.forUpgradeNeededStacks(barrelStorage.getLevel()+1)).asGuiItem(inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));

        this.setItem(24, ItemBuilder.from(Material.GREEN_WOOL)
                .name(Component.text("§2§lBestätigen"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Klicke um das Upgraden"),
                        Component.text("§7➥ des §9ItemSpeichers §7zu"),
                        Component.text("§7➥ bestätigen!")
                ).asGuiItem(inventoryClickEvent -> {

                    inventoryClickEvent.setCancelled(true);

                    ItemStack stackNeeded = this.getInventory().getItem(20);

                    ItemStack stackToUprade = this.getInventory().getItem(22);

                    TileState tileState = (TileState) block.getState();


                    if(stackToUprade == null || stackToUprade.getType() == Material.AIR){
                        removeOpener(block);
                        this.close(player, false);
                        return;
                    }

                    if(StorageUpdateItemPreset.isStorageUpgradeStack(this.getInventory().getItem(20)) && !StorageUpdateItemPreset.isStorageUpgradeStack(stackToUprade)){
                        player.getInventory().addItem(stackToUprade);
                        player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du musst Speicher Upgrade Items verwenden, um diesen Item Speicher upgraden zu können!");
                        removeOpener(block);
                        this.close(player, false);
                        return;
                    }

                    if(StorageUpdateItemPreset.isExpandedStorageUpgradeStack(this.getInventory().getItem(20)) && !StorageUpdateItemPreset.isExpandedStorageUpgradeStack(stackToUprade)){
                        player.getInventory().addItem(stackToUprade);
                        player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du musst Erweiterte Speicher Upgrade Items verwenden, um diesen Item Speicher upgraden zu können!");
                        removeOpener(block);
                        this.close(player, false);
                        return;
                    }

                    if(stackNeeded.getAmount() > stackToUprade.getAmount()){
                        player.getInventory().addItem(stackToUprade);
                        String upgradeItemString = StorageUpdateItemPreset.isStorageUpgradeStack(stackNeeded) ? "Speicher Upgrade" : "Erweiterte Speicher Upgrade";
                        player.sendMessage(messageService.getMessage("prefix")+"§cFehler: Du benötigst "+stackNeeded.getAmount()+" "+upgradeItemString+" Items um diesen Item Speicher upgraden zu können!");
                        removeOpener(block);
                        this.close(player, false);
                        return;
                    }

                    if(stackToUprade.getAmount() > stackNeeded.getAmount()){
                        stackToUprade.setAmount(stackToUprade.getAmount()- stackNeeded.getAmount());
                        player.getInventory().addItem(stackToUprade);
                    }

                    barrelStorage.upgradePages(barrelStorage.getLevel()+1);

                    barrelStorage.setLevel(barrelStorage.getLevel()+1);

                    PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();

                    persistentDataContainer = barrelStorage.updateBarrel(persistentDataContainer);

                    tileState.update();

                    player.sendMessage(messageService.getMessage("prefix")+"§7Dieser §9Item Speicher §7ist nun Level §b"+(barrelStorage.getLevel())+"§7!");

                    removeOpener(block);
                    this.close(player, false);

                }));

        this.setCloseGuiAction(inventoryCloseEvent -> {

            removeOpener(block);

            ItemStack stackToUprade = inventoryCloseEvent.getInventory().getItem(22);

            if(stackToUprade == null || stackToUprade.getType() == Material.AIR){
                return;
            }

            player.getInventory().addItem(stackToUprade);
        });

        Bukkit.getScheduler().runTask(XCore.getInstance(), () -> this.open(player));
    }

    private void removeOpener(Block block){
        TileState tileState = (TileState) block.getState();

        PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();

        if(persistentDataContainer.has(new NamespacedKey(XCore.getInstance(), "isOpened"), PersistentDataType.STRING)) {
            persistentDataContainer.remove(new NamespacedKey(XCore.getInstance(), "isOpened"));
            tileState.update();
        }
    }
}
