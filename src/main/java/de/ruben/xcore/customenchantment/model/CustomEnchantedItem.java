package de.ruben.xcore.customenchantment.model;

import de.ruben.xcore.customenchantment.XEnchantment;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomEnchantedItem {

    private ItemStack itemStack;
    private final NBTItem nbtItem;

    public CustomEnchantedItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.nbtItem = new NBTItem(itemStack);
    }

    public HashMap<CustomEnchantment, Integer> getCustomEnchantments(){
        HashMap<CustomEnchantment, Integer> map = new HashMap<>();


         nbtItem.getKeys().stream().filter(s -> s.startsWith("Enchantment_"))
                .map(s -> Map.entry(CustomEnchantment.getByKey().get(s.replace("Enchantment_", "")), nbtItem.getInteger(s)))
                .forEach(classIntegerEntry -> map.put(classIntegerEntry.getKey(), classIntegerEntry.getValue()));

         return map;

    }

    public Integer getCustomEnchantmentLevel(CustomEnchantment customEnchantment){
        return nbtItem.getInteger(customEnchantment.getItemKey());
    }

    public boolean hasCustomEnchantment(CustomEnchantment customEnchantment){
        return nbtItem.hasKey(customEnchantment.getItemKey());
    }

    public void addEnchantment(CustomEnchantment customEnchantment, int level, int slot, Inventory inventory){
        nbtItem.setInteger(customEnchantment.getItemKey(), level);
        itemStack = nbtItem.getItem();
        repopulateStack();
        inventory.setItem(slot, itemStack);
    }

    public void removeEnchantment(CustomEnchantment customEnchantment, int slot){
        nbtItem.removeKey(customEnchantment.getItemKey());
        itemStack = nbtItem.getItem();
        repopulateStack();
    }

    ItemStack repopulateStack(){
        itemStack.editMeta(itemMeta -> {
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            List<Component> newLore = new ArrayList<>();


            newLore.add(Component.text("§bVerzauberungen:"));
            itemStack.getEnchantments().forEach((enchantment, integer) -> newLore.add(Component.text("§7➥ "+ XEnchantment.getEnchantmentNames().get(enchantment)+": §b"+integer)));
            getCustomEnchantments().forEach((customEnchantment, integer) -> newLore.add(Component.text(customEnchantment.getLore(integer))));

            if(itemMeta.hasLore()){
                newLore.add(Component.text("§0 "));

                List<String> oldLore = itemMeta.getLore();
                if(oldLore.contains("§bVerzauberungen:")) {
                    int startIndex = oldLore.indexOf("§0 ")+1;
                    if(oldLore.size() < startIndex) {
                        List<String> subList = oldLore.subList(startIndex, oldLore.size());
                        newLore.addAll(subList.stream().map(Component::text).collect(Collectors.toList()));
                    }
                }else {
                    if(oldLore.size() == 1 && oldLore.get(0).equals(" ")) {

                    }else{
                        newLore.addAll(oldLore.stream().map(Component::text).collect(Collectors.toList()));
                    }
                }
            }

            itemMeta.lore(newLore);
            itemStack.setItemMeta(itemMeta);

        });

        return itemStack;
    }


    public ItemStack getItemStack() {
        return itemStack;
    }
}
