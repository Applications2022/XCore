package de.ruben.xcore.customenchantment.model;

import de.ruben.xcore.customenchantment.XEnchantment;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
@Setter
public class CustomEnchantedItem {

    private ItemStack itemStack;
    private NBTItem nbtItem;

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

    public Map<String, Object> getExtraData(CustomEnchantment customEnchantment){
        if(customEnchantment.hasExtraData()){
            NBTItem nbtItem = new NBTItem(itemStack);

            return nbtItem.getObject(customEnchantment.extraDataKey(), Map.class);
        }else{
            return new HashMap<>();
        }
    }

    public Integer getCustomEnchantmentLevel(CustomEnchantment customEnchantment){
        return nbtItem.getInteger(customEnchantment.getItemKey());
    }

    public boolean hasCustomEnchantment(CustomEnchantment customEnchantment){
        return nbtItem.hasKey(customEnchantment.getItemKey());
    }

    public void addEnchantment(CustomEnchantment customEnchantment, int level, int slot, Inventory inventory){

        itemStack = customEnchantment.enchantItem(itemStack, level);
        nbtItem = new NBTItem(itemStack);
        this.itemStack = repopulateStack();
        nbtItem = new NBTItem(itemStack);

        if(!itemStack.getItemMeta().hasEnchants() && !getCustomEnchantments().isEmpty()){
            itemStack.addUnsafeEnchantment(Enchantment.LURE, 1);
            itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        nbtItem = new NBTItem(itemStack);

        inventory.setItem(slot, itemStack);
    }

    public void addEnchantment(CustomEnchantment customEnchantment, int level){

        if(!itemStack.getItemMeta().hasEnchants()){
            itemStack.addUnsafeEnchantment(Enchantment.LURE, 1);
            itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack = customEnchantment.enchantItem(itemStack, level);
        nbtItem = new NBTItem(itemStack);
        this.itemStack = repopulateStack();
        nbtItem = new NBTItem(itemStack);
    }

    public void removeEnchantment(CustomEnchantment customEnchantment, int slot, Inventory inventory){
        itemStack = customEnchantment.disenchantItem(itemStack);
        nbtItem = new NBTItem(itemStack);
        this.itemStack = repopulateStack();
        nbtItem = new NBTItem(itemStack);

        Map<Enchantment, Integer> enchants = itemStack.getEnchantments();
        enchants.remove(Enchantment.LURE);
        if(getCustomEnchantments().isEmpty()){
            itemStack.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        if(enchants.isEmpty() && getCustomEnchantments().isEmpty()){
            itemStack.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.removeEnchantment(Enchantment.LURE);
        }

        inventory.setItem(slot, itemStack);
    }

    public ItemStack repopulateStack(){

        if(!itemStack.getItemMeta().hasEnchants() && getCustomEnchantments().isEmpty()){
            return itemStack;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> itemLore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();

        List<String> customLore = new ArrayList<>();

        for(int i = itemLore.size()-1; i >= 0; i--){
            if(!itemLore.get(i).equals("§d ") && !itemLore.get(i).equals("§c ") && !itemLore.get(i).equals("§b ") && !itemLore.get(i).equals("§a ")){
                customLore.add(itemLore.get(i));
            }else{
                break;
            }
        }

        List<String> weiteresListe = new ArrayList<>();
        List<String> effekteList = new ArrayList<>();
        List<String> verzauberungen = new ArrayList<>();

        HashMap<CustomEnchantment, Integer> enchants = getCustomEnchantments();

        Comparator<CustomEnchantment> comparator = Comparator.<CustomEnchantment, Boolean>comparing(s -> s.getLore(enchants.get(s)).contains(":")).reversed();

        itemStack.getEnchantments().forEach((enchantment, integer) -> {
            if(!enchantment.getKey().equals(Enchantment.LURE.getKey())){
                verzauberungen.add("§7➥ "+ XEnchantment.getEnchantmentNames().get(enchantment)+": §b"+integer);
            }
        });

        enchants
                .keySet()
                .stream()
                .sorted(Comparator.comparing(enchants::get))
                .sorted(comparator)
                .forEach(customEnchantment -> {
                    int integer = enchants.get(customEnchantment);
                    if(customEnchantment.getWeiteresLore(integer) != null) weiteresListe.add(customEnchantment.getWeiteresLore(integer));
                    if(customEnchantment.getEffekteLore(integer) != null) effekteList.add(customEnchantment.getEffekteLore(integer));
                    verzauberungen.add(customEnchantment.getLore(integer));
                });



        List<String> finalLore = new ArrayList<>();
        if(!verzauberungen.isEmpty()){
            finalLore.add("§bVerzauberungen:");
        }
        finalLore.addAll(verzauberungen);
        if(!verzauberungen.isEmpty()){
            finalLore.add("§a ");
        }
        if(!effekteList.isEmpty()){
            finalLore.add("§bEffekte:");
        }
        finalLore.addAll(effekteList);
        if(!effekteList.isEmpty()){
            finalLore.add("§b ");
        }
        if(!weiteresListe.isEmpty()){
            finalLore.add("§bWeiteres:");
        }
        finalLore.addAll(weiteresListe);
        if(!weiteresListe.isEmpty()){
            finalLore.add("§c ");
        }

        finalLore.addAll(customLore);



        itemMeta.lore(finalLore.stream().map(Component::text).collect(Collectors.toList()));
        itemStack.setItemMeta(itemMeta);

        getCustomEnchantments().forEach((customEnchantment, integer) -> {
            itemStack = customEnchantment.repopulateExtraData(itemStack);
        });

        itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        return itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
