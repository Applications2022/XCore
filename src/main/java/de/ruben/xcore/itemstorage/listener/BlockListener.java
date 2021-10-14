package de.ruben.xcore.itemstorage.listener;

import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.XItemStorage;
import de.ruben.xcore.itemstorage.gui.BarrelStorageGui;
import de.ruben.xcore.itemstorage.gui.BarrelStorageUpgradeGui;
import de.ruben.xcore.itemstorage.model.BarrelStorage;
import de.ruben.xcore.itemstorage.service.BarrelStorageService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.message.MessageService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BlockListener implements Listener {
    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    private final BarrelStorageService barrelStorageService = new BarrelStorageService();

    private final ExecutorService executorService = XItemStorage.getInstance().getExecutorService();

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStackToPlace = event.getItemInHand();

        if (itemStackToPlace != null && itemStackToPlace.hasItemMeta()) {
            if (barrelStorageService.isBarrelStorage(itemStackToPlace)) {
                Block block = event.getBlockPlaced();

                BarrelStorage barrelStorage = new BarrelStorage().fromContainer(itemStackToPlace.getItemMeta().getPersistentDataContainer());

                TileState tileState = (TileState) block.getState();

                PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();

                persistentDataContainer.set(new NamespacedKey(XCore.getInstance(), "isBarrelStorage"), PersistentDataType.INTEGER, 1);
                persistentDataContainer = barrelStorage.getContainer(persistentDataContainer);

                tileState.update();

                player.sendActionBar(Component.text("§aInfo: §7Du hast einen §9Item Speicher §8(lvl." + barrelStorage.getLevel() + ") §7platziert!"));
            }

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block != null && block.getType() == Material.BARREL) {
            TileState tileState = (TileState) block.getState();

            if (barrelStorageService.isBarrelStorage(tileState)) {
                event.setDropItems(false);

                if(tileState.getPersistentDataContainer().has(new NamespacedKey(XCore.getInstance(), "isOpened"), PersistentDataType.STRING)){
                    player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Da gerade ein anderer Spieler diesen Item Speicher editiert, kannst du ihn zur Zeit nicht abbauen!");
                    event.setCancelled(true);
                    return;
                }

                CompletableFuture.supplyAsync(() -> new BarrelStorage().fromContainer(tileState.getPersistentDataContainer()), executorService).thenAccept(barrelStorage -> Bukkit.getScheduler().runTask(XCore.getInstance(), () -> block.getWorld().dropItem(block.getLocation(), barrelStorageService.getBarrelStorageItem(barrelStorage))));
            }
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();

            Block block = event.getClickedBlock();

            if (block != null && block.getType() == Material.BARREL) {
                TileState tileState = (TileState) block.getState();

                if (barrelStorageService.isBarrelStorage(tileState)) {
                    event.setCancelled(true);

                    CompletableFuture.supplyAsync(() -> new BarrelStorage().fromContainer(tileState.getPersistentDataContainer()), executorService).thenAccept(barrelStorage -> {
                        PersistentDataContainer persistentDataContainer = tileState.getPersistentDataContainer();
                        if(persistentDataContainer.has(new NamespacedKey(XCore.getInstance(), "isOpened"), PersistentDataType.STRING)){
                            player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Da gerade ein anderer Spieler diesen Item Speicher editiert, kannst du zur Zeit nicht auf ihn zugreifen!");
                            return;
                        }else {
                            if(player.isSneaking() && barrelStorage.getLevel() >= 15){
                                player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Dieser Item Speicher hat bereits das maximale Level (15) erreicht!");
                                return;
                            }else {
                                persistentDataContainer.set(new NamespacedKey(XCore.getInstance(), "isOpened"), PersistentDataType.STRING, player.getName());
                                Bukkit.getScheduler().runTask(XCore.getInstance(), (@NotNull Runnable) tileState::update);
                            }
                        }

                        if (player.isSneaking()) {
                                new BarrelStorageUpgradeGui(player, barrelStorage, block);
                        } else {
                            new BarrelStorageGui(player, barrelStorage, block, 1);
                        }
                    });
                }
            }
        }
    }
}
