package de.ruben.xcore.clan.model.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortType {
    NO("Keine Sortierung"),
    MONEY("Wohlhabenste zuerst"),
    MEMBERS("Meiste Mitglieder zuerst"),
    CREATED_AT_ASC("Älteste zuerst"),
    CREATED_AT_DESC("Neuste zuerst");

    private String displayName;

    public static SortType nextSortType(SortType sortType){
        switch (sortType){
            case NO:
                return MONEY;
            case MONEY:
                return MEMBERS;
            case MEMBERS:
                return CREATED_AT_ASC;
            case CREATED_AT_ASC:
                return CREATED_AT_DESC;
            default:
                return NO;
        }
    }

}
