package de.ruben.xcore.clan.command;

import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.gui.ClanGui;
import de.ruben.xcore.clan.gui.NoClanGui;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClanCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        ClanPlayerService clanPlayerService = new ClanPlayerService();
        ClanService clanService = new ClanService();
        Player player = (Player) commandSender;

        if(args.length == 0){
            if(!clanPlayerService.isInClan(player.getUniqueId())){
                new NoClanGui().open(player);
            }else{
                new ClanGui().open(player, clanPlayerService.getClan(player.getUniqueId()));
            }
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("leave")){
                clanService.leaveClan(player);
            }else if(args[0].equalsIgnoreCase("delete")){

                if(clanPlayerService.isInClan(player.getUniqueId())){
                    if(clanPlayerService.getClan(player.getUniqueId()).isOwner(player)){
                        XClan.getInstance().getClanDeleteConversation().getConversationFactory().buildConversation((Conversable) player).begin();
                    }else{
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu musst Besitzer deines Clans sein um ihn löschen zu können!");
                    }
                }else{
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu bist in keinem Clan!");
                }

            }
        }

        return false;

    }
}
