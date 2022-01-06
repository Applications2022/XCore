package de.ruben.xcore.tutorialcenter;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xcore.tutorialcenter.command.AdminTutorialCommand;
import de.ruben.xcore.tutorialcenter.command.TutorialCenterCommand;
import de.ruben.xcore.tutorialcenter.listener.LabyModClickListener;
import de.ruben.xcore.tutorialcenter.model.TutorialModule;
import de.ruben.xcore.tutorialcenter.service.TutorialModuleService;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.listener.LabyModMessageListener;
import de.ruben.xdevapi.storage.MongoDBStorage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bson.codecs.configuration.CodecRegistries;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

public class XTutorialCenter implements SubSystem {

    private static XTutorialCenter instance;
    private LinkedHashMap<Integer, TutorialModule> tutorials;

    @Override
    public void onEnable() {
        this.instance = this;

        this.tutorials = new LinkedHashMap<>();

        XCore.getInstance().getServer().getMessenger().registerIncomingPluginChannel(XCore.getInstance(), "labymod3:main", new LabyModClickListener());

        new TutorialModuleService().loadTutorialModules();

        XCore.getInstance().getCommand("admintutorial").setExecutor(new AdminTutorialCommand());
        XCore.getInstance().getCommand("tutorialcenter").setExecutor(new TutorialCenterCommand());
    }

    @Override
    public void onDisable() {

    }

    public static XTutorialCenter getInstance() {
        return instance;
    }

    public MongoDBStorage getMongoDBStorage() {
        return XCore.getInstance().getMongoDBStorage();
    }

    public LinkedHashMap<Integer, TutorialModule> getTutorials() {
        return tutorials;
    }
}
