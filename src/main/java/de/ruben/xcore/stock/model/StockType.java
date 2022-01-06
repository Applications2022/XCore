package de.ruben.xcore.stock.model;

import org.bukkit.Material;

public enum StockType {

    CRYPTOCURRENCY(Material.GOLD_NUGGET, 1),
    STOCK(Material.PAPER, 0),
    PROFILE(null, -1);

    private Material material;
    private Integer weight;

    StockType(Material material, Integer weight){
        this.material = material;
        this.weight = weight;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getWeight() {
        return weight;
    }
}
