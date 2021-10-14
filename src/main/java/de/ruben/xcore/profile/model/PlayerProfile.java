package de.ruben.xcore.profile.model;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerProfile {
    private long firstJoin, lastJoin, messages, commands;
    private TransferData transferData;
    private int playerKills, monsterKills, died, skyDrops;

    public PlayerProfile(long firstJoin, long lastJoin, long messages, long commands, TransferData transferData, int playerKills, int monsterKills, int died, int skyDrops) {
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.messages = messages;
        this.commands = commands;
        this.transferData = transferData;
        this.playerKills = playerKills;
        this.monsterKills = monsterKills;
        this.died = died;
        this.skyDrops = skyDrops;
    }

    public PlayerProfile() {
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    public long getMessages() {
        return messages;
    }

    public void setMessages(long messages) {
        this.messages = messages;
    }

    public long getCommands() {
        return commands;
    }

    public void setCommands(long commands) {
        this.commands = commands;
    }

    public TransferData getTransferData() {
        return transferData;
    }

    public void setTransferData(TransferData transferData) {
        this.transferData = transferData;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }

    public int getMonsterKills() {
        return monsterKills;
    }

    public void setMonsterKills(int monsterKills) {
        this.monsterKills = monsterKills;
    }

    public int getDied() {
        return died;
    }

    public void setDied(int died) {
        this.died = died;
    }

    public int getSkyDrops() {
        return skyDrops;
    }

    public void setSkyDrops(int skyDrops) {
        this.skyDrops = skyDrops;
    }

    public Document toDocument(UUID uuid){
        Document profileDocument = new Document("_id", uuid);
        return appendDocumentParams(profileDocument);
    }

    public Document toDocument(){
        Document profileDocument = new Document();
        return appendDocumentParams(profileDocument);
    }

    @NotNull
    private Document appendDocumentParams(Document profileDocument) {
        profileDocument.append("firstJoin", getFirstJoin());
        profileDocument.append("lastJoin", getLastJoin());
        profileDocument.append("messages", getMessages());
        profileDocument.append("commands", getCommands());
        profileDocument.append("transferCount", getTransferData().getTransferCount());
        profileDocument.append("transferredAmount", getTransferData().getTransferredAmount());
        profileDocument.append("playerKills", getPlayerKills());
        profileDocument.append("monsterKills", getMonsterKills());
        profileDocument.append("died", getDied());
        profileDocument.append("skydrops", getSkyDrops());

        return profileDocument;
    }

    public PlayerProfile fromDocument(Document result){
        return new PlayerProfile(result.getLong("firstJoin")
                , result.getLong("lastJoin")
                , result.getLong("messages")
                , result.getLong("commands")
                , new TransferData(result.getLong("transferCount"), result.getDouble("transferredAmount"))
                , result.getInteger("playerKills")
                , result.getInteger("monsterKills")
                , result.getInteger("died")
                , result.getInteger("skydrops"));
    }
}
