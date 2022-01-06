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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class SchattenEnchant extends CustomEnchantment {
    public SchattenEnchant() {
        super("schatten");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 10;
    }

    @Override
    public @NotNull String getName() {
        return "Schatten";
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
                        Component.text("§7➥ Pro Level erhöht sich die Chance um §b2%§7,"),
                        Component.text("§7➥ deinem Genger mit einem Schlag, für §b3 §7Sekunden"),
                        Component.text("§7➥ den §bBlindheits §7effekt zu geben."),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+(level*2)+"%"),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Schatten: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Schatten:";
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

            if(entityDamageByEntityEvent.getDamager() instanceof Player && entityDamageByEntityEvent.getEntity() instanceof Player) {
                Player damaged = (Player) entityDamageByEntityEvent.getEntity();

                int random = ThreadLocalRandom.current().nextInt(100);

                if (random <= (2*enchantmentLevel)) {
                    damaged.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1));
                }
            }
        }

    }
}
