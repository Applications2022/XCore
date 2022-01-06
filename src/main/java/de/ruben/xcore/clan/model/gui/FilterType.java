package de.ruben.xcore.clan.model.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FilterType {
    ALL("Alle"),
    OPEN_ONLY("Nur Geöffnete"),
    CLOSED_ONLY("Nur Geschlossene"),
    JOIN_ONLY("Nur auf Anfrage");

    private String displayName;

    public static FilterType getNextFilterType(FilterType filterType){
        switch (filterType){
            case ALL:
                return OPEN_ONLY;
            case OPEN_ONLY:
                return CLOSED_ONLY;
            case CLOSED_ONLY:
                return JOIN_ONLY;
            default:
                return ALL;
        }
    }

}
