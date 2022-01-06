package de.ruben.xcore.customenchantment.command;

import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdminEnchantCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;

        if(!player.hasPermission("addictzone.enchants.admin")){
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("repopulate")){

                ItemStack toEnchant = player.getInventory().getItemInMainHand();

                if(toEnchant == null || toEnchant.getType() == Material.AIR){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Bitte nehme ein Item in die Hand!");
                    return true;
                }

                CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(toEnchant);
                customEnchantedItem.repopulateStack();

                player.getInventory().setItemInMainHand(customEnchantedItem.getItemStack());
            }else{
                sendHelpMessage(player);
            }
        }else if(args.length == 3){
            if(args[0].equalsIgnoreCase("enchant")){
                String enchantMentName = args[1].toLowerCase().replace("_", " ");
                int level = Integer.valueOf(args[2]);

                ItemStack toEnchant = player.getInventory().getItemInMainHand();

                if(toEnchant == null || toEnchant.getType() == Material.AIR){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Bitte nehme ein Item in die Hand!");
                    return true;
                }

                if(XEnchantment.getEnchantmentsByName().get(enchantMentName) == null && CustomEnchantment.getByKey().get(enchantMentName) == null){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Bitte gebe ein exestierendes Enchantment an!");
                    return true;
                }

                if(CustomEnchantment.getByKey().get(enchantMentName) == null){
                    toEnchant.addUnsafeEnchantment(XEnchantment.getEnchantmentsByName().get(enchantMentName), level);

                    CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(toEnchant);
                    customEnchantedItem.repopulateStack();

                    player.getInventory().setItemInMainHand(customEnchantedItem.getItemStack());
                }else{
                    CustomEnchantment customEnchantment = CustomEnchantment.getByKey().get(enchantMentName);

                    if(!customEnchantment.canEnchantItem(toEnchant)){
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Du kannst dieses Item mit diesem enchantment nicht enchanten!");
                        return true;
                    }
                    if(level > customEnchantment.getMaxLevel()){
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Das angegebene Level ist über dem maximalen Level!");
                        return true;
                    }

                    CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(toEnchant);

                    customEnchantedItem.addEnchantment(customEnchantment, level, player.getInventory().getHeldItemSlot(), player.getInventory());
                }

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Du hast das Item erfolgreich enchantet!");

            }else if(args[0].equalsIgnoreCase("book")){
                String enchantMentName = args[1].toLowerCase().replace("_", " ");
                int level = Integer.valueOf(args[2]);

                if(CustomEnchantment.getByKey().get(enchantMentName) == null){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Bitte gebe ein exestierendes Enchantment an!");
                    return true;
                }

                player.getInventory().addItem(CustomEnchantment.getByKey().get(enchantMentName).getNBTBookItem(level));

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"Du hast das Buch erfolgreich bekommen!");

            } else{
                sendHelpMessage(player);
            }
        }else{
            sendHelpMessage(player);
        }


        return false;
    }

    public void sendHelpMessage(Player player){
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage(" ");
        player.sendMessage("§7Benutze: §b/adminenchant enchant <enchantment> <level>");
        player.sendMessage("§7Benutze: §b/adminenchant book <enchantment> <level>");
        player.sendMessage("§7Benutze: §b/adminenchant repopulate");
        player.sendMessage(" ");
        player.sendMessage("§8§m--------------------------------------------------");
    }
}
