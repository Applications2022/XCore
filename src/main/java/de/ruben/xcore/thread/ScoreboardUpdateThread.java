package de.ruben.xcore.thread;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.labymod.display.EconomyDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreboardUpdateThread extends Thread{

    final int interval = 20*3;

    @Override
    public void run() {
        while (true){
            try {
                sleep(interval* 50L);
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
                        XDevApi.getInstance().getLabyModDisplay().getEconomyDisplay().updateBalanceDisplay(player, EconomyDisplay.EnumBalanceType.CASH,  Math.round(XCurrency.getInstance().getCashService().getValue(player.getUniqueId()).intValue()));
                        XDevApi.getInstance().getLabyModDisplay().getEconomyDisplay().updateBalanceDisplay(player, EconomyDisplay.EnumBalanceType.BANK,  Math.round(XCurrency.getInstance().getBankService().getValue(player.getUniqueId()).intValue()));

                    }
                }
            } catch (InterruptedException e) {
                XDevApi.getInstance().consoleMessage("ScoreboardUpdateThread interrupted!", true);
            }
        }


    }

}
