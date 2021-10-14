package de.ruben.xcore.currency.command;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.currency.account.CashAccount;
import de.ruben.xcore.currency.account.type.PrivateState;
import de.ruben.xcore.profile.model.TransferData;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.message.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCashCommands implements CommandExecutor {

    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    private final ProfileService profileService = new ProfileService();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        String commandLabel = command.getLabel();

        Player player = (Player) sender;

        if(commandLabel.equalsIgnoreCase("cash")){
            if(args.length == 0) {
                player.sendMessage(messageService.getMessage("prefix")+"§7Zur Zeit befinden sich §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(XCurrency.getInstance().getCashService().getValue(player.getUniqueId())) + "€ §7in deiner Brieftasche!");
            }else if(args.length >= 1){
                String targetName = args[0];
                Player target = Bukkit.getPlayer(targetName);

                if(target == null){
                    player.sendMessage(messageService.getMessage("offlinePlayer"));
                    return true;
                }



                XCurrency.getInstance().getCashService().getAccountAsync(target.getUniqueId(), cashAccount -> {
                    if(!player.hasPermission("addictzone.cash.showother.bypass")) {
                        if (cashAccount.getPrivateState() == PrivateState.PRIVATE && target.getUniqueId() != player.getUniqueId()) {
                            player.sendMessage(messageService.getMessage("prefix") + "§cFehler: §7" + target.getName() + " hat seine Bargeld Infos auf Privat gestellt!");
                            return;
                        }
                    }

                    player.sendMessage(messageService.getMessage("prefix")+"§7Zur Zeit befinden sich §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(cashAccount.getValue())+"€ §7in der Brieftasche von §b"+target.getName()+".");

                });

            }
            return true;
        }

        if(commandLabel.equalsIgnoreCase("pay")){

            if(args.length == 2){

                Player target = Bukkit.getPlayer(args[0]);
                String amountString = args[1];

                if(target.getUniqueId().toString().equals(player.getUniqueId().toString())){
                    player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du kannst dir selbst kein Geld überweisen!");
                    return true;
                }

                if(target == null){
                    player.sendMessage(messageService.getMessage("offlinePlayer"));
                    return true;
                }

                if(!isDouble(amountString)){
                    player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Gebe eine Zahl als Geldbetrag an!");
                    return true;
                }

                double amount = Double.parseDouble(amountString);

                CashAccount cashAccount = XCurrency.getInstance().getCashService().getAccount(player.getUniqueId());

                if(amount<=0){
                    player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du musst einen größeren Betrag angeben!");
                    return true;
                }

                if(cashAccount.getValue()<amount){
                    player.sendMessage(messageService.getMessage("prefix")+"§cFehler: §7Du hast dazu zu wenig Geld!");
                    return true;
                }

                XCurrency.getInstance().getCashService().addValue(target.getUniqueId(), amount);
                cashAccount.setValue(cashAccount.getValue()-amount);
                XCurrency.getInstance().getCashService().updateCashAccount(player.getUniqueId(), cashAccount);

                player.sendMessage(messageService.getMessage("prefix")+"§7Du hast §b"+target.getName()+" "+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7überwiesen!");
                target.sendMessage(messageService.getMessage("prefix")+"§b"+player.getName()+" §7hat dir §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount)+"€ §7überwiesen!");

                profileService.getProfileAsync(player.getUniqueId(), playerProfile -> {
                    TransferData transferData = playerProfile.getTransferData();
                    transferData.setTransferCount(transferData.getTransferCount()+1);
                    transferData.setTransferredAmount(transferData.getTransferredAmount()+amount);
                    playerProfile.setTransferData(transferData);

                    profileService.updateProfile(player.getUniqueId(), playerProfile);
                });

            }else{
                player.sendMessage(messageService.getMessage("prefix")+"§7Benutze: §b/pay §7<§bSpieler§7> <§bBetrag§7>");
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
}
