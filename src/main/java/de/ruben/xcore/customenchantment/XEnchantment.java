package de.ruben.xcore.customenchantment;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.command.AdminEnchantCommand;
import de.ruben.xcore.customenchantment.command.AdminEnchantTabCompleter;
import de.ruben.xcore.customenchantment.command.TestEnchantmentCommand;
import de.ruben.xcore.customenchantment.listener.AnvilListener;
import de.ruben.xcore.customenchantment.listener.EventListener;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xcore.customenchantment.model.enchantment.*;
import de.ruben.xcore.subsystem.SubSystem;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XEnchantment implements SubSystem {

    private static XEnchantment instance;

    private static HashMap<Enchantment, String> enchantmentNames;

    private static HashMap<String, Enchantment> enchantmentsByName;

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void onEnable() {
        instance = this;

        threadPoolExecutor = new ThreadPoolExecutor(0, 3, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        threadPoolExecutor.allowCoreThreadTimeOut(true);

        CustomEnchantment.registerEnchantment(new TelekinesisEnchant());
        CustomEnchantment.registerEnchantment(new MinerEnchant());
        CustomEnchantment.registerEnchantment(new BlockTrackerEnchant());
        CustomEnchantment.registerEnchantment(new TaschendiebEnchant());
        CustomEnchantment.registerEnchantment(new GuillotineEnchant());
        CustomEnchantment.registerEnchantment(new LumberjackEnchant());
        CustomEnchantment.registerEnchantment(new SmeltEnchant());
        CustomEnchantment.registerEnchantment(new ErfahrungEnchant());
        CustomEnchantment.registerEnchantment(new LebensraubEnchant());
        CustomEnchantment.registerEnchantment(new VerderbenEnchant());
        CustomEnchantment.registerEnchantment(new SchattenEnchant());

        enchantmentNames = new HashMap<>();
        enchantmentsByName = new HashMap<>();

        enchantmentNames.put(Enchantment.DIG_SPEED, "Effizienz");
        enchantmentNames.put(Enchantment.ARROW_DAMAGE, "St??rke");
        enchantmentNames.put(Enchantment.ARROW_INFINITE, "Unendlichkeit");
        enchantmentNames.put(Enchantment.ARROW_FIRE, "Flamme");
        enchantmentNames.put(Enchantment.ARROW_KNOCKBACK, "Schlag");
        enchantmentNames.put(Enchantment.BINDING_CURSE, "Fluch der Bindung");
        enchantmentNames.put(Enchantment.CHANNELING, "Entladung");
        enchantmentNames.put(Enchantment.DAMAGE_ALL, "Sch??rfe");
        enchantmentNames.put(Enchantment.DAMAGE_UNDEAD, "Nemesis der Gliederf????er");
        enchantmentNames.put(Enchantment.DEPTH_STRIDER, "Wasserl??ufer");
        enchantmentNames.put(Enchantment.DURABILITY, "Haltbarkeit");
        enchantmentNames.put(Enchantment.FIRE_ASPECT, "Verbrennung");
        enchantmentNames.put(Enchantment.FROST_WALKER, "Eisl??ufer");
        enchantmentNames.put(Enchantment.IMPALING, "Harpune");
        enchantmentNames.put(Enchantment.KNOCKBACK, "R??cksto??");
        enchantmentNames.put(Enchantment.LOOT_BONUS_BLOCKS, "Gl??ck");
        enchantmentNames.put(Enchantment.LOOT_BONUS_MOBS, "Pl??nderung");
        enchantmentNames.put(Enchantment.LOYALTY, "Treue");
        enchantmentNames.put(Enchantment.LUCK, "Gl??ck des Meeres");
        enchantmentNames.put(Enchantment.LURE, "K??der");
        enchantmentNames.put(Enchantment.MENDING, "Reparatur");
        enchantmentNames.put(Enchantment.MULTISHOT, "Mehrfachschuss");
        enchantmentNames.put(Enchantment.OXYGEN, "Atmung");
        enchantmentNames.put(Enchantment.PIERCING, "Durchschuss");
        enchantmentNames.put(Enchantment.PROTECTION_ENVIRONMENTAL, "Schutz");
        enchantmentNames.put(Enchantment.PROTECTION_EXPLOSIONS, "Explosionsschutz");
        enchantmentNames.put(Enchantment.PROTECTION_FALL, "Federfall");
        enchantmentNames.put(Enchantment.PROTECTION_FIRE, "Feuerschutz");
        enchantmentNames.put(Enchantment.PROTECTION_PROJECTILE, "Schusssicher");
        enchantmentNames.put(Enchantment.QUICK_CHARGE, "Schnellladen");
        enchantmentNames.put(Enchantment.RIPTIDE, "Sog");
        enchantmentNames.put(Enchantment.SILK_TOUCH, "Behutsamkeit");
        enchantmentNames.put(Enchantment.SOUL_SPEED, "Seelenl??ufer");
        enchantmentNames.put(Enchantment.SWEEPING_EDGE, "Schwungkraft");
        enchantmentNames.put(Enchantment.THORNS, "Dornen");
        enchantmentNames.put(Enchantment.VANISHING_CURSE, "Fluch des Verschwindens");
        enchantmentNames.put(Enchantment.WATER_WORKER, "Wasseraffinit??t");

        enchantmentNames.forEach((enchantment, s) -> {
            enchantmentsByName.put(s.toLowerCase(), enchantment);
        });

        XCore.getInstance().getCommand("adminenchant").setExecutor(new AdminEnchantCommand());
        XCore.getInstance().getCommand("adminenchant").setTabCompleter(new AdminEnchantTabCompleter());
        Bukkit.getPluginManager().registerEvents(new EventListener(), XCore.getInstance());
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), XCore.getInstance());

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

    public static HashMap<String, Enchantment> getEnchantmentsByName() {
        return enchantmentsByName;
    }
}
