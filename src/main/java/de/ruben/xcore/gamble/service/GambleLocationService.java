package de.ruben.xcore.gamble.service;

import com.mongodb.client.MongoCollection;
import de.ruben.xcore.XCore;
import de.ruben.xcore.gamble.XGamble;
import de.ruben.xdevapi.XDevApi;
import org.bson.Document;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GambleLocationService {

    public void addLocation(Location location, LocationType locationType){
        Document document = getDocumentFromLocation(location, locationType);

        if(locationType == LocationType.GAME_DISPLAY){
            addGameDisplayLocation(location);
        }else{
            addParticipantLocation(location);
        }

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().insertOne(document);
        });
    }

    public void loadLocations(){
        getCollection().find().into(new ArrayList<>()).forEach(document -> {
            String name = document.getString("_id");
            Location location = getLocationFromDocument(document);

            if(name.startsWith("participants_")){
                addParticipantLocation(location);
            }else{
                addGameDisplayLocation(location);
            }

        });
    }

    private Document getDocumentFromLocation(Location location, LocationType locationType){
        Document document = new Document("_id", locationType.prefix+"-"+ UUID.randomUUID().toString());

        location.serialize().forEach((s, o) -> {
            document.append(s, o);
        });

        return document;
    }

    private Location getLocationFromDocument(Document document){
        document.remove("_id");
        return Location.deserialize(document);
    }

    private void addGameDisplayLocation(Location location){
        if(!getLocations().containsKey("game")){
            getLocations().put("game", new ArrayList<>());
        }

        List<Location> locations = getLocations().get("game");
        if(!locations.contains(location)){
            locations.add(location);
        }

        new GambleService().spawnNewGameHologram(location);
    }

    private void addParticipantLocation(Location location){
        if(!getLocations().containsKey("participants")){
            getLocations().put("participants", new ArrayList<>());
        }

        List<Location> locations = getLocations().get("participants");
        if(!locations.contains(location)){
            locations.add(location);
        }

        new GambleService().spawnNewParticipantHologram(location);
    }

    private MongoCollection<Document> getCollection(){
        return XCore.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Gamble").getCollection("Data_GambleLocations");
    }

    private ConcurrentHashMap<String, List<Location>> getLocations(){
        return XGamble.getInstance().getGambleLocations();
    }

    public enum LocationType{
        GAME_DISPLAY("game_"),
        PARTICIPANT_DISPLAY("participants_");

        private String prefix;

        LocationType(String prefix){
            this.prefix = prefix;
        }
    }

}
