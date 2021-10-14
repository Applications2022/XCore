package de.ruben.xcore.currency.command;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.type.PrivateState;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AdminCashCommands implements CommandExecutor {
    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {



        String commandLabel = command.getLabel();

        if(commandLabel.equalsIgnoreCase("eco")){
            // /eco <Type> <set:add:remove:reset> <Spieler> <amount>
            // /eco privateCash <Spieler> <true|false>#
            if(!commandSender.hasPermission("addictzone.eco.admin")){
                commandSender.sendMessage(messageService.getMessage("noperm"));
                return true;
            }


            if(args.length == 4){
                String type = args[0];

                if(type.equalsIgnoreCase("cash") || type.equalsIgnoreCase("bank")){
                    String action = args[1];

                    String targetName = args[2];

                    String amountString = args[3];

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

                    if(offlinePlayer == null){
                        commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                        return true;
                    }

                    if(!isDouble(amountString)){
                        commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Gebe eine Zahl als Geldbetrag an!");
                        return true;
                    }

                    double amount = Double.parseDouble(amountString);

                    switch (action.toLowerCase()){
                        case "set":
                            if(type.equalsIgnoreCase("cash")) {
                                XCurrency.getInstance().getCashService().setValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast das Geld von §b" + offlinePlayer.getName() + " §7erfolgreich auf §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7gesetzt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Dein Geld wurde von §b"+commandSender.getName()+" §7auf §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7gesetzt!");
                                });
                            }else if(type.equalsIgnoreCase("bank")){
                                XCurrency.getInstance().getBankService().setValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast das Geld auf der Bank von §b" + offlinePlayer.getName() + " §7erfolgreich auf §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7gesetzt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Dein Geld auf deiner Bank wurde von §b"+commandSender.getName()+" §7auf §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7gesetzt!");
                                });
                            }
                            break;
                        case "add":
                            if(type.equalsIgnoreCase("cash")) {
                                XCurrency.getInstance().getCashService().addValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast §b" + offlinePlayer.getName() + " §7erfolgreich §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7hinzugefügt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Dir wurden §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7von §b"+commandSender.getName()+" §7hinzugefügt!");
                                });
                            }else if(type.equalsIgnoreCase("bank")){
                                XCurrency.getInstance().getBankService().addValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast der Bank von  §b" + offlinePlayer.getName() + " §7erfolgreich §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7hinzugefügt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Deiner Bank wurden §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7von §b"+commandSender.getName()+" §7hinzugefügt!");
                                });
                            }
                            break;
                        case "remove":
                            if(type.equalsIgnoreCase("cash")) {
                                XCurrency.getInstance().getCashService().removeValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast §b" + offlinePlayer.getName() + " §7erfolgreich §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7abgezogen!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Dir wurden §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7von §b"+commandSender.getName()+" §7entfernt!");
                                });
                            }else if(type.equalsIgnoreCase("bank")){
                                XCurrency.getInstance().getBankService().removeValue(offlinePlayer.getUniqueId(), amount, cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast der Bank von §b" + offlinePlayer.getName() + " §7erfolgreich §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7abgezogen!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Deiner Bank wurden §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7von §b"+commandSender.getName()+" §7entfernt!");
                                });
                            }
                            break;
                        case "reset":
                            if(type.equalsIgnoreCase("cash")) {
                                XCurrency.getInstance().getCashService().resetValue(offlinePlayer.getUniqueId(), cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast das Geld von  §b" + offlinePlayer.getName() + " §7erfolgreich §7zurückgesetzt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Dein Geld wurde von §b"+commandSender.getName()+" §7zurückgesetzt!");
                                });
                            }else if(type.equalsIgnoreCase("bank")){
                                XCurrency.getInstance().getBankService().resetValue(offlinePlayer.getUniqueId(), cashAccount -> {
                                    commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast die Bank von  §b" + offlinePlayer.getName() + " §7erfolgreich §7zurückgesetzt!");
                                    if(offlinePlayer.isOnline()) Bukkit.getPlayer(targetName).sendMessage(messageService.getMessage("prefix") + "§7Das Geld auf deiner Bank wurde von §b"+commandSender.getName()+" §7zurückgesetzt!");
                                });
                            }
                            break;
                        default:
                            commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du musst set, add, remove oder reset als Aktion angeben!");
                            break;
                    }

                }else{
                    sendHelpMessage(commandSender);
                }

            }else if(args.length == 3){
                String targetName = args[1];
                if(args[0].equalsIgnoreCase("privateCash")){
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

                    if(offlinePlayer == null){
                        commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                        return true;
                    }
                    String bool = args[2];

                    boolean privateCash;

                    if(bool.equalsIgnoreCase("false")){
                        privateCash = false;
                    }else if(bool.equalsIgnoreCase("true")){
                        privateCash = true;
                    }else{
                        privateCash = true;
                    }

                    boolean finalPrivateCash = privateCash;
                    XCurrency.getInstance().getCashService().setPrivateState(offlinePlayer.getUniqueId(), privateCash ? PrivateState.PRIVATE : PrivateState.PUBLIC, cashAccount -> {
                        String privacyState = finalPrivateCash ? PrivateState.PRIVATE.toString() : PrivateState.PUBLIC.toString();
                        commandSender.sendMessage(messageService.getMessage("prefix")+" §7Du hast die Brieftasche von §b"+targetName+" §7erfolgreich auf §b"+ privacyState +" §7gestellt!");
                        if(offlinePlayer.isOnline()) Bukkit.getPlayer(offlinePlayer.getUniqueId()).sendMessage(messageService.getMessage("prefix")+"§b"+commandSender.getName()+" §7hat deine Brieftasche auf §b"+ privacyState +" §7gestellt!");
                    });
                }else if(args[0].equalsIgnoreCase("frozen")){

                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);

                    if(offlinePlayer == null){
                        commandSender.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Dieser Spieler war noch nie auf dem Server!");
                        return true;
                    }
                    String bool = args[2];

                    boolean frozen;

                    if(bool.equalsIgnoreCase("false")){
                        frozen = false;
                    }else frozen = bool.equalsIgnoreCase("true");

                    boolean finalFrozen = frozen;
                    XCurrency.getInstance().getBankService().setFrozen(offlinePlayer.getUniqueId(), frozen, bankAccount -> {
                        commandSender.sendMessage(messageService.getMessage("prefix") + "§7Du hast die Bank von §b"+offlinePlayer.getName()+" §7erfolgreich §b"+(finalFrozen ?"eingefroren":"freigegeben")+"§7!");
                        if(offlinePlayer.isOnline()) Bukkit.getPlayer(offlinePlayer.getUniqueId()).sendMessage(messageService.getMessage("prefix") + "§7Dein Konto wurde von §b"+commandSender.getName()+" §7"+(finalFrozen ?"eingefroren":"freigegeben")+"!");
                    });

                }else{
                    sendHelpMessage(commandSender);
                }
            }else{
                sendHelpMessage(commandSender);
            }
        }

        return false;
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendHelpMessage(CommandSender player){
        player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/eco §7<§bcash§7|§bbank§7> <§bset§7|§badd§7|§bremove§7|§breset§7> <§bSpieler§7> <§bAmount§7>");
        player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/eco §7<§bprivateCash§7> <§bSpieler§7> <§btrue§7|§bfalse§7>");
        player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/eco §7<§bfrozen§7> <§bSpieler§7> <§btrue§7|§bfalse§7>");
    }
}
