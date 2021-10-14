package de.ruben.xcore.itemstorage.gui.items;

import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class StorageUpdateItemPreset {

    public static ItemStack getStorageUpdgradeStack(int amount){
        NBTItem nbtItem = new NBTItem(ItemBuilder
                .from(Material.TRIPWIRE_HOOK)
                .amount(amount)
                .enchant(Enchantment.ARROW_DAMAGE)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .name(Component.text("§bSpeicher Upgrade"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Hiermit kannst du deine"),
                        Component.text("§7➥ §9Itemspeicher §7upgraden!"),
                        Component.text(" ")
                ).build());

        nbtItem.setBoolean("storageupgrade", true);

        return nbtItem.getItem();
    }

    public static ItemStack getExpandedStorageUpgradeStack(int amount){
        NBTItem nbtItem = new NBTItem(ItemBuilder
                .from(Material.SOUL_TORCH)
                .amount(amount)
                .enchant(Enchantment.ARROW_DAMAGE)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .name(Component.text("§9Erweitertes Speicher Upgrade"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Hiermit kannst du deine"),
                        Component.text("§7➥ §9Itemspeicher §7upgraden!"),
                        Component.text(" ")
                ).build());

        nbtItem.setBoolean("expandedstorageupgrade", true);

        return nbtItem.getItem();
    }

    public static boolean isStorageUpgradeStack(ItemStack itemStack){
        return new NBTItem(itemStack).hasKey("storageupgrade");
    }

    public static boolean isExpandedStorageUpgradeStack(ItemStack itemStack){
        return new NBTItem(itemStack).hasKey("expandedstorageupgrade");
    }
}
