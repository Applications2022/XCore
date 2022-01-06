package de.ruben.xcore.changelog.service;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xcore.changelog.model.ChangeLogType;
import de.ruben.xcore.changelog.model.Changelog;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChangeLogService {

    private XChangelog xChangelog;
    private ConcurrentHashMap<UUID, Changelog> changeLogMap;

    public ChangeLogService(XChangelog xChangelog) {
        this.xChangelog = xChangelog;
        this.changeLogMap = xChangelog.getChangelogMap();
    }

    public void loadChangeLogsIntoMap(){
        getCollection()
                .find()
                .forEach((Block<? super Document>) document -> {
                    Changelog changelog = new Changelog().fromDocument(document);
                    if(changeLogMap.containsKey(changelog.getId())){
                        changeLogMap.replace(changelog.getId(), changelog);
                    }else {
                        changeLogMap.putIfAbsent(changelog.getId(), changelog);
                    }
                });
    }

    public Changelog saveChangeLog(UUID author, String title, String content, ChangeLogType changeLogType){
        Changelog changelog = new Changelog(author, title, content, new Date(System.currentTimeMillis()), changeLogType);
        changeLogMap.putIfAbsent(changelog.getId(), changelog);
        getCollection().insertOne(changelog.toDocument());
        return changelog;
    }

    public Changelog saveChangeLog(Changelog changelog){
        changeLogMap.putIfAbsent(changelog.getId(), changelog);
        getCollection().insertOne(changelog.toDocument());
        return changelog;
    }

    public Changelog getChangeLog(UUID id){
        return changeLogMap.containsKey(id) ? changeLogMap.get(id) : new Changelog().fromDocument(getCollection().find(Filters.eq("_id", id)).first());
    }

    public Collection<Changelog> getChangeLogs(){
        return changeLogMap.values();
    }

    public List<Changelog> getChangeLogsNewestFirst(){
        return getChangeLogs().stream().sorted((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate())).collect(Collectors.toList());
    }

    public List<Changelog> getChangeLogs(UUID author){
        return getChangeLogs().stream().filter(changelog -> changelog.getAuthor() == author).collect(Collectors.toList());
    }

    public long deleteChangeLog(UUID id){
        changeLogMap.remove(id);
        return getCollection().deleteOne(Filters.eq("_id", id)).getDeletedCount();
    }

    private MongoDBStorage getMongoDBStorage(){
        return XChangelog.getInstance().getMongoDBStorage();
    }

    private MongoCollection<Document> getCollection(){
        return getMongoDBStorage().getMongoClient().getDatabase("ChangeLogs").getCollection("Data_Changelogs");
    }
}
