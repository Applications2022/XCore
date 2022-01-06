package de.ruben.xcore.changelog.command;

import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EditChangelogCommand implements CommandExecutor {

    private XChangelog xChangelog;

    public EditChangelogCommand(XChangelog xChangelog) {
        this.xChangelog = xChangelog;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = (Player) sender;

        if(!player.hasPermission("addictzone.changelog.edit")){
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("load")){
                xChangelog.getChangeLogService().loadChangeLogsIntoMap();
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast alle Changelogs erfolgreich aus der Datenbank geladen.");
            }else{
                player.sendMessage(helpMessage());
            }
        }else{
            player.sendMessage(helpMessage());
        }

        return false;
    }

    private String helpMessage(){
        return XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/editchangelog §7<§bload>";
    }
}
