package de.ruben.xcore.gamble.thread;

import de.ruben.xcore.gamble.XGamble;
import de.ruben.xcore.gamble.service.GambleService;
import de.ruben.xcore.job.XJobs;
import de.ruben.xcore.job.model.JobTopPlayers;
import de.ruben.xdevapi.XDevApi;

import java.util.HashMap;

public class GameThread extends Thread{

    private final int interval = 2;

    @Override
    public void run() {
        while (true){
            try {
                sleep(10);

                XGamble.getInstance().getGame().runGameThreadAction();

                new GambleService().updateHolograms(true);

            } catch (InterruptedException e) {
                XDevApi.getInstance().consoleMessage("GameThread interrupted!", true);
            }
        }
    }
}
