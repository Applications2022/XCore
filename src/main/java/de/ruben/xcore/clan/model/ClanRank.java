package de.ruben.xcore.clan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClanRank {
    private UUID uuid;
    private UUID createdBy;
    private Long createdAt;
    private String name, colorTag;
    private Integer weight;
    private List<ClanRankPermission> permissions;

    public boolean hasPermission(ClanRankPermission clanRankPermission){
        return getPermissions().contains(clanRankPermission);
    }

    public enum ClanRankPermission{
        BANK_DEPOSIT,
        BANK_WITHDRAW,
        SAFE_PUT,
        SAFE_TAKE,
        CLAN_ROLES_ASSIGN,
        CLAN_ROLES_RIGHTS_SET,
        CLAN_ROLES_DELETE,
        CLAN_ROLES_CREATE,
        INVITE_PLAYERS,
        REMOVE_PLAYERS,
        STATUS_SET,
        REQUESTS_ACCEPT,
        REQUESTS_DECLINE,
        CHANGE_NAME,
        CHANGE_TAG_NAME,
        CHANGE_TAG_COLOR
    }
}