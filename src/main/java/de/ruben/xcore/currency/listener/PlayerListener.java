package de.ruben.xcore.currency.listener;

import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.itemstorage.XItemStorage;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.labymod.display.EconomyDisplay;
import de.ruben.xdevapi.message.MessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ExecutorService;

public class PlayerListener implements Listener {

    private final MessageService messageService = XDevApi.getInstance().getMessageService();

    private final ExecutorService executorService = XItemStorage.getInstance().getExecutorService();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        event.getPlayer().sendMessage(messageService.getMessage("prefix")+"§7Deine Daten werden geladen...");
        executorService.execute(() -> {
            XCurrency.getInstance().getCashService().getAccount(event.getPlayer().getUniqueId());
            XCurrency.getInstance().getBankService().getAccount(event.getPlayer().getUniqueId());
            XDevApi.getInstance().getLabyModDisplay().getEconomyDisplay().updateBalanceDisplay(event.getPlayer(), EconomyDisplay.EnumBalanceType.CASH,  Math.round(XCurrency.getInstance().getCashService().getValue(event.getPlayer().getUniqueId()).intValue()));
            XDevApi.getInstance().getLabyModDisplay().getEconomyDisplay().updateBalanceDisplay(event.getPlayer(), EconomyDisplay.EnumBalanceType.BANK,  Math.round(XCurrency.getInstance().getBankService().getValue(event.getPlayer().getUniqueId()).intValue()));
            event.getPlayer().sendMessage(messageService.getMessage("prefix")+"§7Deine Daten wurden erfolgreich geladen!");
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        executorService.execute(() -> {
            XCurrency.getInstance().getCashService().removeCacheEntry(event.getPlayer().getUniqueId());
            XCurrency.getInstance().getBankService().removeCacheEntry(event.getPlayer().getUniqueId());
        });
    }
}
