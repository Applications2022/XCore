package de.ruben.xcore.itemstorage.model;

import de.ruben.xcore.util.BukkitSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class Page {

    private int rows;
    private String itemStacks;

    public Page(int rows, ItemStack[] itemStacks) {
        this.rows = rows;
        this.itemStacks = BukkitSerialization.itemStackArrayToBase64(itemStacks);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public ItemStack[] getItemStacksArray() {
        try {
            return BukkitSerialization.itemStackArrayFromBase64(itemStacks);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setItemStacksArray(ItemStack[] itemStacks) {
        this.itemStacks = BukkitSerialization.itemStackArrayToBase64(itemStacks);
    }

    public String getItemStacks() {
        return itemStacks;
    }

    public void setItemStacks(String itemStacks) {
        this.itemStacks = itemStacks;
    }
}
