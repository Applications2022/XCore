package de.ruben.xcore.changelog.model;

import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.util.global.TimeUtil;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Changelog {

    private UUID id, author;
    private String content;
    private String title;
    private Date createDate;
    private ChangeLogType changeLogType;

    public Changelog() {
    }

    public Changelog(UUID author, String title, String content, Date createDate, ChangeLogType changeLogType) {
        this.id = UUID.randomUUID();
        this.author = author;
        this.content = content;
        this.title = title;
        this.createDate = createDate;
        this.changeLogType = changeLogType;
    }

    public Changelog(UUID id, UUID author, String title, String content, Date createDate, ChangeLogType changeLogType) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createDate = createDate;
        this.title = title;
        this.changeLogType = changeLogType;
    }

    public UUID getAuthor() {
        return author;
    }

    public void setAuthor(UUID author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public ChangeLogType getChangeLogType() {
        return changeLogType;
    }

    public void setChangeLogType(ChangeLogType changeLogType) {
        this.changeLogType = changeLogType;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public ItemStack toItemStack(){
        ItemStack itemStack = ItemBuilder
                                    .from(Material.PAPER)
                                    .name(Component.text(title))
                                    .build();

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = new ArrayList<>();

        lore.add("§8"+id.toString());
        lore.add(" ");
        lore.add("§7Autor: §b"+ Bukkit.getOfflinePlayer(author).getName());
        lore.add("§7Typ: "+getChangeLogType().getName());
        lore.add(" ");
        String[] contents = content.split("\n");

        for(String contentRow : contents) {
            lore.add("§7"+contentRow);
        }

        lore.add(" ");

        long seconds = (System.currentTimeMillis()-getCreateDate().getTime());

//        String timeString = XDevApi.getInstance().getxUtil().getGlobal().getTimeUtil().convertSecondsHM((int) seconds);
//        timeString = timeString.startsWith(" ") ? timeString : " "+timeString;
        String timeString = new TimeUtil.TimeConverter(seconds).toString(2);
        timeString = timeString.substring(0, timeString.length()-1);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        lore.add("§7Datum: §b"+simpleDateFormat.format(createDate)+" §7(vor§b " + timeString +"§7)");

        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;

    }

    public Document toDocument(){
        Document document = new Document("_id", id);
        document.append("author", author);
        document.append("title", title);
        document.append("content", content);
        document.append("createDate", createDate);
        document.append("changeLogType", changeLogType.name());
        return document;
    }

    public Changelog fromDocument(Document document){
        UUID id = document.get("_id", UUID.class);
        UUID author = document.get("author", UUID.class);
        String title = document.getString("title");
        String content = document.getString("content");
        Date date = document.getDate("createDate");
        ChangeLogType changeLogType = ChangeLogType.valueOf(document.getString("changeLogType"));
        return new Changelog(id, author, title, content, date, changeLogType);
    }
}
