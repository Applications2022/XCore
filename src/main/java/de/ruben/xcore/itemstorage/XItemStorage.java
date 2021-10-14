package de.ruben.xcore.itemstorage;

import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.command.AdminStorageCommand;
import de.ruben.xcore.itemstorage.listener.BlockListener;
import de.ruben.xcore.subsystem.SubSystem;
import org.bukkit.Bukkit;

import java.util.concurrent.*;

public class XItemStorage implements SubSystem {

    private static XItemStorage instance;
    public ExecutorService executorService;

    @Override
    public void onEnable(){
        instance = this;
        this.executorService = new ThreadPoolExecutor(
                1,
                5,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100)
        );

        Bukkit.getPluginManager().registerEvents(new BlockListener(), XCore.getInstance());
        XCore.getInstance().getCommand("getstorage").setExecutor(new AdminStorageCommand());
        XCore.getInstance().getCommand("storageUpgradeItem").setExecutor(new AdminStorageCommand());

    }

    @Override
    public void onDisable(){
        executorService.shutdownNow();
    }

    public static XItemStorage getInstance() {
        return instance;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
