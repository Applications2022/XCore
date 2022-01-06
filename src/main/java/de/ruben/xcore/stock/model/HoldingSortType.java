package de.ruben.xcore.stock.model;

public enum HoldingSortType {
    ALL(),
    STOCKONLY(),
    CRYPTOONLY();

    public HoldingSortType getNextSortyType(HoldingSortType holdingSortType){
        switch (holdingSortType) {
            case ALL:
                return CRYPTOONLY;
            case CRYPTOONLY:
                return STOCKONLY;
            case STOCKONLY:
                return ALL;
            default:
                return ALL;
        }
    }
}
