package de.ruben.xcore.gamble.service;

import de.ruben.xcore.XCore;
import de.ruben.xcore.gamble.XGamble;
import de.ruben.xcore.gamble.model.GameState;
import de.ruben.xdevapi.XDevApi;
import me.filoghost.holographicdisplays.api.beta.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import me.filoghost.holographicdisplays.api.beta.hologram.line.TextHologramLine;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class GambleService {

    public void resetPerticipantHolograms(){
        XGamble.getInstance().getParticipantHolograms().forEach(hologram -> {
            for(int i = 1; i < hologram.getLines().size(); i++){
                hologram.getLines().remove(i);
            }
        });
    }

    public void updateHolograms(boolean check){
            if(check){
                if(XGamble.getInstance().getGame().getGameState() == GameState.WAITING_PLAYER) {
                    return;
                }
            }


            XGamble.getInstance().getGameHolograms().forEach(hologram -> {
                ((TextHologramLine) hologram.getLines().get(1)).setText(XGamble.getInstance().getGame().getGameDisplayString());

            });

            XGamble.getInstance().getParticipantHolograms().forEach(hologram -> {
                List<String> strings = XGamble.getInstance().getGame().getParticipantDisplayStrings();
                for (int i = 1; i < strings.size() + 1; i++) {
                    if ((hologram.getLines().size() - 1) >= i) {
                        ((TextHologramLine) hologram.getLines().get(i)).setText(strings.get(i - 1));
                    } else {
                        hologram.getLines().appendText(strings.get(i - 1));
                    }
                }
            });

    }

    public void spawnNewGameHologram(Location location){
        Hologram hologram = HolographicDisplaysAPI.get(XCore.getInstance()).createHologram(location);

        hologram.getLines().appendText("§9§lGamble");
        hologram.getLines().appendText(XGamble.getInstance().getGame().getGameDisplayString());
        hologram.getLines().appendText("§b/gamble bieten §7um teilzunehmen!");

        XGamble.getInstance().getGameHolograms().add(hologram);
    }

    public void spawnNewParticipantHologram(Location location){
        Hologram hologram = HolographicDisplaysAPI.get(XCore.getInstance()).createHologram(location);
        hologram.getLines().appendText("§9§lTeilnehmer");

        XGamble.getInstance().getGame().getParticipantDisplayStrings().forEach(s -> {
            hologram.getLines().appendText(s);
        });

        XGamble.getInstance().getParticipantHolograms().add(hologram);
    }

    public List<Hologram> spawnParticipantDisplayHolograms(){
        List<Hologram> participantHolograms = new ArrayList<>();

        getParticipantLocations().forEach(location -> {
            Hologram hologram = HolographicDisplaysAPI.get(XCore.getInstance()).createHologram(location);
            hologram.getLines().appendText("§9§lTeilnehmer");

            XGamble.getInstance().getGame().getParticipantDisplayStrings().forEach(s -> {
                hologram.getLines().appendText(s);
            });

        });

        return participantHolograms;
    }


    public List<Hologram> spawnGameDisplayHolograms(){
        List<Hologram> gameHolograms = new ArrayList<>();

        getGameLocations().forEach(location -> {
            Hologram hologram = HolographicDisplaysAPI.get(XCore.getInstance()).createHologram(location);

            hologram.getLines().appendText("§9§lGamble");
            hologram.getLines().appendText(XGamble.getInstance().getGame().getGameDisplayString());
            hologram.getLines().appendText("§b/gamble bieten §7um teilzunehmen!");

            gameHolograms.add(hologram);
        });

        return gameHolograms;
    }

    private List<Location> getGameLocations(){
        return XGamble.getInstance().getGambleLocations().getOrDefault("game", new ArrayList<>());
    }

    private List<Location> getParticipantLocations(){
        return XGamble.getInstance().getGambleLocations().getOrDefault("participants", new ArrayList<>());
    }



}
