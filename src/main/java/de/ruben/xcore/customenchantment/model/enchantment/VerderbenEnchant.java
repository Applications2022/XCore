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

import java.util.concurrent.ThreadLocalRandom;

public class VerderbenEnchant extends CustomEnchantment {
    public VerderbenEnchant() {
        super("verderben");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Verderben";
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
                        Component.text("§7➥ Du erhältst eine Chance von §b2%§7,"),
                        Component.text("§7➥ deinen Gegner §bdirekt §7zu töten."),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Verderben";
    }

    @Override
    public String getLore() {
        return "§7➥ Verderben";
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
            if(entityDamageByEntityEvent.getDamager() instanceof Player) {
                Player damager = (Player) entityDamageByEntityEvent.getDamager();

                int random = ThreadLocalRandom.current().nextInt(100);

                System.out.println(random);

                if (random <= 2) {
                    if (entityDamageByEntityEvent.getEntity() instanceof Player) {
                        Player damaged = (Player) entityDamageByEntityEvent.getEntity();

                        damaged.damage(1000000, damager);
                    } else {
                        entityDamageByEntityEvent.getEntity().remove();
                    }
                }
            }
        }
    }
}
