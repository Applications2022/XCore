package de.ruben.xcore.customenchantment.model;

import de.ruben.xcore.customenchantment.model.enchantment.*;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class CustomEnchantment implements CustomEnchantmentEventReaction{
    public static final CustomEnchantment TELEKINESIS = new TelekinesisEnchant();
    public static final CustomEnchantment MINER = new MinerEnchant();
    public static final CustomEnchantment BLOCKTRACKER = new BlockTrackerEnchant();
    public static final CustomEnchantment TASCHENDIEB = new TaschendiebEnchant();
    public static final CustomEnchantment GUILLOTINE = new GuillotineEnchant();
    public static final CustomEnchantment LUMBERJACK = new LumberjackEnchant();
    public static final CustomEnchantment SMELT = new SmeltEnchant();
    public static final CustomEnchantment ERFAHRUNG = new ErfahrungEnchant();
    public static final CustomEnchantment LEBENSRAUB = new LebensraubEnchant();
    public static final CustomEnchantment VERDERBEN = new VerderbenEnchant();
    public static final CustomEnchantment SCHATTEN = new SchattenEnchant();

    private final String key;

    private static final HashMap<String, CustomEnchantment> byKey = new HashMap<>();

    public CustomEnchantment(String key) {
        this.key = key;
    }

    public String getItemKey() {
        return "Enchantment_"+key;
    }

    public String getBookKey() {
        return "EnchantmentBook_"+key;
    }


    @NotNull
    public abstract int getMaxLevel();

    @NotNull
    public int getStarterLevel(){
        return 1;
    }

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract EnchantmentTarget enchantmentTarget();

    public String extraDataKey(){
        return key+"_extradata";
    }

    public ItemStack enchantItem(ItemStack itemStack, int level){
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(getItemKey(), level);
        return nbtItem.getItem();
    }

    public  ItemStack disenchantItem(ItemStack itemStack){
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.removeKey(getItemKey());
        return nbtItem.getItem();
    }

    public boolean hasExtraData(){
        return false;
    }

    public ItemStack repopulateExtraData(ItemStack itemStack){
        return itemStack;
    }

    public abstract boolean conflictsWith(@NotNull Enchantment var1);

    public abstract boolean canEnchantItem(@NotNull ItemStack var1);

    @NotNull
    public Component displayName(){
        return Component.text("§b"+getName());
    }

    @NotNull
    public abstract ItemStack getBookItem(int level);

    public ItemStack getNBTBookItem(int level){
        NBTItem nbtItem = new NBTItem(getBookItem(level));
        nbtItem.setString("customEnchBook", getKey());
        nbtItem.setInteger("customEnchBookLevel", level);
        return nbtItem.getItem();
    }

    public abstract String getLore(int level);

    public abstract String getLore();

    public abstract String getWeiteresLore(int level);

    public abstract String getEffekteLore(int level);

    public Map<String, Object> getExtraData(ItemStack itemStack){
        if(hasExtraData()){
            NBTItem nbtItem = new NBTItem(itemStack);

            return nbtItem.getObject(extraDataKey(), Map.class);
        }else{
            return new HashMap<>();
        }
    }

    public static void registerEnchantment(CustomEnchantment customEnchantment){
        getByKey().putIfAbsent(customEnchantment.getKey(), customEnchantment);
    }

    public static HashMap<String, CustomEnchantment> getByKey() {
        return byKey;
    }

    public String getKey() {
        return key;
    }
}
