package de.ruben.xcore.changelog;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import de.ruben.xcore.XCore;
import de.ruben.xcore.changelog.command.ChangeLogCommand;
import de.ruben.xcore.changelog.command.CreateChangelogCommand;
import de.ruben.xcore.changelog.command.EditChangelogCommand;
import de.ruben.xcore.changelog.model.ChangeLogType;
import de.ruben.xcore.changelog.model.Changelog;
import de.ruben.xcore.changelog.service.ChangeLogService;
import de.ruben.xcore.currency.codec.TransactionCodec;
import de.ruben.xcore.subsystem.SubSystem;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.codecs.configuration.CodecRegistries;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class XChangelog implements SubSystem {

    private static XChangelog instance;

    private ConcurrentHashMap<UUID, Changelog> changelogMap;

    private ChangeLogService changeLogService;

    @Override
    public void onEnable() {
        this.instance = this;

        this.changelogMap = new ConcurrentHashMap<>();

        this.changeLogService = new ChangeLogService(this);

        changeLogService.loadChangeLogsIntoMap();

        XCore.getInstance().getCommand("changelog").setExecutor(new ChangeLogCommand(this));
        XCore.getInstance().getCommand("createchangelog").setExecutor(new CreateChangelogCommand(this));
        XCore.getInstance().getCommand("editchangelog").setExecutor(new EditChangelogCommand(this));
    }

    @Override
    public void onDisable() {

    }

    public MongoDBStorage getMongoDBStorage() {
        return XCore.getInstance().getMongoDBStorage();
    }

    public static XChangelog getInstance() {
        return instance;
    }

    public ConcurrentHashMap<UUID, Changelog> getChangelogMap() {
        return changelogMap;
    }

    public ChangeLogService getChangeLogService() {
        return changeLogService;
    }
}
