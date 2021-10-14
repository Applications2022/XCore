package de.ruben.xcore.changelog.model;

public enum ChangeLogType {
    NEW("§2§lNeu"),
    CHANGE("§9§lAktualisierung"),
    FIX("§c§lFehlerbehebung"),
    EVENT("§e§lEvent");

    String name;

    ChangeLogType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
