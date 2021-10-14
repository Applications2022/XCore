package de.ruben.xcore.profile.command;

import de.ruben.xcore.profile.gui.ProfileGui;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminProfileCommand implements CommandExecutor {
    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        String commandLabel = command.getLabel();

        if(commandLabel.equalsIgnoreCase("adminprofile")){
            Player player = (Player)  commandSender;

            if(!player.hasPermission("addictzone.adminprofile")){
                player.sendMessage(messageService.getMessage("noperm"));
                return true;
            }

            if(args.length == 1){
                String targetName = args[0];

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

                if(offlinePlayer == null){
                    commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                    return true;
                }

                new ProfileService().getProfileAsync(offlinePlayer.getUniqueId(), playerProfile -> new ProfileGui(player, offlinePlayer.getUniqueId()));

            }else{
                player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/adminprofile §7<§bSpieler§7>");
            }
        }

        return false;
    }
}
