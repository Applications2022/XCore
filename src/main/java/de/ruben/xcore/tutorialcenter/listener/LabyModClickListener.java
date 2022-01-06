package de.ruben.xcore.tutorialcenter.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruben.xcore.tutorialcenter.model.TutorialModule;
import de.ruben.xcore.tutorialcenter.service.TutorialModuleService;
import de.ruben.xdevapi.labymod.LabyModProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class LabyModClickListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("labymod3:main")) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        ByteBuf buf = Unpooled.wrappedBuffer(message);
        String key = LabyModProtocol.readString(buf, Short.MAX_VALUE);
        String json = LabyModProtocol.readString(buf, Short.MAX_VALUE);

        if(key.equalsIgnoreCase("screen")){
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = null;
            try {
                node = objectMapper.readTree(json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            int id = node.get("id").asInt();
            int toOpenPage = node.get("widget_id").asInt();
            if(toOpenPage != 1250) {


                TutorialModule tutorialModule = new TutorialModuleService().getTutorialModule(id);

                tutorialModule.openGUI(player, toOpenPage);
            }

        }
    }
}
