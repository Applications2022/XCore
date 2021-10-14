package de.ruben.xcore;

import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.itemstorage.XItemStorage;
import de.ruben.xcore.profile.XProfile;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xcore.thread.RecentDataUpdateThread;
import de.ruben.xcore.thread.ScoreboardUpdateThread;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class XCore extends JavaPlugin {

    private static XCore instance;

    private Thread scoreboardThread;

    private RecentDataUpdateThread recentDataUpdateThread;

    private List<SubSystem> subSystems;

    @Override
    public void onEnable() {
        instance = this;
        this.subSystems = setSubSystems();

        subSystems.forEach(SubSystem::onEnable);

        this.scoreboardThread = new ScoreboardUpdateThread();
        scoreboardThread.start();

        this.recentDataUpdateThread = new RecentDataUpdateThread();
        recentDataUpdateThread.start();

    }

    @Override
    public void onDisable() {
        scoreboardThread.interrupt();
        recentDataUpdateThread.interrupt();

        subSystems.forEach(SubSystem::onDisable);
    }

    public static XCore getInstance() {
        return instance;
    }

    public RecentDataUpdateThread getRecentDataUpdateThread() {
        return recentDataUpdateThread;
    }

    public Thread getScoreboardThread() {
        return scoreboardThread;
    }

    public List<SubSystem> setSubSystems(){
        return List.of(new XItemStorage(), new XCurrency(), new XProfile(), new XEnchantment(), new XChangelog());
    }
}
