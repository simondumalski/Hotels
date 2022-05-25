package me.simondumalski.hotels.utils;

import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

public class HotelWorldGenerator extends WorldCreator {

    public HotelWorldGenerator(String name) {
        super(name);
        generateStructures(false);
        type(WorldType.FLAT);
    }

}
