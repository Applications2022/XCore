package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ErfahrungEnchant extends CustomEnchantment {
    public ErfahrungEnchant() {
        super("erfahrung");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 5;
    }

    @Override
    public @NotNull String getName() {
        return "Erfahrung";
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
        return EnchantmentTarget.ALL.includes(var1);
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return ItemBuilder
                .from(Material.ENCHANTED_BOOK)
                .name(displayName())
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Du erhältst pro Level §b10% §7mehr XP"),
                        Component.text("§7➥ von abgebauten Blöcken und getöteten Tieren"),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+(level*10)+"%"),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Erfahrung: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Erfahrung:";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        return "§7➥ §b"+(level*10)+"% §7mehr XP";
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {

        if(event instanceof BlockBreakEvent){
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;

            int levelpercentage = enchantmentLevel*10;
            double percentage = (blockBreakEvent.getExpToDrop() * (levelpercentage/100.0f));
            int xpToDrop = Math.toIntExact(Math.round(blockBreakEvent.getExpToDrop() + percentage));

            xpToDrop = (xpToDrop < 1 && blockBreakEvent.getExpToDrop() > 0) ? 1 : xpToDrop;

            blockBreakEvent.setExpToDrop(xpToDrop);
        }

        if(event instanceof EntityDeathEvent){
            EntityDeathEvent entityDeathEvent = (EntityDeathEvent) event;

            int levelpercentage = enchantmentLevel*10;
            double percentage = (entityDeathEvent.getDroppedExp() * (levelpercentage/100.0f));
            int xpToDrop = Math.toIntExact(Math.round(entityDeathEvent.getDroppedExp() + percentage));


            xpToDrop = (xpToDrop < 1 && entityDeathEvent.getDroppedExp() > 0) ? 1 : xpToDrop;

            entityDeathEvent.setDroppedExp(xpToDrop);
        }
    }
}
