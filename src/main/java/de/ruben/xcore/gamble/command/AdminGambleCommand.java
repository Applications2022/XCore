package de.ruben.xcore.gamble.command;

import de.ruben.xcore.gamble.service.GambleLocationService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminGambleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player = (Player) commandSender;

        if(!player.hasPermission("addictzone.admingamble")){
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length == 1) {
            if (args[0].equalsIgnoreCase("game")) {
                new GambleLocationService().addLocation(player.getEyeLocation(), GambleLocationService.LocationType.GAME_DISPLAY);
            } else if (args[0].equalsIgnoreCase("par")) {
                new GambleLocationService().addLocation(player.getEyeLocation(), GambleLocationService.LocationType.PARTICIPANT_DISPLAY);
            }else{
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/admingable §7<§bgame§7|§bpar§7>");
            }
        }else{
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/admingable §7<§bgame§7|§bpar§7>");
        }

        return false;
    }
}
