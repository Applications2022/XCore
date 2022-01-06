package de.ruben.xcore.customenchantment.listener;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantmentBook;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event){
        AnvilInventory inventory = event.getInventory();

        ItemStack stack1 = inventory.getFirstItem();
        ItemStack stack2 = inventory.getSecondItem();

        if((stack1 != null && stack1.getType() != Material.AIR) && (stack2 != null && stack2.getType() != Material.AIR)){

            CustomEnchantmentBook customEnchantmentBook = new CustomEnchantmentBook(stack1);
            CustomEnchantmentBook customEnchantmentBook2 = new CustomEnchantmentBook(stack2);

            if(customEnchantmentBook.isEnchantmentBook() && customEnchantmentBook2.isEnchantmentBook()){
                event.setResult(customEnchantmentBook.getCustomEnchantmentBook(customEnchantmentBook2));
                inventory.setRepairCost(30);
            }else if(customEnchantmentBook.isEnchantmentBook() || customEnchantmentBook2.isEnchantmentBook()){


                ItemStack toEnchantStack = customEnchantmentBook.isEnchantmentBook() ? stack2.clone() : stack1.clone();
                CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(toEnchantStack);

                CustomEnchantmentBook customEnchantmentBookFinal = customEnchantmentBook.isEnchantmentBook() ? customEnchantmentBook : customEnchantmentBook2;

                CustomEnchantment customEnchantment = customEnchantmentBookFinal.getCustomEnchantment();
                int level = customEnchantmentBookFinal.getCustomEnchantmentLevel();

                if(customEnchantmentBookFinal.getCustomEnchantment().canEnchantItem(toEnchantStack)){

                    if(customEnchantedItem.hasCustomEnchantment(customEnchantment)){
                        int customEnchLevel = customEnchantedItem.getCustomEnchantmentLevel(customEnchantment);


                        if(customEnchLevel == level){
                            if((customEnchLevel+1) <= customEnchantment.getMaxLevel()) {
                                customEnchantedItem.addEnchantment(customEnchantment, level + 1);
                                event.setResult(customEnchantedItem.getItemStack());
                                inventory.setRepairCost(30);
                            }
                        }else if(customEnchLevel < level){
                            customEnchantedItem.addEnchantment(customEnchantment, level);
                            event.setResult(customEnchantedItem.getItemStack());
                            inventory.setRepairCost(30);
                        }
                    }else{
                        customEnchantedItem.addEnchantment(customEnchantment, level);
                        event.setResult(customEnchantedItem.getItemStack());
                        inventory.setRepairCost(30);
                    }

                }

            }

        }

        if(event.getResult() != null && event.getResult().getType() != Material.AIR){
            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(event.getResult());

            event.setResult(customEnchantedItem.repopulateStack());
        }

    }

}
