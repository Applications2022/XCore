package de.ruben.xcore.gamble.model;

import de.ruben.xcore.gamble.service.GambleService;
import de.ruben.xdevapi.XDevApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private final int startTimeSeconds = 10;
    private final int maxMultiplier = 5;

    private double timeToStart;
    private double multiplier;
    private double stop;
    private GameState gameState;
    private HashMap<UUID, GambleParticipant> participants;
    private MultiplierState multiplierState;

    public void runGameThreadAction(){
        switch (gameState){
            case WAITING_PLAYER -> {
                if(!getParticipants().isEmpty()){
                    switchGameState(GameState.BEFORE_GAME);
                }
                break;
            }
            case BEFORE_GAME -> {
                setTimeToStart(getTimeToStart()-1);

                if(getTimeToStart() <= 0){
                    switchGameState(GameState.LOAD_GAME);
                }

                break;
            }
            case LOAD_GAME -> {
                break;
            }
            case GAME -> {
                if(multiplierState == MultiplierState.LOADING) {
                    if(getMultiplier() <= 2.5){
                        setMultiplier(getMultiplier() + 0.0075);
                    }else if(getMultiplier() > 2.5 && getMultiplier() <= 10){
                        setMultiplier(getMultiplier() + 0.02);
                    }else if(getMultiplier() > 10 && getMultiplier() <= 12){
                        setMultiplier(getMultiplier() + 0.025);
                    }else if(getMultiplier() > 12 && getMultiplier() <= 15){
                        setMultiplier(getMultiplier() + 0.03);
                    }else if(getMultiplier() > 15){
                        setMultiplier(getMultiplier() + 0.05);
                    }

                    if (getMultiplier() >= stop) {
                        switchGameState(GameState.AFTER_GAME);
                    }
                }
                break;
            }
        }
    }

    public void switchGameState(GameState gameState){

        switch (gameState){
            case WAITING_PLAYER -> {
                setGameState(gameState);
                new GambleService().resetPerticipantHolograms();
            }
            case BEFORE_GAME -> {
                setGameState(gameState);
                setTimeToStart(startTimeSeconds*100);
                break;
            }
            case LOAD_GAME -> {
                setGameState(gameState);
                setTimeToStart(startTimeSeconds*100);
                setMultiplier(0.5);
                double newStop = getRandomDouble();
                System.out.println(newStop);
                setStop(newStop);
                XDevApi
                        .getInstance()
                        .createTaskBatch()
                        .wait(1, TimeUnit.SECONDS)
                        .doAsync(() -> {
                            switchGameState(GameState.GAME);
                        })
                        .executeBatch();
                break;
            }
            case GAME -> {
                setGameState(gameState);
                setMultiplierState(MultiplierState.LOADING);
                break;
            }
            case AFTER_GAME -> {
                XDevApi
                        .getInstance()
                        .createTaskBatch()
                        .doSync(() -> {
                            setMultiplierState(MultiplierState.FAILED);
                            getParticipants().forEach((uuid, gambleParticipant) -> {
                                if(gambleParticipant.getGambleState() == GambleState.UNKNOWN) {
                                    setParticipantState(uuid, GambleState.LOST);
                                }
                            });
                        })
                        .wait(2, TimeUnit.SECONDS)
                        .doAsync(() -> {
                            setMultiplier(0.5);
                            getParticipants().clear();
                            switchGameState(GameState.WAITING_PLAYER);
                            new GambleService().updateHolograms(false);
                        })
                        .executeBatch();
                break;
            }
        }

    }

    public List<String> getParticipantDisplayStrings(){
        return getParticipants().values().stream().limit(10).map(gambleParticipant -> gambleParticipant.getBetDisplay()).collect(Collectors.toList());
    }

    public String getGameDisplayString(){
        switch (getGameState()){
            case WAITING_PLAYER -> {
                return "§7Warte auf Spieler...";
            }
            case BEFORE_GAME -> {
                return "§7Es sind noch §b"+String.format("%.1f", (timeToStart*0.01))+"s §7bis zum Start!";
            }
            case GAME -> {
                return "§7Aktueller Multiplier: "+(getMultiplierState() == MultiplierState.FAILED ? "§4" : "§2")+String.format("%.2f", multiplier)+"x";
            }
            default -> {
                return "§7Starte neues Spiel...";
            }
        }
    }

    public void clearParticipants(){
        participants.clear();
    }

    public void addParticipant(GambleParticipant gambleParticipant){
        participants.putIfAbsent(gambleParticipant.getUuid(), gambleParticipant);
    }

    public void setParticipantState(UUID uuid, GambleState gambleState){
        if(participants.containsKey(uuid)){
            GambleParticipant gambleParticipant = participants.get(uuid);
            gambleParticipant.setGambleState(gambleState);
            participants.replace(uuid, gambleParticipant);
        }
    }

    public void setParticipantBet(UUID uuid, Double bet){
        if(participants.containsKey(uuid)){
            GambleParticipant gambleParticipant = participants.get(uuid);
            gambleParticipant.setBet(bet);
            participants.replace(uuid, gambleParticipant);
        }
    }

    public boolean isParticipant(UUID uuid){
        return getParticipants().containsKey(uuid);
    }

    private double getRandomDouble(){
        double firstRandom = ThreadLocalRandom.current().nextDouble(1, 95);

        if(firstRandom > 0 && firstRandom <= 50){
            return ThreadLocalRandom.current().nextDouble(1, 1.6);
        }else if(firstRandom > 50 && firstRandom <= 75){
            return ThreadLocalRandom.current().nextDouble(1, 2.5);
        }else if(firstRandom > 75 && firstRandom <= 85){
            return ThreadLocalRandom.current().nextDouble(1, 5);
        }else if(firstRandom > 85 && firstRandom <= 90){
            return ThreadLocalRandom.current().nextDouble(1, 11);
        }else if(firstRandom > 90 && firstRandom <= 92){
            return ThreadLocalRandom.current().nextDouble(1, 14);
        }else{
            return ThreadLocalRandom.current().nextDouble(1, 15);
        }
    }

    public enum MultiplierState{
        UNKNOWN,
        LOADING,
        FAILED,
    }

}
