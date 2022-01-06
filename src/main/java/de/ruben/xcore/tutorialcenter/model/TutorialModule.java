package de.ruben.xcore.tutorialcenter.model;

import com.google.gson.JsonObject;
import de.ruben.xcore.util.BukkitSerialization;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.labymod.LabyModProtocol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.labymod.serverapi.common.widgets.WidgetScreen;
import net.labymod.serverapi.common.widgets.components.widgets.ButtonWidget;
import net.labymod.serverapi.common.widgets.components.widgets.ImageWidget;
import net.labymod.serverapi.common.widgets.util.Anchor;
import net.labymod.serverapi.common.widgets.util.EnumScreenAction;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TutorialModule {
    private UUID uuid;
    private List<String> pictureURLS;
    private BookMeta alternativeBookMeta;
    private ItemStack itemStack;
    private int id;

    public TutorialModule(Document document){
        this.uuid = document.get("_id", UUID.class);
        this.pictureURLS = document.getList("urls", String.class);
        try {
            this.alternativeBookMeta = BukkitSerialization.bookMetaFromBase64(document.getString("bookMeta"));
            this.itemStack = BukkitSerialization.itemStackFromBase64(document.getString("itemStack"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.id = document.getInteger("id");

    }

    public Document toDocument(){
        Document document = new Document("_id", uuid);
        document.append("urls", pictureURLS);
        document.append("bookMeta", BukkitSerialization.bookMetaToBase64(alternativeBookMeta));
        document.append("itemStack", BukkitSerialization.itemStackToBase64(itemStack));
        document.append("id", id);

        return document;
    }

    public void performOpen(Player player, int page){

        if(XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())){
            if(pictureURLS.isEmpty()){
                if(alternativeBookMeta != null){
                    ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
                    itemStack.setItemMeta(alternativeBookMeta);

                    player.openBook(itemStack);
                }else{
                    player.closeInventory();
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Weitere Inhalte dieses Tutorials sind in Arbeit!");
                }
            }else{
                openGUI(player, page);
            }
        }else{
            if(alternativeBookMeta != null){
                ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK);
                itemStack.setItemMeta(alternativeBookMeta);

                player.openBook(itemStack);
            }else{
                player.closeInventory();
                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Weitere Inhalte dieses Tutorials sind in Arbeit!");
            }
        }
    }


    public void openGUI(Player player, int page) {
        WidgetScreen screen = new WidgetScreen(id);

        Anchor anchor = new Anchor(50, 50);

        if((page+1) < getPictureURLS().size()) {
            ButtonWidget button = new ButtonWidget(page + 1, anchor, 50, 122, "->", 40, 20);
            button.setCloseScreenOnClick(false);
            screen.addWidget(button);
        }

        if((page-1) >= 0) {
            ButtonWidget backButton = new ButtonWidget(page - 1, anchor, -90, 122, "<-", 40, 20);
            backButton.setCloseScreenOnClick(false);
            screen.addWidget(backButton);
        }

        screen.addWidget(new ImageWidget(1250, anchor, -200, -112, 400, 224, getPictureURLS().get(page)));

        JsonObject object = screen.toJsonObject(EnumScreenAction.OPEN);

        LabyModProtocol.sendLabyModMessage( player, "screen", object );
    }


}
