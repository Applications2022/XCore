package de.ruben.xcore.job.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.ruben.xcore.XCore;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.job.XJobs;
import de.ruben.xcore.job.model.JobPlayer;
import de.ruben.xdevapi.XDevApi;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.cache2k.Cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
public class JobService {

    private UUID uuid;
    private JobPlayer jobPlayer;
    private OfflinePlayer offlinePlayer;

    public JobService(UUID uuid){
        this.uuid = uuid;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        this.jobPlayer = fetchJobPlayer();
    }


    public void prestige(String job){

        if(new CashService().getValue(uuid) < ((getJobPlayer().getJobPrestige(job)+1)*1000000)){
            offlinePlayer.getPlayer().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Dazu hast du zu wenig Geld!");
            return;
        }

        new CashService().removeValue(uuid, ((getJobPlayer().getJobPrestige(job)+1)*1000000), cashAccount -> {
            getJobPlayer().setJobLevel(job, 1);
            getJobPlayer().setJobXp(job, 0.0);
            getJobPlayer().addJobPrestige(job, 1);
            updateJobPlayerOnlyIncache();

            if(offlinePlayer.isOnline()){
                offlinePlayer.getPlayer().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du bist nun §bPrestige "+getJobPlayer().getJobPrestige(job)+" §7im Job §b"+job+"§7.");
            }
        });
    }

    public void setJobLevel(String job, int level){
        getJobPlayer().setJobLevel(job, level);
        updateJobPlayerOnlyIncache();
    }

    public void setJobXp(String job, double xp){
        getJobPlayer().setJobXp(job, xp);
        updateJobPlayerOnlyIncache();
    }

    public void addJobLevel(String job, int level){
        getJobPlayer().addJobLevel(job, level);
        updateJobPlayerOnlyIncache();
    }

    public void addJobXp(String job, double xp){
        if(offlinePlayer.isOnline()){
            getJobPlayer().addJobXp(job, xp, offlinePlayer.getPlayer());
        }else {
            getJobPlayer().addJobXp(job, xp);
        }

        updateJobPlayerOnlyIncache();
    }

    public void addJobPrestige(String job, int prestige){
        getJobPlayer().addJobPrestige(job, prestige);
        updateJobPlayerOnlyIncache();
    }

    public void setJobPrestige(String job, int prestige){
        getJobPlayer().setJobPrestige(job, prestige);
        updateJobPlayerOnlyIncache();
    }

    public void updateJobPlayerOnlyIncache(){
        if(offlinePlayer.isOnline()){
            getCache().replace(uuid, jobPlayer);
        }else{
            updateJobPlayer();
        }
    }

    public void updateJobPlayer(){
        if(offlinePlayer.isOnline()) {
            getCache().replace(uuid, jobPlayer);
        }

        XDevApi.getInstance().getxScheduler().async(() -> {
            getCollection().replaceOne(Filters.eq("_id", uuid), jobPlayer.toDocument());
        });
    }

    public JobPlayer getJobPlayer() {
        return jobPlayer;
    }

    private JobPlayer fetchJobPlayer(){
        if(getCache().containsKey(uuid)){
            return getCache().get(uuid);
        }else{

            Document document = getCollection().find(Filters.eq("_id", uuid)).first();

            JobPlayer jobPlayer;

            if(document == null){
                jobPlayer = new JobPlayer(uuid, new HashMap<>());
                getCollection().insertOne(jobPlayer.toDocument());
            }else{
                jobPlayer = new JobPlayer().fromDocument(document);

            }
            if(offlinePlayer.isOnline()) {
                getCache().putIfAbsent(uuid, jobPlayer);
            }

            return jobPlayer;

        }
    }

    public void pushJobPlayerCache(){
        XJobs.getInstance().getJobPlayerCache().asMap().forEach((uuid1, jobPlayer1) -> {
            XJobs.getInstance().getMongoDBStorage().getMongoDatabase().getCollection("Data_JobPlayer").replaceOne(Filters.eq("_id", uuid1), jobPlayer1.toDocument());
            if(!Bukkit.getOfflinePlayer(uuid1).isOnline()){
                XJobs.getInstance().getJobPlayerCache().remove(uuid1);
            }
        });
    }

    public List<JobPlayer> getAllJobPlayers(){
        return getCollection().find().into(new ArrayList<>()).stream().map(document -> new JobPlayer().fromDocument(document)).collect(Collectors.toList());
    }

    public GuiItem getTopItem(String job, Material material){
        List<Component> lore = new ArrayList<>();

        List<JobPlayer> topPlayers = XJobs.getInstance().getJobTopPlayers().getTopPlayers(job);

        for(int i = 0; i < topPlayers.size(); i++){
            lore.add(Component.text("§7➥ §b"+(i+1)+". "+Bukkit.getOfflinePlayer(topPlayers.get(i).getUuid()).getName()+" §7mit Level §b"+topPlayers.get(i).getJobLevel(job)+" §8(Prestige "+topPlayers.get(i).getJobPrestige(job)+")"));
        }

        return ItemBuilder.from(material).name(Component.text("§b"+job)).lore(lore).asGuiItem();
    }

    private Cache<UUID, JobPlayer> getCache(){
        return XJobs.getInstance().getJobPlayerCache();
    }

    private MongoCollection<Document> getCollection(){
        return XCore.getInstance().getMongoDBStorage().getMongoClient().getDatabase("Job").getCollection("Data_JobPlayer");
    }
}
