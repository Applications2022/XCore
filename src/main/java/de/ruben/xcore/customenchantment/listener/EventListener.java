package de.ruben.xcore.customenchantment.listener;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EventListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){

        if(event.isCancelled()) return;

        if(event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR){
            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(event.getPlayer().getInventory().getItemInMainHand());

            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.TELEKINESIS)){
                CustomEnchantment.TELEKINESIS.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.TELEKINESIS));
            }
            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.MINER)){
                CustomEnchantment.MINER.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.MINER));
            }
            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.BLOCKTRACKER)){
                CustomEnchantment.BLOCKTRACKER.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.BLOCKTRACKER));
            }

            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.LUMBERJACK)){
                CustomEnchantment.LUMBERJACK.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.LUMBERJACK));
            }

            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.SMELT) && !customEnchantedItem.hasCustomEnchantment(CustomEnchantment.TELEKINESIS)){
                CustomEnchantment.SMELT.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.SMELT));
            }

            if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.ERFAHRUNG)){
                CustomEnchantment.ERFAHRUNG.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.ERFAHRUNG));
            }
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){

        Player killer = event.getEntity().getKiller();
        if(killer != null) {

            ItemStack itemInHand = killer.getInventory().getItemInMainHand();

            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(itemInHand);

                if (customEnchantedItem.hasCustomEnchantment(CustomEnchantment.TASCHENDIEB)) {
                    CustomEnchantment.TASCHENDIEB.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.TASCHENDIEB));
                }

                if (customEnchantedItem.hasCustomEnchantment(CustomEnchantment.GUILLOTINE)) {
                    System.out.println("yeaa");
                    CustomEnchantment.GUILLOTINE.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.GUILLOTINE));
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){
        Player killer = event.getEntity().getKiller();

        if(killer != null) {
            ItemStack itemInHand = killer.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(itemInHand);

                if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.ERFAHRUNG)){
                    CustomEnchantment.ERFAHRUNG.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.ERFAHRUNG));
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player){
            Player damager = ((Player) event.getDamager());

            ItemStack itemInHand = damager.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.getType() != Material.AIR) {
                CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(itemInHand);

                if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.LEBENSRAUB)){
                    CustomEnchantment.LEBENSRAUB.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.LEBENSRAUB));
                }

                if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.VERDERBEN)){
                    CustomEnchantment.VERDERBEN.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.VERDERBEN));
                }

                if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.SCHATTEN)){
                    CustomEnchantment.SCHATTEN.handleEvent(event, customEnchantedItem.getCustomEnchantmentLevel(CustomEnchantment.SCHATTEN));
                }
            }

        }
    }
}
