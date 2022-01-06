package de.ruben.xcore.clan.gui.conversation;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanDeleteConversation implements ConversationAbandonedListener {

    private ConversationFactory conversationFactory;

    public ClanDeleteConversation(){
        this.conversationFactory = new ConversationFactory(XCore.getInstance())
                .withModality(true)
                .withPrefix(new ConversationPrefix() {
                    @Override
                    public @NotNull String getPrefix(@NotNull ConversationContext conversationContext) {
                        return "§9§lClan Löschen §8| §7";
                    }
                })
                .withFirstPrompt(new DeleteClanInput())
                .withEscapeSequence("stop")
                .addConversationAbandonedListener(this)
                .withTimeout(10)
                .thatExcludesNonPlayersWithMessage("Go away!");

    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        if(!conversationAbandonedEvent.gracefulExit()){
            ((Player) conversationAbandonedEvent.getContext().getForWhom()).sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDer Löschprozess wurde abgebrochen.");
        }
    }

    public class DeleteClanInput extends FixedSetPrompt{

        public DeleteClanInput(){
            super("ja", "nein");
        }

        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            return input.equalsIgnoreCase("ja") || input.equalsIgnoreCase("nein");
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {

            if(s.equalsIgnoreCase("ja")){
                Player player = (Player) conversationContext.getForWhom();
                Clan clan = new ClanPlayerService().getClan(player.getUniqueId());
                new ClanService().deleteClan(clan.getId());

                return ClanCreateConversation.getMessagePromt("§7Dein Clan wurde erfolgreich gelöscht!");
            }else if(s.equalsIgnoreCase("nein")){
                return ClanCreateConversation.getMessagePromt("§cDu hast die Löschung abgebrochen!");
            }else{
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            Player player = (Player) conversationContext.getForWhom();

            Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

            return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("Willst du den §b"+(clan.getName())+" §7[§b"+((String) clan.getTag())+"§7] Clan löschen? (§2Ja §7| §cNein§7)");
        }
    }

}
