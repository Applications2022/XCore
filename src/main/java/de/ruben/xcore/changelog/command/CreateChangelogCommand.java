package de.ruben.xcore.changelog.command;

import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xcore.changelog.model.ChangeLogType;
import de.ruben.xcore.changelog.model.Changelog;
import de.ruben.xcore.changelog.service.ChangeLogService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class CreateChangelogCommand implements CommandExecutor {

    private XChangelog xChangelog;

    public CreateChangelogCommand(XChangelog xChangelog) {
        this.xChangelog = xChangelog;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player = (Player) commandSender;

        if(!player.hasPermission("addictzone.changelog.create")){
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length == 1){

            ItemStack bookStack = player.getInventory().getItemInMainHand();
            if(bookStack == null || bookStack.getType() != Material.WRITTEN_BOOK){
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cBitte nehme ein beschriebenes Buch in die Hand!");
                return true;
            }

            if(ChangeLogType.valueOf(args[0].toUpperCase()) != null){

                BookMeta bookMeta = (BookMeta) bookStack.getItemMeta();

                Changelog changelog = new Changelog(
                        player.getUniqueId(),
                        ChatColor.translateAlternateColorCodes('&', bookMeta.getTitle()),
                        ChatColor.translateAlternateColorCodes('&', bookMeta.getPage(1)),
                        new Date(System.currentTimeMillis()),
                        ChangeLogType.valueOf(args[0].toUpperCase())
                );

                xChangelog.getChangeLogService().saveChangeLog(changelog);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich einen Changelog Eintrag erstellt!");

            }else{
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cBitte benutze einen Validen Tag!");
            }

        }else{
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/createchangelog §7<§bnew§7|§bchange§7|§bfix§7|§bevent§7>");
        }

        return false;
    }
}
