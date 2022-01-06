package de.ruben.xcore.gamble.command;

import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.gamble.XGamble;
import de.ruben.xcore.gamble.model.GambleParticipant;
import de.ruben.xcore.gamble.model.GambleState;
import de.ruben.xcore.gamble.model.Game;
import de.ruben.xcore.gamble.model.GameState;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GambleCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player = (Player) commandSender;

        Game game = XGamble.getInstance().getGame();

        if(args.length == 0){
            if(game.isParticipant(player.getUniqueId())){
                GambleParticipant gambleParticipant = XGamble.getInstance().getGame().getParticipants().get(player.getUniqueId());
                if(game.getGameState() == GameState.GAME && game.getMultiplierState() == Game.MultiplierState.LOADING && gambleParticipant.getGambleState() == GambleState.UNKNOWN){
                    new CashService().addValue(player.getUniqueId(), gambleParticipant.getBet()*game.getMultiplier(), cashAccount -> {
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7[§2+§7] §7"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(gambleParticipant.getBet()*game.getMultiplier())+"€");
                        game.setParticipantBet(player.getUniqueId(), gambleParticipant.getBet()*game.getMultiplier());
                        game.setParticipantState(player.getUniqueId(), GambleState.WON);
                    });
                }else{
                    sendHelpMessage(player);
                }
            }else{
                sendHelpMessage(player);
            }
        }else if(args.length == 2){
            if(args[0].equalsIgnoreCase("bieten") || args[0].equalsIgnoreCase("bet")){
                if(game.getGameState() == GameState.BEFORE_GAME || game.getGameState() == GameState.WAITING_PLAYER) {
                    if (game.isParticipant(player.getUniqueId())) {
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast an dem aktuellen Spiel bereits teilgenommen!");
                        return true;
                    }

                    if (!isDouble(args[1])) {
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Zahl als Einsatz an!");
                        return true;
                    }

                    double amount = Double.parseDouble(args[1]);

                    if (new CashService().getValue(player.getUniqueId()) < amount) {
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§cDazu hast du zu wenig Geld!");
                        return true;
                    }

                    if (amount > 1000000) {
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§cDu kannst maximal 1.000.000€ bieten!");
                        return true;
                    }

                    new CashService().removeValue(player.getUniqueId(), amount, cashAccount -> {
                        game.addParticipant(new GambleParticipant(player.getUniqueId(), player.getName(), amount, GambleState.UNKNOWN));
                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(amount) + "€ §7eingezahlt!");
                    });
                }else{
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§cEs wird gerade ein Spiel gespielt! Warte bis es vorbei ist um mitspieler zu können!");
                }
            }else{
                sendHelpMessage(player);
            }
        }else{
            sendHelpMessage(player);
        }

        return false;
    }

    private void sendHelpMessage(Player player){
        player.sendMessage("§8§m--------------------------------------------------");
        player.sendMessage(" ");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/gamble §7um dein eingezahltes Geld wieder auszuzahlen.");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Benutze: §b/gamble bieten <amount> §7um dein Geld einzuzahlen.");
        player.sendMessage(" ");
        player.sendMessage("§8§m--------------------------------------------------");
    }

    private boolean isDouble(String s){
        try
        {
            Double.parseDouble(s);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }
}
