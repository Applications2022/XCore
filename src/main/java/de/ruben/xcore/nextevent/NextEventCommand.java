package de.ruben.xcore.nextevent;

import de.ruben.xcore.XCore;
import de.ruben.xcore.placeholder.EventPlaceHolderExpansion;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class NextEventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!sender.hasPermission("addictZone.nextevent")){
            sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length >= 1){
            if(args[0].equalsIgnoreCase("new")){
                if(args.length == 2){

                    if(args[1].equalsIgnoreCase("null")){
                        XCore.getInstance().setNextEventDate(0L);
                        XCore.getInstance().setNextEventTitle("");

                        EventPlaceHolderExpansion.setDate2Long(0L);
                        EventPlaceHolderExpansion.setTitle("");

                        sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Nun steht kein Event mehr an!");
                    }else{
                        sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent new §7<§bdate§7|§bnull§7> §7<§bTitel§7>");
                        sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent info §7<§bEventinfo§7>");
                    }
                }else if(args.length >= 3){
                    String dateString = args[1];
                    String title = "";

                    for(int i = 2; i < args.length; i++){
                        title += args[i] + (i == (args.length-1) ? "" : " ");
                    }

                    Long date = System.currentTimeMillis()+Long.parseLong(dateString);

                    XCore.getInstance().setNextEventDate(date);
                    XCore.getInstance().setNextEventTitle(title);

                    EventPlaceHolderExpansion.setDate2Long(date);
                    EventPlaceHolderExpansion.setTitle(title);

                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Event erfolgreich aktualisiert!");

                }else{
                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent new §7<§bdate§7|§bnull§7> §7<§bTitel§7>");
                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent info §7<§bEventinfo§7>");
                }
            }else if(args[0].equalsIgnoreCase("info")){
                if(args.length >= 2){
                    String info = "";

                    for(int i = 1; i < args.length; i++){
                        info += args[i] + (i == (args.length-1) ? "" : " ");
                    }

                    XCore.getInstance().setNextEventInfo(info);
                    EventPlaceHolderExpansion.setInfo(info);

                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Eventinfo erfolgreich aktualisiert!");
                }else{
                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent new §7<§bdate§7|§bnull§7> §7<§bTitel§7>");
                    sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent info §7<§bEventinfo§7>");
                }
            }else{
                sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent new §7<§bdate§7|§bnull§7> §7<§bTitel§7>");
                sender.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/nexevent info §7<§bEventinfo§7>");
            }
        }


        return false;
    }
}
