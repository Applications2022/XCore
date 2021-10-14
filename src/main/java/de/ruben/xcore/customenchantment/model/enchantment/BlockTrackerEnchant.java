package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockTrackerEnchant extends CustomEnchantment {
    public BlockTrackerEnchant(String key) {
        super(key);
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull int getStarterLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Blocktracker";
    }

    @Override
    public @NotNull EnchantmentTarget enchantmentTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment var1) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack var1) {
        return EnchantmentTarget.TOOL.includes(var1);
    }

    @Override
    public @NotNull Component displayName() {
        return Component.text("Blocktracker");
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return new ItemStack(Material.ACACIA_WOOD);
    }

    @Override
    public String getLore(int level) {
        return "Blocktracker";
    }

    @Override
    public String getLore() {
        return "Blocktracker";
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {
        if(event instanceof BlockBreakEvent) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;

            ItemStack itemStack = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();
            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(itemStack);

        }
    }

    @Override
    public boolean canHandle(Event event) {
        return false;
    }
}
