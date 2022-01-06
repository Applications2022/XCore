package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LebensraubEnchant extends CustomEnchantment {
    public LebensraubEnchant() {
        super("lebensraub");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 5;
    }

    @Override
    public @NotNull String getName() {
        return "Lebensraub";
    }

    @Override
    public @NotNull EnchantmentTarget enchantmentTarget() {
        return EnchantmentTarget.ALL;
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
                        Component.text("§7➥ Pro Level erhältst du §b5% §7mehr des Schadens,"),
                        Component.text("§7➥ den du deinem genger gemacht hast, als Leben."),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+(level*5)+"%"),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();

    }

    @Override
    public String getLore(int level) {
        return "§7➥ Lebensraub: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Lebensraub:";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        return null;
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {
        if(event instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;

            if(entityDamageByEntityEvent.getDamager() instanceof Player){
                Player damager = (Player) entityDamageByEntityEvent.getDamager();
                int levelpercentage = enchantmentLevel*5;
                double percentage = (entityDamageByEntityEvent.getDamage() * (levelpercentage/100.0f));
                System.out.println("+"+percentage);

                double health = damager.getHealth()+percentage;
                damager.setHealth(health <= 20 ? health : 20);
            }

        }
    }
}
