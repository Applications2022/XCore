package de.ruben.xcore.scoreboard;

import de.ruben.xcore.currency.service.CashService;
import de.ruben.xdevapi.XDevApi;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PlayerSideBar {

    public void sendNewScoreBoard(Player player){

        Scoreboard scoreboard = new Scoreboard();

        IChatBaseComponent iChatBaseComponent = new ChatMessage("§9§lAddictZone");

        ScoreboardObjective scoreboardObjective = scoreboard.registerObjective("AddictZone", IScoreboardCriteria.DUMMY, iChatBaseComponent, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);

        PacketPlayOutScoreboardObjective removepack = new PacketPlayOutScoreboardObjective(scoreboardObjective, 1);
        PacketPlayOutScoreboardObjective createpack = new PacketPlayOutScoreboardObjective(scoreboardObjective, 0);
        PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, scoreboardObjective);

        scoreboardObjective.setDisplayName(iChatBaseComponent);

        scoreboard.setDisplaySlot(1, scoreboardObjective);

        sendPacket(player, removepack);
        sendPacket(player, createpack);
        sendPacket(player, display);

        createScoreBoardTeam(scoreboard, player, "onlinePlayer", "§7➥ §b"+ Bukkit.getServer().getOnlinePlayers().size()+"§7/§b"+Bukkit.getServer().getMaxPlayers(), "", "§a");

        if(!XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
            createScoreBoardTeam(scoreboard, player, "money", "§7➥ §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(new CashService().getValue(player.getUniqueId()))+"€", "", "§p");
        }

        getScoreboardScores(player).forEach(packetPlayOutScoreboardScore -> sendPacket(player, packetPlayOutScoreboardScore));

        XScoreBoard.getScoreBoardsbyUUID().put(player.getUniqueId(), scoreboard);
    }

    public void upadteScoreBoard(Player player){
        if(!XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
           updateScoreBoardTeam(XScoreBoard.getScoreBoardsbyUUID().get(player.getUniqueId()), player, "money", "§7➥ §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(new CashService().getValue(player.getUniqueId()))+"€", "");
        }
        updateScoreBoardTeam(XScoreBoard.getScoreBoardsbyUUID().get(player.getUniqueId()), player, "onlinePlayer", "§7➥ §b"+ Bukkit.getServer().getOnlinePlayers().size()+"§7/§b"+Bukkit.getServer().getMaxPlayers(), "");


    }

    public boolean hasScoreBoard(Player player){
        return XScoreBoard.getScoreBoardsbyUUID().containsKey(player.getUniqueId());
    }

    private List<PacketPlayOutScoreboardScore> getScoreboardScores(Player player){
        if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
            System.out.println("yeah2");
            return Arrays.asList(
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§1 ", 9),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lSpieler§7:", 8),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§a", 7),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§c ", 6),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lOnlinezeit§7:", 5),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§7➥ §bKommt bald", 4),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", " ", 3),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lTeamspeak§7:", 2),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§7➥ §bAddictZone.net", 1)
            );
        }else{
            return Arrays.asList(
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§1 ", 12),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lSpieler§7:", 11),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§a", 10),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", " ", 9),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lKonto§7:", 8),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§p", 7),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§2 ", 6),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lOnlinezeit§7:", 5),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§7➥ §bKommt bald", 4),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§0 ", 3),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§9§lTeamspeak§7:", 2),
                    new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, "AddictZone", "§7➥ §bAddictZone.net", 1)
            );
        }
    }

    private ScoreboardTeam createScoreBoardTeam(Scoreboard scoreboard, Player player, String name, String prefix, String suffix, String identifier){
        ScoreboardTeam team = scoreboard.createTeam(name);
        team.setPrefix(new ChatMessage(prefix));
        team.setSuffix(new ChatMessage(suffix));
        team.getPlayerNameSet().add(identifier);

        sendPacket(player, new PacketPlayOutScoreboardTeam(team, 0));
        return team;
    }

    private ScoreboardTeam updateScoreBoardTeam(Scoreboard scoreboard, Player player, String name, String prefix, String suffix){
        ScoreboardTeam team = scoreboard.getTeam(name);

        if(team != null){

            team.setPrefix(new ChatMessage(prefix));
            team.setSuffix(new ChatMessage(suffix));
            sendPacket(player, new PacketPlayOutScoreboardTeam(team, 2));
        }

        return team;
    }

    private void sendPacket(Player player, Packet<?> packet){
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
