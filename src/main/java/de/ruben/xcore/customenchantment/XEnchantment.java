package de.ruben.xcore.customenchantment;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.command.TestEnchantmentCommand;
import de.ruben.xcore.customenchantment.listener.EventListener;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xcore.customenchantment.model.enchantment.MinerEnchant;
import de.ruben.xcore.customenchantment.model.enchantment.TelekinesisEnchant;
import de.ruben.xcore.subsystem.SubSystem;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XEnchantment implements SubSystem {

    private static XEnchantment instance;

    private static HashMap<Enchantment, String> enchantmentNames;

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void onEnable() {
        instance = this;

        threadPoolExecutor = new ThreadPoolExecutor(0, 3, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        XCore.getInstance().getCommand("testench").setExecutor(new TestEnchantmentCommand());
        Bukkit.getPluginManager().registerEvents(new EventListener(), XCore.getInstance());

        CustomEnchantment.registerEnchantment(new TelekinesisEnchant());
        CustomEnchantment.registerEnchantment(new MinerEnchant());

        enchantmentNames = new HashMap<>();

        enchantmentNames.put(Enchantment.DIG_SPEED, "Effizienz");
    }

    @Override
    public void onDisable() {
        getThreadPoolExecutor().shutdownNow();
    }

    public static XEnchantment getInstance() {
        return instance;
    }

    public static HashMap<Enchantment, String> getEnchantmentNames() {
        return enchantmentNames;
    }

    public ExecutorService getThreadPoolExecutor() {
        return threadPoolExecutor;
    }
}
