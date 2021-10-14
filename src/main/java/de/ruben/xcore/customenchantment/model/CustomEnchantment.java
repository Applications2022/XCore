package de.ruben.xcore.customenchantment.model;

import de.ruben.xcore.customenchantment.model.enchantment.MinerEnchant;
import de.ruben.xcore.customenchantment.model.enchantment.TelekinesisEnchant;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class CustomEnchantment implements CustomEnchantmentEventReaction{
    public static final CustomEnchantment TELEKINESIS = new TelekinesisEnchant();
    public static final CustomEnchantment MINER = new MinerEnchant();

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
    public abstract int getStarterLevel();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract EnchantmentTarget enchantmentTarget();

    public abstract boolean conflictsWith(@NotNull Enchantment var1);

    public abstract boolean canEnchantItem(@NotNull ItemStack var1);

    @NotNull
    public abstract Component displayName();

    @NotNull
    public abstract ItemStack getBookItem(int level);

    public abstract String getLore(int level);

    public abstract String getLore();

    public static void registerEnchantment(CustomEnchantment customEnchantment){
        getByKey().putIfAbsent(customEnchantment.key, customEnchantment);
    }

    public static HashMap<String, CustomEnchantment> getByKey() {
        return byKey;
    }
}
