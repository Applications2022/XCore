package de.ruben.xcore.tutorialcenter.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.currency.XCurrency;
import de.ruben.xcore.tutorialcenter.XTutorialCenter;
import de.ruben.xcore.tutorialcenter.model.TutorialModule;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.storage.MongoDBStorage;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.stream.Collectors;

public class TutorialModuleService {

    public boolean existTutorialModule(int id){
        return getTutorialMap().containsKey(id);
    }

    public void createTutorialModule(int id, BookMeta bookMeta, List arrayList, ItemStack itemStack){
        if(getTutorialModule(id) == null){
            TutorialModule tutorialModule = new TutorialModule(UUID.randomUUID(), arrayList, bookMeta, itemStack, id);

            getMongoDBStorage().insertOne(tutorialModule.toDocument());
            getTutorialMap().putIfAbsent(tutorialModule.getId(), tutorialModule);
        }
    }

    public void deleteTutorialModule(int id){
        if(existTutorialModule(id)){
            getMongoDBStorage().deleteOne(Filters.eq("id", id));
            getTutorialMap().remove(id);
        }
    }

    public TutorialModule removeURL(Integer id, String URL){
        TutorialModule tutorialModule = getTutorialModule(id);
        List<String> urls = tutorialModule.getPictureURLS();
        urls.removeIf(s -> s.equalsIgnoreCase(URL));

        tutorialModule.setPictureURLS(urls);

        return modifyTutorialModule(tutorialModule);
    }

    public TutorialModule addURL(Integer id, String URL){
        TutorialModule tutorialModule = getTutorialModule(id);

        List<String> urls = tutorialModule.getPictureURLS();
        urls.add(URL);

        tutorialModule.setPictureURLS(urls);

        return modifyTutorialModule(tutorialModule);

    }

    public List<TutorialModule> loadTutorialModules(){

        List<TutorialModule> modules = getMongoDBStorage().find().into(new ArrayList<>()).stream().map(document -> new TutorialModule(document)).collect(Collectors.toList());

        modules.forEach(tutorialModule -> getTutorialMap().putIfAbsent(tutorialModule.getId(), tutorialModule));

        return modules;
    }

    public TutorialModule getTutorialModule(Integer id){
        if(getTutorialMap().containsKey(id)){
            return getTutorialMap().get(id);
        }else {
            Document document = getMongoDBStorage().find(Filters.eq("id", id)).first();
            if(document != null) {
                TutorialModule tutorialModule = new TutorialModule(document);
                getTutorialMap().putIfAbsent(id, tutorialModule);
                return tutorialModule;
            }else{
                return null;
            }
        }
    }

    public TutorialModule modifyTutorialModule(TutorialModule tutorialModule){
        if(getTutorialMap().containsKey(tutorialModule.getUuid())){
            getTutorialMap().replace(tutorialModule.getId(), tutorialModule);
        }

        XDevApi.getInstance().getxScheduler().async(() -> {
            getMongoDBStorage().replaceOne(Filters.eq("_id", tutorialModule.getUuid()), tutorialModule.toDocument());
        });

        return tutorialModule;
    }


    private MongoCollection<Document> getMongoDBStorage(){
        return XTutorialCenter.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Tutorials").getCollection("Data_TutorialModules");
    }

    public LinkedHashMap<Integer, TutorialModule> getTutorialMap(){
        return XTutorialCenter.getInstance().getTutorials();
    }

}
