package de.ruben.xcore.itemstorage.gui;

import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.XItemStorage;
import de.ruben.xcore.itemstorage.model.BarrelStorage;
import de.ruben.xcore.itemstorage.model.Page;
import de.ruben.xcore.itemstorage.service.BarrelStorageService;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarrelStorageGui extends Gui {

    private final ExecutorService executorService = XItemStorage.getInstance().getExecutorService();

    private final BarrelStorageService barrelStorageService = new BarrelStorageService();

    public BarrelStorageGui(Player player, BarrelStorage barrelStorage, Block block, int page) {
        super(barrelStorage.getPages().get(page).getRows() + 2, "§9Lvl." + barrelStorage.getLevel() + " Item Speicher §8(Seite " + page + "§8/" + barrelStorage.getPages().size() + "§8)", Set.of());

        AtomicBoolean removeOpener = new AtomicBoolean(true);

        this.enableItemPlace();
        this.enableItemTake();

        this.getFiller().fillBorder(ItemPreset.fillItem(inventoryClickEvent -> inventoryClickEvent.setCancelled(true)));

        this.setItem((this.getRows() * 9) - 5, ItemPreset.closeItem(inventoryClickEvent -> {
            inventoryClickEvent.setCancelled(true);
            this.close(player);
        }));

        if (barrelStorage.hasNextPage(page)) {
            this.setItem((this.getRows() * 9) - 4, ItemBuilder.from(Material.MAP).name(Component.text("§9Nächste Seite")).asGuiItem(inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                removeOpener.set(false);
                new BarrelStorageGui(player, barrelStorage, block, page + 1);
            }));
        }

        if (barrelStorage.hasPreviousPage(page)) {
            this.setItem((this.getRows() * 9) - 6, ItemBuilder.from(Material.MAP).name(Component.text("§9Letzte Seite")).asGuiItem(inventoryClickEvent -> {
                inventoryClickEvent.setCancelled(true);
                removeOpener.set(false);
                new BarrelStorageGui(player, barrelStorage, block, page - 1);
            }));
        }

        this.setCloseGuiAction(inventoryCloseEvent -> {

            TileState tileState = (TileState) block.getState();

            CompletableFuture.runAsync(() -> {
                PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();

                if(removeOpener.get()){
                    if(persistentDataContainer.has(new NamespacedKey(XCore.getInstance(), "isOpened"), PersistentDataType.STRING)) {
                        persistentDataContainer.remove(new NamespacedKey(XCore.getInstance(), "isOpened"));
                    }
                }

                HashMap<Integer, Page> pageMap = barrelStorage.getPages();

                if (pageMap.containsKey(page)) {
                    pageMap.replace(page, new Page(
                            barrelStorage.getPages().get(page).getRows(),
                            barrelStorageService.getItemStacks(this, player)
                    ));
                } else {
                    pageMap.put(page, new Page(
                            barrelStorage.getPages().get(page).getRows(),
                            barrelStorageService.getItemStacks(this, player)
                    ));
                }

                barrelStorage.setPages(pageMap);

                persistentDataContainer = barrelStorage.updateBarrel(persistentDataContainer);
            }, executorService).thenAccept(unused -> Bukkit.getScheduler().runTask(XCore.getInstance(), (@NotNull Runnable) tileState::update));
        });


        open(player, barrelStorage, page);
    }


    public void open(@NotNull Player player, BarrelStorage barrelStorage, int page) {

        CompletableFuture.runAsync(() -> Arrays.stream(barrelStorage.getPages().get(page).getItemStacksArray()).forEach(itemStack -> {
            if (itemStack != null && itemStack.getType() != Material.AIR && itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

                persistentDataContainer.remove(new NamespacedKey(XCore.getInstance(), "mf-gui"));

                itemStack.setItemMeta(itemMeta);
            }

            this.addItem(ItemBuilder.from(itemStack).asGuiItem());
        }), executorService).thenAccept(unused -> Bukkit.getScheduler().runTask(XCore.getInstance(), () -> {
            this.update();
            super.open(player);
        }));


    }
}
