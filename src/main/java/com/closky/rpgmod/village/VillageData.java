package com.closky.rpgmod.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Состояние одной деревни: тип, центр, очки роста, число построек. */
public class VillageData {
    private final BlockPos center;
    private VillageType type;
    private int growthPoints;
    private int buildingsBuilt;
    private int population;
    private int radius;

    public VillageData(BlockPos center, VillageType type) {
        this.center = center;
        this.type = type;
        this.radius = 48;
    }

    public VillageData(BlockPos center, CompoundTag tag) {
        this.center = center;
        this.type = VillageType.byId(tag.getString("Type"));
        this.growthPoints = tag.getInt("Growth");
        this.buildingsBuilt = tag.getInt("Buildings");
        this.population = tag.getInt("Population");
        this.radius = Math.max(24, tag.getInt("Radius"));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.getId());
        tag.putInt("Growth", growthPoints);
        tag.putInt("Buildings", buildingsBuilt);
        tag.putInt("Population", population);
        tag.putInt("Radius", radius);
        return tag;
    }

    public BlockPos getCenter() {
        return center;
    }

    public VillageType getType() {
        return type;
    }

    public void setType(VillageType type) {
        this.type = type;
    }

    public int getGrowthPoints() {
        return growthPoints;
    }

    public void addGrowthPoints(int points) {
        this.growthPoints += points;
    }

    public boolean canBuild(int cost) {
        return growthPoints >= cost;
    }

    public void consumeGrowth(int cost) {
        this.growthPoints = Math.max(0, growthPoints - cost);
    }

    public int getBuildingsBuilt() {
        return buildingsBuilt;
    }

    public void onBuildingBuilt() {
        this.buildingsBuilt++;
        this.radius = Math.min(128, radius + 4);
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getRadius() {
        return radius;
    }
}
