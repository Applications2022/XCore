package de.ruben.xcore.itemstorage.service;

import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.gui.items.StorageUpdateItemPreset;
import de.ruben.xcore.itemstorage.model.BarrelStorage;
import de.ruben.xcore.itemstorage.model.Page;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.util.type.StackPile;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class BarrelStorageService {

    public ItemStack getBarrelStorageItem(BarrelStorage barrelStorage){
        ItemStack itemStack = ItemBuilder
                .from(Material.BARREL)
                .name(Component.text("§9Item Speicher"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Level: §b"+barrelStorage.getLevel()),
                        Component.text("§7➥ Seiten: §b"+barrelStorage.getPages().size()),
                        Component.text("§7➥ Plätze: §b"+getSlots(barrelStorage.getPages().values())),
                        Component.text(" ")

                )
                .flags(ItemFlag.HIDE_ENCHANTS)
                .build();

        ItemMeta itemMeta = itemStack.getItemMeta();

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        persistentDataContainer.set(new NamespacedKey(XCore.getInstance(), "isBarrelStorage"), PersistentDataType.INTEGER, 1);
        persistentDataContainer = barrelStorage.getContainer(persistentDataContainer);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack[] getItemStacks(Gui gui, Player player){
        List<ItemStack> stackList = new ArrayList<>();
        int rows = gui.getRows();
        for(int i = 0; i < rows * 9; ++i) {
            if (i <= 8 || i >= rows * 9 - 9 || i == 9 || i == 18 || i == 27 || i == 36 || i == 17 || i == 26 || i == 35 || i == 44) {

            }else{
                if(gui.getInventory().getItem(i) != null) {
                    if(isBarrelStorage(gui.getInventory().getItem(i)) || gui.getInventory().getItem(i).getType() == Material.SHULKER_BOX){
                        int finalI = i;
                        stackList.add(new ItemStack(Material.AIR));
                        if(XDevApi.getInstance().getxUtil().getBukkitInventoryUtil().hasStorageContentSpaceFor(player.getInventory(), new StackPile(gui.getInventory().getItem(i)))){
                            Bukkit.getScheduler().runTask(XCore.getInstance(), () -> player.getInventory().addItem(gui.getInventory().getItem(finalI)));
                        }else{
                            Bukkit.getScheduler().runTask(XCore.getInstance(), () -> player.getWorld().dropItemNaturally(player.getLocation(), gui.getInventory().getItem(finalI)));
                        }
                    }else {
                        stackList.add(gui.getInventory().getItem(i));
                    }
                }else{
                    stackList.add(new ItemStack(Material.AIR));
                }
            }
        }

        return stackList.toArray(new ItemStack[0]);
    }

    public boolean isBarrelStorage(ItemStack itemStack){
        return itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(XCore.getInstance(), "isBarrelStorage"), PersistentDataType.INTEGER);
    }

    public boolean isBarrelStorage(TileState tileState){
        return tileState.getPersistentDataContainer().has(new NamespacedKey(XCore.getInstance(), "isBarrelStorage"), PersistentDataType.INTEGER);
    }

    public Integer getSlots(Collection<Page> pages){
        return pages.stream().map(Page::getRows).collect(Collectors.toList()).stream().mapToInt(value -> value).sum()*7;
    }

    public BarrelStorage getBarrelStorage(int level){
        HashMap<Integer, Page> pageMap = new HashMap<>();


        switch (level) {
            case 1:
                pageMap.put(1, new Page(1, new ItemStack[]{}));
                break;
            case 2:
                pageMap.put(1, new Page(2, new ItemStack[]{}));
                break;
            case 3:
                pageMap.put(1, new Page(3, new ItemStack[]{}));
                break;
            case 4:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                break;
            case 5:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(1, new ItemStack[]{}));
                break;
            case 6:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(2, new ItemStack[]{}));
                break;
            case 7:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(3, new ItemStack[]{}));
                break;
            case 8:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                break;
            case 9:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(2, new ItemStack[]{}));
                break;
            case 10:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                break;
            case 11:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                pageMap.put(4, new Page(2, new ItemStack[]{}));
                break;
            case 12:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                pageMap.put(4, new Page(4, new ItemStack[]{}));
                break;
            case 13:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                pageMap.put(4, new Page(4, new ItemStack[]{}));
                pageMap.put(5, new Page(2, new ItemStack[]{}));
                break;
            case 14:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                pageMap.put(4, new Page(4, new ItemStack[]{}));
                pageMap.put(5, new Page(4, new ItemStack[]{}));
                break;
            case 15:
                pageMap.put(1, new Page(4, new ItemStack[]{}));
                pageMap.put(2, new Page(4, new ItemStack[]{}));
                pageMap.put(3, new Page(4, new ItemStack[]{}));
                pageMap.put(4, new Page(4, new ItemStack[]{}));
                pageMap.put(5, new Page(4, new ItemStack[]{}));
                pageMap.put(6, new Page(4, new ItemStack[]{}));
                break;
            default:
                pageMap.put(1, new Page(1, new ItemStack[]{}));
                break;

        }

        return new BarrelStorage(level, pageMap);
    }


    public ItemStack forUpgradeNeededStacks(int level){

        switch (level){
            case 2:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(1);
            case 3:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(2);
            case 4:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(4);
            case 5:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(8);
            case 6:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(16);
            case 7:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(32);
            case 8:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(64);
            case 9:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(1);
            case 10:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(2);
            case 11:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(4);
            case 12:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(8);
            case 13:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(16);
            case 14:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(32);
            case 15:
                return StorageUpdateItemPreset.getExpandedStorageUpgradeStack(64);
            default:
                return StorageUpdateItemPreset.getStorageUpdgradeStack(1);
        }

    }


}
