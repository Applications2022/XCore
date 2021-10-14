package de.ruben.xcore.itemstorage.model;

import com.google.gson.Gson;
import de.ruben.xcore.XCore;
import de.ruben.xcore.itemstorage.service.BarrelStorageService;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class BarrelStorage {

    private final Gson gson = new Gson();

    private int level;
    private HashMap<Integer, Page> pages;

    public BarrelStorage(int level, HashMap<Integer, Page> pages) {
        this.level = level;
        this.pages = pages;
    }

    public BarrelStorage(){
        this.level = 0;
        this.pages = new HashMap<>();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public HashMap<Integer, Page> getPages() {
        return pages;
    }

    public void setPages(HashMap<Integer, Page> pages) {
        this.pages = pages;
    }

    public void addPage(Page page){
        getPages().put(getPages().size()+1, page);
    }

    public void addPage(Integer index, Page page){
        if(getPages().containsKey(index)) {
            getPages().replace(index, page);
        }else{
            getPages().put(index, page);
        }
    }

    public boolean hasNextPage(int page){
        return getPages().size()>page;
    }

    public boolean hasPreviousPage(int page){
        return (getPages().size()-page)<getPages().size()-1;
    }

    public boolean isLastPage(int page){
        return getPages().size() - ((page + 1) * getPages().get(page).getRows()) < 1;
    }

    public void upgradePages(int level){
        HashMap<Integer, Page> pageHashMap = getPages();

        new BarrelStorageService().getBarrelStorage(level).getPages().forEach((integer, page) -> {
            if(pageHashMap.containsKey(integer) && pageHashMap.get(integer).getRows()!=4){
                Page toReplace = getPages().get(integer);
                toReplace.setRows(page.getRows());
                pageHashMap.replace(integer, toReplace);
            }else{
                pageHashMap.put(integer, page);
            }
        });

        setPages(pageHashMap);

    }

    public PersistentDataContainer getContainer(@NotNull PersistentDataContainer persistentDataContainer){

        NamespacedKey levelKey = new NamespacedKey(XCore.getInstance(), "barrelstorge-level");

        if(persistentDataContainer.has(levelKey, PersistentDataType.INTEGER)){
            persistentDataContainer.remove(levelKey);
        }

        persistentDataContainer.set(levelKey, PersistentDataType.INTEGER, getLevel());

        getPages().forEach((index, page) -> {
            NamespacedKey pageKey = new NamespacedKey(XCore.getInstance(), "barrelstorge-page-"+index);

            if(persistentDataContainer.has(pageKey, PersistentDataType.STRING)){
                persistentDataContainer.remove(pageKey);
            }

            persistentDataContainer.set(pageKey, PersistentDataType.STRING, gson.toJson(page));
        });

        return persistentDataContainer;
    }

    public PersistentDataContainer updateBarrel(@NotNull PersistentDataContainer persistentDataContainer){

        persistentDataContainer.getKeys().forEach(namespacedKey -> {
            if(namespacedKey.getKey().startsWith("barrelstorge")){
                persistentDataContainer.remove(namespacedKey);
            }
        });

        NamespacedKey levelKey = new NamespacedKey(XCore.getInstance(), "barrelstorge-level");

        persistentDataContainer.set(levelKey, PersistentDataType.INTEGER, getLevel());

        getPages().forEach((index, page) -> {
            NamespacedKey pageKey = new NamespacedKey(XCore.getInstance(), "barrelstorge-page-"+index);

            persistentDataContainer.set(pageKey, PersistentDataType.STRING, gson.toJson(page));
        });

        return persistentDataContainer;
    }

    public BarrelStorage fromContainer(@NotNull PersistentDataContainer persistentDataContainer){
        BarrelStorage barrelStorage = new BarrelStorage();

        persistentDataContainer.getKeys().stream()
                .forEach(namespacedKey -> {

                    if(namespacedKey.getKey().equalsIgnoreCase("barrelstorge-level")){
                        barrelStorage.setLevel(persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER));
                    }

                    if(namespacedKey.getKey().startsWith("barrelstorge-page-")){
                        int index = Integer.parseInt(namespacedKey.getKey().replace("barrelstorge-page-", ""));

                        barrelStorage.addPage(index, gson.fromJson(persistentDataContainer.get(namespacedKey, PersistentDataType.STRING), Page.class));
                    }
                });

        return barrelStorage;
    }

    @Override
    public String toString() {
        return "BarrelStorage{" +
                ", level=" + level +
                ", pages=" + pages +
                '}';
    }
}
