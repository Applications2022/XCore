package de.ruben.xcore.customenchantment.listener;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

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
        }

    }
}
