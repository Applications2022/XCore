package de.ruben.xcore.itemstorage.command;

import de.ruben.xcore.itemstorage.gui.items.StorageUpdateItemPreset;
import de.ruben.xcore.itemstorage.service.BarrelStorageService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.message.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminStorageCommand implements CommandExecutor {

    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        String commandLabel = command.getLabel();

        if(commandLabel.equalsIgnoreCase("getstorage")){

            Player player = (Player) sender;

            if(args.length == 1){
                BarrelStorageService barrelStorageService = new BarrelStorageService();

                int level = (Integer.parseInt(args[0])<=10)?Integer.parseInt(args[0]):1;

                player.getInventory().addItem(barrelStorageService.getBarrelStorageItem(barrelStorageService.getBarrelStorage(Integer.parseInt(args[0]))));
                player.sendMessage(messageService.getMessage("prefix")+"§91xItem Speicher §8(lvl."+level+") §7wurde deinem Inventar inzugefügt!");
            }else{
                player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/getstrorage §7<§blevel§7>");
            }

            return true;
        }

        if(commandLabel.equalsIgnoreCase("storageUpgradeItem")){
            Player player = (Player) sender;

            if(args.length == 2){
                String type = args[0];
                String amount = args[1];

                if(type.equalsIgnoreCase("normal")){
                    player.getInventory().addItem(StorageUpdateItemPreset.getStorageUpdgradeStack(Integer.parseInt(amount)));
                }else if(type.equalsIgnoreCase("extended")){
                    player.getInventory().addItem(StorageUpdateItemPreset.getExpandedStorageUpgradeStack(Integer.parseInt(amount)));
                }else{
                    player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/storageUpgradeItem §7<§bNormal§7|§bExtended§7> §7<§bAnzahl§7>");
                    return true;
                }

                String itemString = type.equalsIgnoreCase("normal") ? "§bUpgrade Item" : "§9Erweitertes Upgrade Item";

                player.sendMessage(messageService.getMessage("prefix")+"§b"+amount+"x "+itemString+" §7wurden deinem Inventar hinzugefügt!");
            }else{
                player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/storageUpgradeItem §7<§bNormal§7|§bExtended§7> §7<§bAnzahl§7>");
            }
        }
        return false;
    }
}
