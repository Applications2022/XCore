package de.ruben.xcore.customenchantment.model;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;

public class CustomEnchantmentBook {

    private ItemStack itemStack;

    public CustomEnchantmentBook(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public boolean isEnchantmentBook(){
        return new NBTItem(itemStack).hasKey("customEnchBook");
    }

    public CustomEnchantment getCustomEnchantment(){
        return CustomEnchantment.getByKey().get(new NBTItem(itemStack).getString("customEnchBook"));
    }

    public Integer getCustomEnchantmentLevel(){
        return new NBTItem(itemStack).getInteger("customEnchBookLevel");
    }

    public ItemStack getCustomEnchantmentBook(CustomEnchantmentBook customEnchantmentBook){
        if(customEnchantmentBook.getCustomEnchantmentLevel() == getCustomEnchantmentLevel()){
            if(getCustomEnchantmentLevel()+1 <= getCustomEnchantment().getMaxLevel()) {
                return customEnchantmentBook.getCustomEnchantment().getNBTBookItem(getCustomEnchantmentLevel() + 1);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
